package de.hpi.swa.graal.squeak.model;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.image.reading.SqueakImageChunk;
import de.hpi.swa.graal.squeak.interop.InteropArray;
import de.hpi.swa.graal.squeak.interop.LookupMethodByStringNode;
import de.hpi.swa.graal.squeak.interop.WrapToSqueakNode;
import de.hpi.swa.graal.squeak.nodes.DispatchSendNode;
import de.hpi.swa.graal.squeak.nodes.DispatchUneagerlyNode;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectLibrary;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

@ExportLibrary(SqueakObjectLibrary.class)
@ExportLibrary(InteropLibrary.class)
public abstract class AbstractSqueakObjectWithClassAndHash extends AbstractSqueakObject {
    public static final int IDENTITY_HASH_MASK = 0x400000 - 1;
    private static final long HASH_UNINITIALIZED = -1;
    private static final int PINNED_BIT_SHIFT = 30;
    private static final int PINNED_BIT_MASK = 1 << PINNED_BIT_SHIFT;

    public final SqueakImageContext image;
    private long squeakHash;
    private ClassObject squeakClass;

    // For special/well-known objects only.
    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image) {
        this.image = image;
        squeakHash = HASH_UNINITIALIZED;
        squeakClass = null;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final ClassObject klass) {
        this.image = image;
        squeakHash = HASH_UNINITIALIZED;
        squeakClass = klass;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final int hash) {
        this.image = image;
        // TODO: Generate new hash if `0`. This might have something to do with compact classes?
        squeakHash = hash != 0 ? hash : HASH_UNINITIALIZED;
        squeakClass = null;
    }

    protected AbstractSqueakObjectWithClassAndHash(final SqueakImageContext image, final long hash, final ClassObject klass) {
        this.image = image;
        squeakHash = hash;
        squeakClass = klass;
    }

    public abstract void fillin(SqueakImageChunk chunk);

    public final void becomeOtherClass(final AbstractSqueakObjectWithClassAndHash other) {
        final ClassObject otherSqClass = other.squeakClass;
        other.setSqueakClass(squeakClass);
        setSqueakClass(otherSqClass);
    }

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

    public final long getSqueakHash() {
        if (needsSqueakHash()) {
            /** Lazily initialize squeakHash and derive value from hashCode. */
            squeakHash = hashCode() & IDENTITY_HASH_MASK;
        }
        return squeakHash;
    }

    public final boolean needsSqueakHash() {
        return squeakHash == HASH_UNINITIALIZED;
    }

    public final boolean needsSqueakClass() {
        return squeakClass == null;
    }

    public final boolean isClass() {
        assert !(this instanceof ClassObject) || getSqueakClass().isMetaClass() || getSqueakClass().getSqueakClass().isMetaClass();
        CompilerAsserts.neverPartOfCompilation();
        return this instanceof ClassObject;
    }

    public final boolean isMetaClass() {
        return this == image.metaClass;
    }

    public final boolean isPinned() {
        return (squeakHash >> PINNED_BIT_SHIFT & 1) == 1;
    }

    public String getClassName() {
        return "???NotAClass";
    }

    public final void setPinned() {
        setSqueakHash(getSqueakHash() | PINNED_BIT_MASK);
    }

    public final void setSqueakClass(final ClassObject newClass) {
        squeakClass = newClass;
    }

    public final void setSqueakHash(final long newHash) {
        squeakHash = newHash;
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return "a " + getSqueakClassName();
    }

    public final void unsetPinned() {
        setSqueakHash(getSqueakHash() & ~PINNED_BIT_MASK);
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

    /*
     * INTEROPERABILITY
     */

    @SuppressWarnings("static-method")
    @ExportMessage
    protected final boolean hasArrayElements() {
        return squeakClass.isVariable();
    }

    @ExportMessage
    protected final long getArraySize(@CachedLibrary("this") final SqueakObjectLibrary objectLibrary) {
        return objectLibrary.size(this);
    }

    @SuppressWarnings("static-method")
    @ExportMessage(name = "isArrayElementReadable")
    @ExportMessage(name = "isArrayElementModifiable")
    @ExportMessage(name = "isArrayElementInsertable")
    protected final boolean isArrayElementReadable(final long index, @CachedLibrary("this") final SqueakObjectLibrary objectLibrary) {
        return 0 <= index && index < objectLibrary.size(this);
    }

    @ExportMessage
    protected final Object readArrayElement(final long index, @CachedLibrary("this") final SqueakObjectLibrary objectLibrary) {
        return objectLibrary.at0(this, (int) index);
    }

    @ExportMessage
    protected final void writeArrayElement(final long index, final Object value,
                    @Shared("wrapNode") @Cached final WrapToSqueakNode wrapNode,
                    @CachedLibrary("this") final SqueakObjectLibrary objectLibrary) throws InvalidArrayIndexException {
        try {
            objectLibrary.atput0(this, (int) index, wrapNode.executeWrap(value));
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw InvalidArrayIndexException.create(index);
        }
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    protected final boolean hasMembers() {
        return true;
    }

    @ExportMessage
    protected final Object getMembers(@SuppressWarnings("unused") final boolean includeInternal) {
        return new InteropArray(getSqueakClass().listMethods());
    }

    @ExportMessage(name = "isMemberReadable")
    @ExportMessage(name = "isMemberModifiable")
    @ExportMessage(name = "isMemberInvocable")
    protected final boolean isMemberReadable(final String key,
                    @Shared("lookupNode") @Cached final LookupMethodByStringNode lookupNode) {
        return lookupNode.executeLookup(getSqueakClass(), toSelector(key)) != null;
    }

    @ExportMessage
    protected final Object readMember(final String key,
                    @Shared("lookupNode") @Cached final LookupMethodByStringNode lookupNode) {
        return lookupNode.executeLookup(getSqueakClass(), toSelector(key));
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    protected final boolean isMemberInsertable(@SuppressWarnings("unused") final String member) {
        return false;
    }

    @ExportMessage
    protected final void writeMember(final String member, final Object value,
                    @Shared("lookupNode") @Cached final LookupMethodByStringNode lookupNode,
                    @Shared("wrapNode") @Cached final WrapToSqueakNode wrapNode,
                    @Shared("dispatchNode") @Cached final DispatchUneagerlyNode dispatchNode) throws UnsupportedMessageException {
        final Object methodObject = lookupNode.executeLookup(getSqueakClass(), toSelector(member));
        if (methodObject instanceof CompiledMethodObject) {
            final CompiledMethodObject method = (CompiledMethodObject) methodObject;
            if (method.getNumArgs() == 1) {
                dispatchNode.executeDispatch(method, new Object[]{this, wrapNode.executeWrap(value)}, NilObject.SINGLETON);
            } else {
                throw UnsupportedMessageException.create();
            }
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    protected final Object invokeMember(final String member, final Object[] arguments,
                    @Shared("lookupNode") @Cached final LookupMethodByStringNode lookupNode,
                    @Shared("wrapNode") @Cached final WrapToSqueakNode wrapNode,
                    @Shared("dispatchNode") @Cached final DispatchUneagerlyNode dispatchNode) throws UnsupportedMessageException, ArityException {
        final Object methodObject = lookupNode.executeLookup(getSqueakClass(), toSelector(member));
        if (methodObject instanceof CompiledMethodObject) {
            final CompiledMethodObject method = (CompiledMethodObject) methodObject;
            final int actualArity = arguments.length;
            final int expectedArity = method.getNumArgs();
            if (actualArity == expectedArity) {
                return dispatchNode.executeDispatch(method, ArrayUtils.copyWithFirst(wrapNode.executeObjects(arguments), this), NilObject.SINGLETON);
            } else {
                throw ArityException.create(1 + expectedArity, 1 + actualArity);  // +1 for receiver
            }
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    /**
     * Converts an interop identifier to a Smalltalk selector. Most languages do not allow colons in
     * identifiers, so treat underscores as colons as well.
     *
     * @param identifier for interop
     * @return Smalltalk selector
     */
    private static String toSelector(final String identifier) {
        return identifier.replace('_', ':');
    }

    @ExportMessage
    protected final ClassObject squeakClass() {
        return getSqueakClass();
    }

    @ExportMessage
    protected final long squeakHash() {
        return getSqueakHash();
    }
}
