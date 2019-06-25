package de.hpi.swa.graal.squeak.nodes.context.frame;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.CONTEXT;
import de.hpi.swa.graal.squeak.nodes.AbstractNodeWithCode;
import de.hpi.swa.graal.squeak.util.FrameAccess;

@ImportStatic(CONTEXT.class)
public abstract class FrameStackReadAndClearNode extends AbstractNodeWithCode {

    protected FrameStackReadAndClearNode(final CompiledCodeObject code) {
        super(code);
    }

    public static FrameStackReadAndClearNode create(final CompiledCodeObject code) {
        return FrameStackReadAndClearNodeGen.create(code);
    }

    public final Object executePop(final VirtualFrame frame) {
        final int newSP = FrameAccess.getStackPointer(frame, code) - 1;
        assert newSP >= 0 : "Bad stack pointer";
        FrameAccess.setStackPointer(frame, code, newSP);
        return execute(frame, newSP);
    }

    @ExplodeLoop
    public final Object[] executePopN(final VirtualFrame frame, final int numPop) {
        final int currentSP = FrameAccess.getStackPointer(frame, code);
        assert currentSP - numPop >= 0;
        final Object[] result = new Object[numPop];
        for (int i = 1; i <= numPop; i++) {
            result[numPop - i] = execute(frame, currentSP - i);
            assert result[numPop - i] != null;
        }
        FrameAccess.setStackPointer(frame, code, currentSP - numPop);
        return result;
    }

    protected abstract Object execute(Frame frame, int stackIndex);

    @SuppressWarnings("unused")
    @Specialization(guards = {"index == cachedIndex"}, limit = "MAX_STACK_SIZE")
    protected static final Object doClear(final Frame frame, final int index,
                    @Cached("index") final int cachedIndex,
                    @Cached("code.getStackSlot(index)") final FrameSlot slot,
                    @Cached("createReadNode(slot, index)") final AbstractFrameSlotReadNode clearNode) {
        return clearNode.executeRead(frame);
    }

    protected final AbstractFrameSlotReadNode createReadNode(final FrameSlot frameSlot, final int index) {
        // Only clear stack values, not receiver, arguments, or temporary variables.
        if (index >= code.getNumArgsAndCopied() + code.getNumTemps()) {
            return FrameSlotReadAndClearNode.create(frameSlot);
        } else {
            return FrameSlotReadNode.create(frameSlot);
        }
    }
}
