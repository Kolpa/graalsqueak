package de.hpi.swa.graal.squeak.nodes.accessing;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.model.*;
import de.hpi.swa.graal.squeak.nodes.AbstractNode;

public abstract class SqueakObjectInstSizeNode extends AbstractNode {

    public static SqueakObjectInstSizeNode create() {
        return SqueakObjectInstSizeNodeGen.create();
    }

    public abstract int execute(AbstractSqueakObject obj);

    @Specialization
    protected static final int doArray(final ArrayObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doPointers(final AbstractPointersObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doClass(final ClassObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doContext(final ContextObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doClosure(final BlockClosureObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doCode(final CompiledCodeObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doEmpty(final EmptyObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doNative(final NativeObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doFloat(final FloatObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doLarge(final LargeIntegerObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doNil(final NilObject obj) {
        return obj.instsize();
    }

    @Specialization
    protected static final int doNativeImmutableBytesObject(final NativeImmutableBytesObject obj) {
        return obj.instsize();
    }

    @Fallback
    protected static final int doFail(final AbstractSqueakObject object) {
        throw SqueakException.create("Unexpected value:", object);
    }
}
