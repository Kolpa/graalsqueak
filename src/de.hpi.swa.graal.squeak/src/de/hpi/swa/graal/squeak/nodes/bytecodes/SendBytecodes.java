package de.hpi.swa.graal.squeak.nodes.bytecodes;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.BranchProfile;

import de.hpi.swa.graal.squeak.exceptions.Returns.NonLocalReturn;
import de.hpi.swa.graal.squeak.exceptions.Returns.NonVirtualReturn;
import de.hpi.swa.graal.squeak.model.ClassObject;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.nodes.AbstractNode;
import de.hpi.swa.graal.squeak.nodes.DispatchSend2Node;
import de.hpi.swa.graal.squeak.nodes.LookupMethodNode;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectClassNode;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameSlotReadNode;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameStackPushNode;
import de.hpi.swa.graal.squeak.nodes.primitives.impl.ControlPrimitives.PrimExitToDebuggerNode;
import de.hpi.swa.graal.squeak.util.FrameAccess;

public final class SendBytecodes {
    public abstract static class AbstractSendNode extends AbstractInstrumentableBytecodeNode {
        protected final NativeObject selector;
        private final int argumentCount;

        @Child private AbstractLookupClassNode lookupClassNode;
        @Child private LookupMethodNode lookupMethodNode = LookupMethodNode.create();
        @Child private DispatchSend2Node dispatchSendNode;
        @Child private FrameSlotReadNode receiverNode;
        @Child private FrameStackPushNode pushNode;

        private final BranchProfile nlrProfile = BranchProfile.create();
        private final BranchProfile nvrProfile = BranchProfile.create();

        private AbstractSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object sel, final int argcount) {
            this(code, index, numBytecodes, sel, argcount, new LookupClassNode());
        }

