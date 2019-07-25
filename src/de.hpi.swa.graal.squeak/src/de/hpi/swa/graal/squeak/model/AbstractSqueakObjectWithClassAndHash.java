package de.hpi.swa.graal.squeak.model;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.interop.LookupMethodByStringNode;
import de.hpi.swa.graal.squeak.nodes.DispatchUneagerlyNode;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

@ExportLibrary(InteropLibrary.class)
public abstract class AbstractSqueakObjectWithClassAndHash extends AbstractSqueakObjectWithHash {
    private ClassObject squeakClass;
    private final Assumption squeakClassStable = Truffle.getRuntime().createAssumption("squeakClass stability");

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
        if (squeakClass != otherSqClass) {
            other.setSqueakClass(squeakClass);
            setSqueakClass(otherSqClass);
        }
    }

    @Override
    public final void fillInSqueakClass(final ClassObject newClass) {
        squeakClass = newClass;
    }

    @Override
    public final ClassObject getSqueakClass() {
        return squeakClass;
    }

    public final Assumption getSqueakClassStableAssumption() {
        return squeakClassStable;
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

    public final void setSqueakClass(final ClassObject newClass) {
        squeakClassStable.invalidate("class has changed");
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
            return DispatchUneagerlyNode.getUncached().executeDispatch((CompiledMethodObject) methodObject, ArrayUtils.copyWithFirst(arguments, this), NilObject.SINGLETON);
        } else {
            throw SqueakExceptions.SqueakException.create("CompiledMethodObject expected, got: " + methodObject);
        }
    }
}
