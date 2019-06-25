package de.hpi.swa.graal.squeak.model;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.interop.LookupMethodByStringNode;
import de.hpi.swa.graal.squeak.nodes.DispatchSendNode;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

@ExportLibrary(InteropLibrary.class)
public abstract class AbstractSqueakObjectWithClassAndHash extends AbstractSqueakObjectWithHash {
    private ClassObject squeakClass;

    // For special/well-known objects only.
    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image) {
        super(image);
        squeakClass = null;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final ClassObject klass) {
        super(image);
        squeakClass = klass;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final int hash) {
        super(image, hash);
        // TODO: Generate new hash if `0`. This might have something to do with compact classes?
        squeakClass = null;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final long hash, final ClassObject klass) {
        super(image, hash);
        squeakClass = klass;
    }

    public final void becomeOtherClass(final AbstractSqueakObjectWithClassAndHash other) {
        final ClassObject otherSqClass = other.squeakClass;
        other.setSqueakClass(squeakClass);
        setSqueakClass(otherSqClass);
    }

    @Override
    public final ClassObject getSqueakClass() {
        return squeakClass;
    }

    public final String getSqueakClassName() {
        if (isClass()) {
            return getClassName() + " class";
        } else {
            return getSqueakClass().getClassName();
        }
    }

    public final boolean isClass() {
        assert !(this instanceof ClassObject) || getSqueakClass().isMetaClass() || getSqueakClass().getSqueakClass().isMetaClass();
        CompilerAsserts.neverPartOfCompilation();
        return this instanceof ClassObject;
    }

    public final boolean isMetaClass() {
        return this == image.metaClass;
    }

    @Override
    public final void setSqueakClass(final ClassObject newClass) {
        squeakClass = newClass;
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "a " + getSqueakClassName();
    }

    public final Object send(final String selector, final Object... arguments) {
        CompilerAsserts.neverPartOfCompilation("For testing or instrumentation only.");
        final Object methodObject = LookupMethodByStringNode.getUncached().executeLookup(getSqueakClass(), selector);
        if (methodObject instanceof CompiledMethodObject) {
            final CompiledMethodObject method = (CompiledMethodObject) methodObject;
            final MaterializedFrame frame = Truffle.getRuntime().createMaterializedFrame(ArrayUtils.EMPTY_ARRAY, method.getFrameDescriptor());
            return DispatchSendNode.create(image).executeSend(frame, method.getCompiledInSelector(), method, getSqueakClass(), ArrayUtils.copyWithFirst(arguments, this), NilObject.SINGLETON);
        } else {
            throw SqueakExceptions.SqueakException.create("CompiledMethodObject expected, got: " + methodObject);
        }
    }
}
