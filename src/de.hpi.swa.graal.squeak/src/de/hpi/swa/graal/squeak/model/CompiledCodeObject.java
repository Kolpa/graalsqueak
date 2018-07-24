package de.hpi.swa.graal.squeak.model;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.utilities.CyclicAssumption;

import de.hpi.swa.graal.squeak.SqueakLanguage;
import de.hpi.swa.graal.squeak.image.SqueakImageChunk;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.instrumentation.CompiledCodeObjectPrinter;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.CONTEXT;
import de.hpi.swa.graal.squeak.nodes.EnterCodeNode;
import de.hpi.swa.graal.squeak.util.BitSplitter;

public abstract class CompiledCodeObject extends AbstractSqueakObject {
    public enum SLOT_IDENTIFIER {
        THIS_CONTEXT_OR_MARKER,
        INSTRUCTION_POINTER,
        STACK_POINTER,
    }

    protected static final int BYTES_PER_WORD = 4;

    // frame info
    public static final FrameDescriptor frameDescriptorTemplate;
    public static final FrameSlot thisContextOrMarkerSlot;
    public static final FrameSlot instructionPointerSlot;
    public static final FrameSlot stackPointerSlot;
    @CompilationFinal private FrameDescriptor frameDescriptor;
    @CompilationFinal(dimensions = 1) public FrameSlot[] stackSlots;
    // header info and data
    @CompilationFinal(dimensions = 1) protected Object[] literals;
    @CompilationFinal(dimensions = 1) protected byte[] bytes;
    @CompilationFinal private int numArgs;
    @CompilationFinal protected int numLiterals;
    @CompilationFinal private boolean isOptimized;
    @CompilationFinal private boolean hasPrimitive;
    @CompilationFinal protected boolean needsLargeFrame = false;
    @CompilationFinal private int numTemps;
    @CompilationFinal private long accessModifier;
    @CompilationFinal private boolean altInstructionSet;

    private final int numCopiedValues; // for block closures

    private static final boolean ALWAYS_NON_VIRTUALIZED = false;
    private final Assumption canBeVirtualized = Truffle.getRuntime().createAssumption("CompiledCodeObject: does not need a materialized context");

    private Source source;

    @CompilationFinal private RootCallTarget callTarget;
    private final CyclicAssumption callTargetStable = new CyclicAssumption("CompiledCodeObject assumption");

    static {
        frameDescriptorTemplate = new FrameDescriptor();
        thisContextOrMarkerSlot = frameDescriptorTemplate.addFrameSlot(SLOT_IDENTIFIER.THIS_CONTEXT_OR_MARKER, FrameSlotKind.Object);
        instructionPointerSlot = frameDescriptorTemplate.addFrameSlot(SLOT_IDENTIFIER.INSTRUCTION_POINTER, FrameSlotKind.Int);
        stackPointerSlot = frameDescriptorTemplate.addFrameSlot(SLOT_IDENTIFIER.STACK_POINTER, FrameSlotKind.Int);
    }

    protected CompiledCodeObject(final SqueakImageContext img, final ClassObject klass, final int numCopiedValues) {
        super(img, klass);
        if (ALWAYS_NON_VIRTUALIZED) {
            invalidateCanBeVirtualizedAssumption();
        }
        this.numCopiedValues = numCopiedValues;
    }

    protected CompiledCodeObject(final SqueakImageContext img, final int numCopiedValues) {
        this(img, img.compiledMethodClass, numCopiedValues);
    }

    protected CompiledCodeObject(final CompiledCodeObject original) {
        this(original.image, original.getSqClass(), original.numCopiedValues);
        setLiteralsAndBytes(original.literals.clone(), original.bytes.clone());
    }

