package de.hpi.swa.graal.squeak.nodes.context.stack;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.model.ContextObject;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameStackWriteNode;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.util.FrameAccess;

/**
 * This node pushes values onto the stack and is intended to be used in
 * {@link AbstractPrimitiveNode}. Unlike {@link StackPushNode}, it does not need a
 * {@link CompiledCodeObject} on creation, but uses the one attached to the provided
 * {@link VirtualFrame}. This is necessary to ensure correct {@link FrameDescriptor} ownership
 * during eager primitive calls.
 */
@ImportStatic(FrameAccess.class)
public abstract class StackPushForPrimitivesNode extends Node {

    public static StackPushForPrimitivesNode create() {
        return StackPushForPrimitivesNodeGen.create();
    }

    public abstract void executeWrite(VirtualFrame frame, Object value);

    @Specialization(guards = {"isVirtualized(frame, codeObject)"}, limit = "1")
    protected static final void doWriteVirtualized(final VirtualFrame frame, final Object value,
                    @Cached("getMethod(frame)") final CompiledMethodObject codeObject,
                    @Cached("create(codeObject)") final FrameStackWriteNode writeNode) {
        assert value != null;
        final int currentStackPointer = FrameUtil.getIntSafe(frame, codeObject.stackPointerSlot);
        frame.setInt(codeObject.stackPointerSlot, currentStackPointer + 1);
        writeNode.execute(frame, currentStackPointer, value);
    }

    @Specialization(guards = {"!isVirtualized(frame, codeObject)"}, limit = "1")
    protected static final void doWrite(final VirtualFrame frame, final Object value,
                    @Cached("getMethod(frame)") final CompiledMethodObject codeObject) {
        assert value != null;
        getContext(frame, codeObject).push(value);
    }

    @Fallback
    protected static final void doFail(@SuppressWarnings("unused") final Object value) {
        throw new SqueakException("Should never happen");
    }

    protected static final boolean isVirtualized(final VirtualFrame frame, final CompiledCodeObject codeObject) {
        return !(FrameUtil.getObjectSafe(frame, codeObject.thisContextOrMarkerSlot) instanceof ContextObject);
    }

    protected static final ContextObject getContext(final VirtualFrame frame, final CompiledCodeObject codeObject) {
        return (ContextObject) FrameUtil.getObjectSafe(frame, codeObject.thisContextOrMarkerSlot);
    }
}
