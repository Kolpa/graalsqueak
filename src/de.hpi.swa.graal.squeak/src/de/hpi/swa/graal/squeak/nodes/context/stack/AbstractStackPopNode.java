package de.hpi.swa.graal.squeak.nodes.context.stack;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.model.ContextObject;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameStackClearNode;
import de.hpi.swa.graal.squeak.util.FrameAccess;

public abstract class AbstractStackPopNode extends AbstractStackNode {
    @Child private FrameStackClearNode clearNode;

    public AbstractStackPopNode(final CompiledCodeObject code) {
        super(code);
    }

    protected final Object atStackAndClear(final VirtualFrame frame, final int index) {
        final Object value = getReadNode().execute(frame, index - 1);
        final CompiledMethodObject method = FrameAccess.getMethod(frame);
        if (index >= 1 + method.getNumArgs() + method.getNumTemps()) {
            // Only clear stack values, not receiver, arguments, or temporary variables.
            getClearNode().execute(frame, index - 1);
        }
        return value;
    }

    protected final Object atStackAndClear(final ContextObject context, final long argumentIndex) {
        final Object value = context.atStack(argumentIndex);
        final CompiledMethodObject method = context.getMethod();
        if (argumentIndex >= 1 + method.getNumArgs() + method.getNumTemps()) {
            // Only nil out stack values, not receiver, arguments, or temporary variables.
            context.atStackPut(argumentIndex, code.image.nil);
        }
        return value;
    }

    private FrameStackClearNode getClearNode() {
        if (clearNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            clearNode = insert(FrameStackClearNode.create(code));
        }
        return clearNode;
    }
}