    private void setLiteralsAndBytes(final Object[] literals, final byte[] bytes) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        this.literals = literals;
        decodeHeader();
        this.bytes = bytes;
        createNewCallTarget();
    }

    public final Source getSource() {
        if (source == null) {
            /*
             * sourceSection requested when logging transferToInterpreters. Therefore, do not
             * trigger another TTI here which otherwise would cause endless recursion in Truffle
             * debug code.
             */
            source = Source.newBuilder(CompiledCodeObjectPrinter.getString(this)).mimeType(SqueakLanguage.MIME_TYPE).name(toString()).build();
        }
        return source;
    }

    public final int sqContextSize() {
        return needsLargeFrame ? CONTEXT.LARGE_FRAMESIZE : CONTEXT.SMALL_FRAMESIZE;
    }

    @SuppressWarnings("deprecation")
    @TruffleBoundary
    protected final void prepareFrameDescriptor() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        frameDescriptor = frameDescriptorTemplate.shallowCopy();
        /**
         * Arguments and copied values are also pushed onto the stack in {@link EnterCodeNode},
         * therefore there must be enough slots for all these values as well as the Squeak stack.
         */
        final int numFrameSlots = getNumArgsAndCopied() + sqContextSize();
        stackSlots = new FrameSlot[numFrameSlots];
        for (int i = 0; i < numFrameSlots; i++) {
            stackSlots[i] = frameDescriptor.addFrameSlot(i, FrameSlotKind.Illegal);
        }
    }

    public RootCallTarget getSplitCallTarget() {
        return getCallTarget();
    }

    public final RootCallTarget getCallTarget() {
        if (callTarget == null) {
            createNewCallTarget();
        }
        return callTarget;
    }

    private void createNewCallTarget() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        callTarget = Truffle.getRuntime().createCallTarget(EnterCodeNode.create(image.getLanguage(), this));
    }

    public final Assumption getCallTargetStable() {
        return callTargetStable.getAssumption();
    }

    public final FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public final int getNumArgs() {
        return numArgs;
    }

    public final int getNumArgsAndCopied() {
        return numArgs + numCopiedValues;
    }

    public final int getNumTemps() {
        return numTemps;
    }

    public final int getNumLiterals() {
        return numLiterals;
    }

    public final FrameSlot getStackSlot(final int i) {
        if (i >= stackSlots.length) { // This is fine, ignore for decoder
            return null;
        }
        return stackSlots[i];
    }

    public final long getNumStackSlots() {
        return stackSlots.length;
    }

    public final void fillin(final SqueakImageChunk chunk) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        final int[] data = chunk.data();
        final int header = data[0] >> 1; // header is a tagged small integer
        final int literalsize = header & 0x7fff;
        final Object[] ptrs = chunk.getPointers(literalsize + 1);
        assert literals == null;
        literals = ptrs;
        decodeHeader();
        assert bytes == null;
        bytes = chunk.getBytes(ptrs.length);
    }

    protected final void decodeHeader() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        final int hdr = getHeader();
        final int[] splitHeader = BitSplitter.splitter(hdr, new int[]{15, 1, 1, 1, 6, 4, 2, 1});
        numLiterals = splitHeader[0];
        isOptimized = splitHeader[1] == 1;
        hasPrimitive = splitHeader[2] == 1;
        needsLargeFrame = splitHeader[3] == 1;
        numTemps = splitHeader[4];
        numArgs = splitHeader[5];
        accessModifier = splitHeader[6];
        altInstructionSet = splitHeader[7] == 1;
        prepareFrameDescriptor();
    }

    public final int getHeader() {
        return (int) (long) literals[0];
    }

    public final void become(final CompiledCodeObject other) {
        becomeOtherClass(other);
        CompilerDirectives.transferToInterpreterAndInvalidate();
        final Object[] literals2 = other.literals;
        final byte[] bytes2 = other.bytes;
        other.setLiteralsAndBytes(literals, bytes);
        this.setLiteralsAndBytes(literals2, bytes2);
        other.callTargetStable.invalidate();
        callTargetStable.invalidate();
    }

    public final int getBytecodeOffset() {
        return (1 + numLiterals) * BYTES_PER_WORD; // header plus numLiterals
    }

    public final void atput0(final long longIndex, final Object obj) {
        final int index = (int) longIndex;
        assert index >= 0;
        CompilerDirectives.transferToInterpreterAndInvalidate();
        if (index < getBytecodeOffset()) {
            assert index % BYTES_PER_WORD == 0;
            setLiteral(index / BYTES_PER_WORD, obj);
        } else {
            final int realIndex = index - getBytecodeOffset();
            assert realIndex < bytes.length;
            if (obj instanceof Integer) {
                bytes[realIndex] = (byte) (int) obj;
            } else if (obj instanceof Long) {
                bytes[realIndex] = (byte) (long) obj;
            } else {
                bytes[realIndex] = (byte) obj;
            }
        }
    }

    public final Object getLiteral(final long longIndex) {
        final int index = (int) longIndex;
        final int literalIndex = 1 + index; // skip header
        if (literalIndex < literals.length) {
            return literals[literalIndex];
        } else {
            return literals[0]; // for decoder
        }
    }

    public final void setLiteral(final long longIndex, final Object obj) {
        final int index = (int) longIndex;
        CompilerDirectives.transferToInterpreterAndInvalidate();
        if (index == 0) {
            assert obj instanceof Long;
            final int oldNumLiterals = numLiterals;
            literals[0] = obj;
            decodeHeader();
            assert numLiterals == oldNumLiterals;
        } else {
            literals[index] = obj;
        }
    }

    public final boolean hasPrimitive() {
        return hasPrimitive;
    }

    public final int primitiveIndex() {
        if (hasPrimitive() && bytes.length >= 3) {
            return (Byte.toUnsignedInt(bytes[2]) << 8) + Byte.toUnsignedInt(bytes[1]);
        } else {
            return 0;
        }
    }

    public final Object[] getLiterals() {
        return literals;
    }

    public final byte[] getBytes() {
        return bytes;
    }

    public final boolean canBeVirtualized() {
        return canBeVirtualized.isValid();
    }

    public final Assumption getCanBeVirtualizedAssumption() {
        return canBeVirtualized;
    }

    public final void invalidateCanBeVirtualizedAssumption() {
        canBeVirtualized.invalidate();
    }

    public final boolean isUnwindMarked() {
        return hasPrimitive() && primitiveIndex() == 198;
    }

    public final boolean isExceptionHandlerMarked() {
        return hasPrimitive() && primitiveIndex() == 199;
    }

    public static final long makeHeader(final int numArgs, final int numTemps, final int numLiterals, final boolean hasPrimitive, final boolean needsLargeFrame) {
        return (numArgs & 0x0F) << 24 | (numTemps & 0x3F) << 18 | numLiterals & 0x7FFF | (needsLargeFrame ? 0x20000 : 0) | (hasPrimitive ? 0x10000 : 0);
    }
}
