package de.hpi.swa.graal.squeak.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.graal.squeak.exceptions.Returns.LocalReturn;
import de.hpi.swa.graal.squeak.exceptions.Returns.NonLocalReturn;
import de.hpi.swa.graal.squeak.model.BaseSqueakObject;
import de.hpi.swa.graal.squeak.model.BlockClosureObject;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.model.ContextObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.SPECIAL_OBJECT_INDEX;
import de.hpi.swa.graal.squeak.nodes.bytecodes.SendBytecodes.SendSelectorNode;
import de.hpi.swa.graal.squeak.nodes.context.TemporaryReadNode;
import de.hpi.swa.graal.squeak.nodes.context.TemporaryWriteNode;
import de.hpi.swa.graal.squeak.nodes.context.stack.StackPushNode;

public abstract class AboutToReturnNode extends AbstractNodeWithCode {
    @Child protected BlockActivationNode dispatch = BlockActivationNodeGen.create();
    @Child private SendSelectorNode sendAboutToReturnNode;
    @Child private StackPushNode pushNode;
    @Child private SqueakNode blockArgumentNode;
    @Child private SqueakNode completeTempReadNode;
    @Child private TemporaryWriteNode completeTempWriteNode;

    public static AboutToReturnNode create(final CompiledMethodObject code) {
        return AboutToReturnNodeGen.create(code);
    }

    protected AboutToReturnNode(final CompiledMethodObject method) {
        super(method);
        sendAboutToReturnNode = new SendSelectorNode(method, -1, -1, aboutToReturnSelector(method), 2);
        pushNode = StackPushNode.create(method);
        blockArgumentNode = TemporaryReadNode.create(method, 0);
        completeTempReadNode = TemporaryReadNode.create(method, 1);
        completeTempWriteNode = TemporaryWriteNode.create(method, 1);
    }

    private static BaseSqueakObject aboutToReturnSelector(final CompiledMethodObject method) {
        return (BaseSqueakObject) method.image.specialObjectsArray.at0(SPECIAL_OBJECT_INDEX.SelectorAboutToReturn);
    }

    public abstract void executeAboutToReturn(VirtualFrame frame, NonLocalReturn nlr);

    /*
     * Virtualized version of Context>>aboutToReturn:through:, more specifically
     * Context>>resume:through:. This is only called if code.isUnwindMarked(), so there is no need
     * to unwind contexts here as this is already happening when NonLocalReturns are handled. Note
     * that this however does not check if the current context isDead nor does it terminate contexts
     * (this may be a problem).
     */
    @Specialization(guards = {"isVirtualized(frame)"})
    protected void doAboutToReturnVirtualized(final VirtualFrame frame, @SuppressWarnings("unused") final NonLocalReturn nlr) {
        CompilerDirectives.ensureVirtualizedHere(frame);
        if (completeTempReadNode.executeRead(frame) == code.image.nil) {
            completeTempWriteNode.executeWrite(frame, code.image.sqTrue);
            final BlockClosureObject block = (BlockClosureObject) blockArgumentNode.executeRead(frame);
            try {
                dispatch.executeBlock(block, block.getFrameArguments(frame));
            } catch (LocalReturn blockLR) { // ignore
            } catch (NonLocalReturn blockNLR) {
                if (!blockNLR.hasArrivedAtTargetContext()) {
                    throw blockNLR;
                }
            }
        }
    }

    @Specialization(guards = {"!isVirtualized(frame)"})
    protected void doAboutToReturn(final VirtualFrame frame, final NonLocalReturn nlr) {
        final ContextObject context = getContext(frame);
        pushNode.executeWrite(frame, nlr.getTargetContext());
        pushNode.executeWrite(frame, nlr.getReturnValue());
        pushNode.executeWrite(frame, context);
        sendAboutToReturnNode.executeSend(frame);
    }
}