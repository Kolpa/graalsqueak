package de.hpi.swa.graal.squeak.model;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.utilities.CyclicAssumption;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.image.reading.SqueakImageChunk;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.BLOCK_CLOSURE;
import de.hpi.swa.graal.squeak.nodes.EnterCodeNode;
import de.hpi.swa.graal.squeak.nodes.accessing.ContextObjectNodes;

public final class BlockClosureObject extends AbstractSqueakObject {
    @CompilationFinal private Object receiver;
    @CompilationFinal private Object outerContextOrMarker;
    @CompilationFinal private CompiledBlockObject block;
    @CompilationFinal private long pc = -1;
    @CompilationFinal private long numArgs = -1;
    @CompilationFinal(dimensions = 1) private Object[] copied;
    @CompilationFinal private RootCallTarget callTarget;

    private final CyclicAssumption callTargetStable = new CyclicAssumption("BlockClosureObject assumption");

    public BlockClosureObject(final SqueakImageContext image, final long hash) {
        super(image, hash, image.blockClosureClass);
        this.copied = new Object[0]; // ensure copied is set
    }

    public BlockClosureObject(final SqueakImageContext image) {
        super(image, image.blockClosureClass);
        this.copied = new Object[0]; // ensure copied is set
    }

    public BlockClosureObject(final CompiledBlockObject compiledBlock, final RootCallTarget callTarget, final Object receiver, final Object[] copied, final Object outerContext) {
        super(compiledBlock.image, compiledBlock.image.blockClosureClass);
        this.block = compiledBlock;
        this.callTarget = callTarget;
        this.outerContextOrMarker = outerContext;
        this.receiver = receiver;
        this.copied = copied;
        this.pc = block.getInitialPC();
        this.numArgs = block.getNumArgs();
    }

    private BlockClosureObject(final BlockClosureObject original) {
        super(original.image, original.image.blockClosureClass);
        this.block = original.block;
        this.callTarget = original.callTarget;
        this.outerContextOrMarker = original.outerContextOrMarker;
        this.receiver = original.receiver;
        this.copied = original.copied;
        this.pc = original.pc;
        this.numArgs = original.numArgs;
    }

    public void fillin(final SqueakImageChunk chunk) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        final Object[] pointers = chunk.getPointers();
        assert pointers.length >= BLOCK_CLOSURE.FIRST_COPIED_VALUE;
        outerContextOrMarker = pointers[BLOCK_CLOSURE.OUTER_CONTEXT];
        assert outerContextOrMarker instanceof ContextObject;
        pc = (long) pointers[BLOCK_CLOSURE.START_PC];
        numArgs = (long) pointers[BLOCK_CLOSURE.ARGUMENT_COUNT];
        copied = new Object[pointers.length - BLOCK_CLOSURE.FIRST_COPIED_VALUE];
        for (int i = 0; i < copied.length; i++) {
            copied[i] = pointers[BLOCK_CLOSURE.FIRST_COPIED_VALUE + i];
        }
    }

    public long getStartPC() {
        if (pc == -1) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            pc = block.getInitialPC();
        }
        return pc;
    }

    public long getNumArgs() {
        if (numArgs == -1) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            numArgs = block.getNumArgs();
        }
        return numArgs;
    }

    public Object[] getCopied() {
        return copied;
    }

    public Object at0(final long longIndex) {
        final int index = (int) longIndex;
        switch (index) {
            case BLOCK_CLOSURE.OUTER_CONTEXT:
                return outerContextOrMarker;
            case BLOCK_CLOSURE.START_PC:
                return getStartPC();
            case BLOCK_CLOSURE.ARGUMENT_COUNT:
                return getNumArgs();
            default:
                return copied[index - BLOCK_CLOSURE.FIRST_COPIED_VALUE];
        }
    }

    public void atput0(final long longIndex, final Object obj) {
        final int index = (int) longIndex;
        switch (index) {
            case BLOCK_CLOSURE.OUTER_CONTEXT:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                assert obj instanceof ContextObject;
                outerContextOrMarker = obj;
                break;
            case BLOCK_CLOSURE.START_PC:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                pc = (int) (long) obj;
                break;
            case BLOCK_CLOSURE.ARGUMENT_COUNT:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                numArgs = (int) (long) obj;
                break;
            default:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                copied[index - BLOCK_CLOSURE.FIRST_COPIED_VALUE] = obj;
                break;
        }
    }

    public void become(final BlockClosureObject other) {
        becomeOtherClass(other);
        final Object[] otherCopied = other.copied;
        other.setCopied(this.copied);
        this.setCopied(otherCopied);
    }

    public void setCopied(final Object[] copied) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        this.copied = copied;
    }

    public int size() {
        return copied.length + instsize();
    }

    public static int instsize() {
        return BLOCK_CLOSURE.FIRST_COPIED_VALUE;
    }

    public Object[] getStack() {
        return copied;
    }

    public Object getReceiver() {
        if (receiver == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            receiver = ((ContextObject) outerContextOrMarker).getReceiver();
        }
        return receiver;
    }

    public void setReceiver(final Object receiver) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        this.receiver = receiver;
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    public Assumption getCallTargetStable() {
        return callTargetStable.getAssumption();
    }

    public CompiledBlockObject getCompiledBlock() {
        if (block == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            assert pc >= 0;
            final CompiledCodeObject code = ((ContextObject) outerContextOrMarker).getMethod();
            final CompiledMethodObject method;
            if (code instanceof CompiledMethodObject) {
                method = (CompiledMethodObject) code;
            } else {
                method = ((CompiledBlockObject) code).getMethod();
            }
            final int offset = (int) pc - method.getInitialPC();
            final int j = code.getBytes()[offset - 2];
            final int k = code.getBytes()[offset - 1];
            final int blockSize = (j << 8) | (k & 0xff);
            block = CompiledBlockObject.create(code, method, ((Long) numArgs).intValue(), copied.length, offset, blockSize);
            callTarget = Truffle.getRuntime().createCallTarget(EnterCodeNode.create(block.image.getLanguage(), block));
        }
        return block;
    }

    public boolean hasHomeContext() {
        return outerContextOrMarker != null;
    }

    @TruffleBoundary
    public ContextObject getHomeContext() {
        if (outerContextOrMarker instanceof FrameMarker) {
            outerContextOrMarker = ContextObjectNodes.getMaterializedContextForMarker((FrameMarker) outerContextOrMarker);
        }
        final ContextObject context = (ContextObject) outerContextOrMarker;
        if (context.isTerminated()) {
            throw new SqueakException("BlockCannotReturnError");
        }
        // recursively unpack closures until home context is reached
        final BlockClosureObject closure = context.getClosure();
        if (closure != null) {
            return closure.getHomeContext();
        } else {
            return context;
        }
    }

    public Object getOuterContext() {
        return outerContextOrMarker;
    }

    public void setOuterContext(final Object outerContext) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        assert outerContext instanceof ContextObject || outerContext instanceof FrameMarker;
        this.outerContextOrMarker = outerContext;
    }

    public AbstractSqueakObject shallowCopy() {
        return new BlockClosureObject(this);
    }

    public Object[] getTraceableObjects() {
        final Object[] result = new Object[copied.length + 2];
        for (int i = 0; i < copied.length; i++) {
            result[i] = copied[i];
        }
        result[copied.length] = receiver;
        result[copied.length + 1] = outerContextOrMarker;
        return result;
    }
}
