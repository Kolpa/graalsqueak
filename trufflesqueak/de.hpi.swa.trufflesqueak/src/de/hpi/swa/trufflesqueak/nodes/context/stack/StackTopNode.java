package de.hpi.swa.trufflesqueak.nodes.context.stack;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;

public abstract class StackTopNode extends AbstractStackNode {

    public static StackTopNode create(CompiledCodeObject code) {
        return StackTopNodeGen.create(code);
    }

    protected StackTopNode(CompiledCodeObject code) {
        super(code);
    }

    @Specialization(guards = {"isVirtualized(frame)"})
    protected Object doTopVirtualized(VirtualFrame frame) {
        CompilerDirectives.ensureVirtualizedHere(frame);
        return readNode.execute(frame, (int) frameStackPointer(frame));
    }

    @Specialization(guards = {"!isVirtualized(frame)"})
    protected Object doTop(VirtualFrame frame) {
        return getContext(frame).top();
    }
}