        private AbstractSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object sel, final int argcount, final AbstractLookupClassNode lookupClassNode) {
            super(code, index, numBytecodes);
            selector = sel instanceof NativeObject ? (NativeObject) sel : code.image.doesNotUnderstand;
            argumentCount = argcount;
            this.lookupClassNode = lookupClassNode;
            dispatchSendNode = DispatchSend2Node.create(code, argumentCount);
        }

        protected AbstractSendNode(final AbstractSendNode original) {
            this(original.code, original.index, original.numBytecodes, original.selector, original.argumentCount);
        }

        @Override
        public final void executeVoid(final VirtualFrame frame) {
            final Object receiver = getReceiver(frame);
            final ClassObject rcvrClass = lookupClassNode.executeLookup(frame, receiver);
            final Object lookupResult = lookupMethodNode.executeLookup(rcvrClass, selector);
            final Object result;
            try {
                result = dispatchSendNode.executeSend(frame, selector, lookupResult, rcvrClass);
                assert result != null : "Result of a message send should not be null";
                // Insert push node lazily as dispatch might context switch or throw.
                getPushNode().execute(frame, result);
            } catch (final NonLocalReturn nlr) {
                nlrProfile.enter();
                if (nlr.getTargetContextOrMarker() == getMarker(frame) || nlr.getTargetContextOrMarker() == getContext(frame)) {
                    getPushNode().execute(frame, nlr.getReturnValue());
                } else {
                    throw nlr;
                }
            } catch (final NonVirtualReturn nvr) {
                nvrProfile.enter();
                if (nvr.getTargetContext() == getContext(frame)) {
                    getPushNode().execute(frame, nvr.getReturnValue());
                } else {
                    throw nvr;
                }
            }
        }

        private Object getReceiver(final VirtualFrame frame) {
            if (receiverNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                final int stackPointer = FrameAccess.getStackPointer(frame, code) - 1 - argumentCount;
                receiverNode = insert(FrameSlotReadNode.create(code.getStackSlot(stackPointer)));
            }
            return receiverNode.executeRead(frame);
        }

        private FrameStackPushNode getPushNode() {
            if (pushNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                pushNode = insert(FrameStackPushNode.create(code));
            }
            return pushNode;
        }

        public final Object getSelector() {
            return selector;
        }

        @Override
        public final boolean hasTag(final Class<? extends Tag> tag) {
            if (tag == StandardTags.CallTag.class) {
                return true;
            }
            if (tag == DebuggerTags.AlwaysHalt.class) {
                return PrimExitToDebuggerNode.SELECTOR_NAME.equals(selector.asStringUnsafe());
            }
            return super.hasTag(tag);
        }

        @Override
        public String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "send: " + selector.asStringUnsafe();
        }
    }

    protected abstract static class AbstractLookupClassNode extends AbstractNode {
        protected abstract ClassObject executeLookup(VirtualFrame frame, Object receiver);
    }

    @NodeInfo(cost = NodeCost.NONE)
    protected static final class LookupClassNode extends AbstractLookupClassNode {
        @Child private SqueakObjectClassNode lookupClassNode = SqueakObjectClassNode.create();

        @Override
        protected ClassObject executeLookup(final VirtualFrame frame, final Object receiver) {
            return lookupClassNode.executeLookup(receiver);
        }
    }

    protected static final class LookupSuperClassNode extends AbstractLookupClassNode {
        @Override
        protected ClassObject executeLookup(final VirtualFrame frame, final Object receiver) {
            final ClassObject methodClass = FrameAccess.getMethod(frame).getMethodClass();
            final ClassObject superclass = methodClass.getSuperclassOrNull();
            return superclass == null ? methodClass : superclass;
        }
    }

    public static final class SecondExtendedSendNode extends AbstractSendNode {
        public SecondExtendedSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int i) {
            super(code, index, numBytecodes, code.getLiteral(i & 63), i >> 6);
        }
    }

    public static final class SendLiteralSelectorNode extends AbstractSendNode {
        public SendLiteralSelectorNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object selector, final int argCount) {
            super(code, index, numBytecodes, selector, argCount);
        }

        public static AbstractInstrumentableBytecodeNode create(final CompiledCodeObject code, final int index, final int numBytecodes, final int literalIndex, final int argCount) {
            final Object selector = code.getLiteral(literalIndex);
            return new SendLiteralSelectorNode(code, index, numBytecodes, selector, argCount);
        }
    }

    public static final class SendSpecialSelectorNode extends AbstractSendNode {
        private SendSpecialSelectorNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object selector, final int argcount) {
            super(code, index, numBytecodes, selector, argcount);
        }

        public static SendSpecialSelectorNode create(final CompiledCodeObject code, final int index, final int selectorIndex) {
            final NativeObject specialSelector = code.image.getSpecialSelector(selectorIndex);
            final int numArguments = code.image.getSpecialSelectorNumArgs(selectorIndex);
            return new SendSpecialSelectorNode(code, index, 1, specialSelector, numArguments);
        }
    }

    public static final class SendSelfSelector extends AbstractSendNode {
        public SendSelfSelector(final CompiledCodeObject code, final int index, final int numBytecodes, final Object selector, final int numArgs) {
            super(code, index, numBytecodes, selector, numArgs);
        }
    }

    public static final class SingleExtendedSendNode extends AbstractSendNode {
        public SingleExtendedSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int param) {
            super(code, index, numBytecodes, code.getLiteral(param & 31), param >> 5);
        }
    }

    public static final class SingleExtendedSuperNode extends AbstractSendNode {

        public SingleExtendedSuperNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int rawByte) {
            this(code, index, numBytecodes, rawByte & 31, rawByte >> 5);
        }

        public SingleExtendedSuperNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int literalIndex, final int numArgs) {
            super(code, index, numBytecodes, code.getLiteral(literalIndex), numArgs, new LookupSuperClassNode());
        }

        @Override
        public String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "sendSuper: " + selector.asStringUnsafe();
        }
    }
}
