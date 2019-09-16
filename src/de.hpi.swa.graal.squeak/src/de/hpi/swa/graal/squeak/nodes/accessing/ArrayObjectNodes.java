package de.hpi.swa.graal.squeak.nodes.accessing;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import de.hpi.swa.graal.squeak.exceptions.Returns;
import de.hpi.swa.graal.squeak.model.AbstractSqueakObject;
import de.hpi.swa.graal.squeak.model.ArrayObject;
import de.hpi.swa.graal.squeak.model.BooleanObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.model.NilObject;
import de.hpi.swa.graal.squeak.nodes.AbstractNode;
import de.hpi.swa.graal.squeak.nodes.accessing.ArrayObjectNodesFactory.ArrayObjectReadNodeGen;
import de.hpi.swa.graal.squeak.nodes.accessing.ArrayObjectNodesFactory.ArrayObjectSizeNodeGen;
import de.hpi.swa.graal.squeak.nodes.accessing.ArrayObjectNodesFactory.ArrayObjectToObjectArrayCopyNodeGen;
import de.hpi.swa.graal.squeak.nodes.accessing.ArrayObjectNodesFactory.ArrayObjectTraceableToObjectArrayNodeGen;
import de.hpi.swa.graal.squeak.nodes.accessing.ArrayObjectNodesFactory.ArrayObjectWriteNodeGen;
import de.hpi.swa.graal.squeak.util.ArrayUtils;
import de.hpi.swa.graal.squeak.util.UnsafeUtils;

public final class ArrayObjectNodes {

    @GenerateUncached
    public abstract static class ArrayObjectReadNode extends AbstractNode {

        public static ArrayObjectReadNode create() {
            return ArrayObjectReadNodeGen.create();
        }

        public static ArrayObjectReadNode getUncached() {
            return ArrayObjectReadNodeGen.getUncached();
        }

        public abstract Object execute(ArrayObject obj, long index);

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final NilObject doEmptyArray(final ArrayObject obj, final long index,
                        @Cached final BranchProfile outOfBoundsProfile) {
            if (index < 0 || obj.getEmptyLength() <= index) {
                outOfBoundsProfile.enter();
                throw Returns.OUT_OF_BOUNDS;
            } else {
                return NilObject.SINGLETON;
            }
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final Object doArrayOfBooleans(final ArrayObject obj, final long index,
                        @Cached("createBinaryProfile()") final ConditionProfile falseProfile,
                        @Cached("createBinaryProfile()") final ConditionProfile trueProfile) {
            final byte value = UnsafeUtils.getByte(obj.getStorage(), index);
            if (falseProfile.profile(value == ArrayObject.BOOLEAN_FALSE_TAG)) {
                return BooleanObject.FALSE;
            } else if (trueProfile.profile(value == ArrayObject.BOOLEAN_TRUE_TAG)) {
                return BooleanObject.TRUE;
            } else {
                assert value == ArrayObject.BOOLEAN_NIL_TAG;
                return NilObject.SINGLETON;
            }
        }

        @Specialization(guards = "obj.isCharType()")
        protected static final Object doArrayOfChars(final ArrayObject obj, final long index,
                        @Shared("nilProfile") @Cached("createBinaryProfile()") final ConditionProfile nilProfile) {
            final char value = UnsafeUtils.getChar(obj.getStorage(), index);
            return nilProfile.profile(value == ArrayObject.CHAR_NIL_TAG) ? NilObject.SINGLETON : value;
        }

        @Specialization(guards = "obj.isLongType()")
        protected static final Object doArrayOfLongs(final ArrayObject obj, final long index,
                        @Shared("nilProfile") @Cached("createBinaryProfile()") final ConditionProfile nilProfile) {
            final long value = UnsafeUtils.getLong(obj.getStorage(), index);
            return nilProfile.profile(value == ArrayObject.LONG_NIL_TAG) ? NilObject.SINGLETON : value;
        }

        @Specialization(guards = "obj.isDoubleType()")
        protected static final Object doArrayOfDoubles(final ArrayObject obj, final long index,
                        @Shared("nilProfile") @Cached("createBinaryProfile()") final ConditionProfile nilProfile) {
            final double value = UnsafeUtils.getDouble(obj.getStorage(), index);
            return nilProfile.profile(Double.doubleToRawLongBits(value) == ArrayObject.DOUBLE_NIL_TAG_LONG) ? NilObject.SINGLETON : value;
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final AbstractSqueakObject doArrayOfNativeObjects(final ArrayObject obj, final long index,
                        @Shared("nilProfile") @Cached("createBinaryProfile()") final ConditionProfile nilProfile) {
            return NilObject.nullToNil(obj.getNativeObjectStorage()[(int) index], nilProfile);
        }

        @Specialization(guards = "obj.isObjectType()")
        protected static final Object doArrayOfObjects(final ArrayObject obj, final long index) {
            assert obj.getObjectStorage()[(int) index] != null : "Unexpected `null` value";
            return UnsafeUtils.getObject(obj.getStorage(), index);
        }
    }

    @GenerateUncached
    public abstract static class ArrayObjectShallowCopyNode extends AbstractNode {

        public abstract ArrayObject execute(ArrayObject obj);

        @Specialization(guards = "obj.isEmptyType()")
        protected static final ArrayObject doEmptyArray(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getEmptyStorage());
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final ArrayObject doArrayOfBooleans(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getBooleanStorage().clone());
        }

        @Specialization(guards = "obj.isCharType()")
        protected static final ArrayObject doArrayOfChars(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getCharStorage().clone());
        }

        @Specialization(guards = "obj.isLongType()")
        protected static final ArrayObject doArrayOfLongs(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getLongStorage().clone());
        }

        @Specialization(guards = "obj.isDoubleType()")
        protected static final ArrayObject doArrayOfDoubles(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getDoubleStorage().clone());
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final ArrayObject doArrayOfNatives(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getNativeObjectStorage().clone());
        }

        @Specialization(guards = "obj.isObjectType()")
        protected static final ArrayObject doArrayOfObjects(final ArrayObject obj) {
            return ArrayObject.createWithStorage(obj.image, obj.getSqueakClass(), obj.getObjectStorage().clone());
        }
    }

    @GenerateUncached
    public abstract static class ArrayObjectSizeNode extends AbstractNode {

        public static ArrayObjectSizeNode create() {
            return ArrayObjectSizeNodeGen.create();
        }

        public static ArrayObjectSizeNode getUncached() {
            return ArrayObjectSizeNodeGen.getUncached();
        }

        public abstract int execute(ArrayObject obj);

        @Specialization(guards = "obj.isEmptyType()")
        protected static final int doEmptyArrayObject(final ArrayObject obj) {
            return obj.getEmptyStorage();
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final int doArrayObjectOfBooleans(final ArrayObject obj) {
            return obj.getBooleanLength();
        }

        @Specialization(guards = "obj.isCharType()")
        protected static final int doArrayObjectOfChars(final ArrayObject obj) {
            return obj.getCharLength();
        }

        @Specialization(guards = "obj.isLongType()")
        protected static final int doArrayObjectOfLongs(final ArrayObject obj) {
            return obj.getLongLength();
        }

        @Specialization(guards = "obj.isDoubleType()")
        protected static final int doArrayObjectOfDoubles(final ArrayObject obj) {
            return obj.getDoubleLength();
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final int doArrayObjectOfNatives(final ArrayObject obj) {
            return obj.getNativeObjectLength();
        }

        @Specialization(guards = "obj.isObjectType()")
        protected static final int doArrayObjectOfObjects(final ArrayObject obj) {
            return obj.getObjectLength();
        }
    }

    @GenerateUncached
    public abstract static class ArrayObjectToObjectArrayCopyNode extends AbstractNode {

        public static ArrayObjectToObjectArrayCopyNode create() {
            return ArrayObjectToObjectArrayCopyNodeGen.create();
        }

        public final Object[] executeWithFirst(final ArrayObject obj, final Object first) {
            return ArrayUtils.copyWithFirst(execute(obj), first);
        }

        public abstract Object[] execute(ArrayObject obj);

        @Specialization(guards = "obj.isObjectType()")
        protected static final Object[] doArrayOfObjects(final ArrayObject obj) {
            return obj.getObjectStorage();
        }

        @Specialization(guards = "obj.isEmptyType()")
        protected static final Object[] doEmptyArray(final ArrayObject obj) {
            return ArrayUtils.withAll(obj.getEmptyStorage(), NilObject.SINGLETON);
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final Object[] doArrayOfBooleans(final ArrayObject obj) {
            final byte[] booleans = obj.getBooleanStorage();
            final int length = booleans.length;
            final Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                objects[i] = ArrayObject.toObjectFromBoolean(booleans[i]);
            }
            return objects;
        }

        @Specialization(guards = "obj.isCharType()")
        protected static final Object[] doArrayOfChars(final ArrayObject obj) {
            final char[] chars = obj.getCharStorage();
            final int length = chars.length;
            final Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                final char value = chars[i];
                objects[i] = ArrayObject.toObjectFromChar(value);
            }
            return objects;
        }

        @Specialization(guards = "obj.isLongType()")
        protected static final Object[] doArrayOfLongs(final ArrayObject obj) {
            final long[] longs = obj.getLongStorage();
            final int length = longs.length;
            final Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                objects[i] = ArrayObject.toObjectFromLong(longs[i]);
            }
            return objects;
        }

        @Specialization(guards = "obj.isDoubleType()")
        protected static final Object[] doArrayOfDoubles(final ArrayObject obj) {
            final double[] doubles = obj.getDoubleStorage();
            final int length = doubles.length;
            final Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                objects[i] = ArrayObject.toObjectFromDouble(doubles[i]);
            }
            return objects;
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final Object[] doArrayOfNatives(final ArrayObject obj) {
            final NativeObject[] nativeObjects = obj.getNativeObjectStorage();
            final int length = nativeObjects.length;
            final Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                objects[i] = ArrayObject.toObjectFromNativeObject(nativeObjects[i]);
            }
            return objects;
        }
    }

    @GenerateUncached
    public abstract static class ArrayObjectTraceableToObjectArrayNode extends AbstractNode {

        public static ArrayObjectTraceableToObjectArrayNode create() {
            return ArrayObjectTraceableToObjectArrayNodeGen.create();
        }

        public abstract Object[] execute(ArrayObject obj);

        @Specialization(guards = "obj.isObjectType()")
        protected static final Object[] doArrayOfObjects(final ArrayObject obj) {
            return obj.getObjectStorage();
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final Object[] doArrayOfNatives(final ArrayObject obj) {
            return obj.getNativeObjectStorage();
        }
    }

    @GenerateUncached
    @ImportStatic(ArrayObject.class)
    public abstract static class ArrayObjectWriteNode extends AbstractNode {

        public static ArrayObjectWriteNode create() {
            return ArrayObjectWriteNodeGen.create();
        }

        public static ArrayObjectWriteNode getUncached() {
            return ArrayObjectWriteNodeGen.getUncached();
        }

        public abstract void execute(ArrayObject obj, long index, Object value);

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArray(final ArrayObject obj, final long index, final NilObject value) {
            assert index < obj.getEmptyLength();
            // Nothing to do.
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArray(final ArrayObject obj, final long index, final NativeObject value) {
            obj.transitionFromEmptyToNatives();
            doArrayOfNativeObjects(obj, index, value);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArrayToBoolean(final ArrayObject obj, final long index, final boolean value) {
            obj.transitionFromEmptyToBooleans();
            doArrayOfBooleans(obj, index, value);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArrayToChar(final ArrayObject obj, final long index, final char value,
                        @Cached final BranchProfile nilTagProfile) {
            if (ArrayObject.isCharNilTag(value)) {
                nilTagProfile.enter();
                doEmptyArrayToObject(obj, index, value);
                return;
            }
            obj.transitionFromEmptyToChars();
            doArrayOfChars(obj, index, value);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArrayToLong(final ArrayObject obj, final long index, final long value,
                        @Cached final BranchProfile nilTagProfile) {
            if (ArrayObject.isLongNilTag(value)) {
                nilTagProfile.enter();
                doEmptyArrayToObject(obj, index, value);
                return;
            }
            obj.transitionFromEmptyToLongs();
            doArrayOfLongs(obj, index, value);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()"})
        protected static final void doEmptyArrayToDouble(final ArrayObject obj, final long index, final double value,
                        @Cached final BranchProfile nilTagProfile) {
            if (ArrayObject.isDoubleNilTag(value)) {
                nilTagProfile.enter();
                doEmptyArrayToObject(obj, index, value);
                return;
            }
            obj.transitionFromEmptyToDoubles();
            doArrayOfDoubles(obj, index, value);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"obj.isEmptyType()", "!isBoolean(value)", "!isCharacter(value)", "!isLong(value)", "!isDouble(value)", "!isNativeObject(value)"})
        protected static final void doEmptyArrayToObject(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromEmptyToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final void doArrayOfBooleans(final ArrayObject obj, final long index, final boolean value) {
            UnsafeUtils.putByte(obj.getStorage(), index, value ? ArrayObject.BOOLEAN_TRUE_TAG : ArrayObject.BOOLEAN_FALSE_TAG);
        }

        @Specialization(guards = "obj.isBooleanType()")
        protected static final void doArrayOfBooleans(final ArrayObject obj, final long index, @SuppressWarnings("unused") final NilObject value) {
            UnsafeUtils.putByte(obj.getStorage(), index, ArrayObject.BOOLEAN_NIL_TAG);
        }

        @Specialization(guards = {"obj.isBooleanType()", "!isBoolean(value)", "!isNil(value)"})
        protected static final void doArrayOfBooleans(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromBooleansToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = {"obj.isCharType()", "!isCharNilTag(value)"})
        protected static final void doArrayOfChars(final ArrayObject obj, final long index, final char value) {
            UnsafeUtils.putChar(obj.getStorage(), index, value);
        }

        @Specialization(guards = {"obj.isCharType()", "isCharNilTag(value)"})
        protected static final void doArrayOfCharsNilTagClash(final ArrayObject obj, final long index, final char value) {
            /** `value` happens to be char nil tag, need to despecialize to be able store it. */
            obj.transitionFromCharsToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isCharType()")
        protected static final void doArrayOfChars(final ArrayObject obj, final long index, @SuppressWarnings("unused") final NilObject value) {
            UnsafeUtils.putChar(obj.getStorage(), index, ArrayObject.CHAR_NIL_TAG);
        }

        @Specialization(guards = {"obj.isCharType()", "!isCharacter(value)", "!isNil(value)"})
        protected static final void doArrayOfChars(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromCharsToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = {"obj.isLongType()", "!isLongNilTag(value)"})
        protected static final void doArrayOfLongs(final ArrayObject obj, final long index, final long value) {
            UnsafeUtils.putLong(obj.getStorage(), index, value);
        }

        @Specialization(guards = {"obj.isLongType()", "isLongNilTag(value)"})
        protected static final void doArrayOfLongsNilTagClash(final ArrayObject obj, final long index, final long value) {
            /** `value` happens to be long nil tag, need to despecialize to be able store it. */
            obj.transitionFromLongsToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isLongType()")
        protected static final void doArrayOfLongs(final ArrayObject obj, final long index, @SuppressWarnings("unused") final NilObject value) {
            UnsafeUtils.putLong(obj.getStorage(), index, ArrayObject.LONG_NIL_TAG);
        }

        @Specialization(guards = {"obj.isLongType()", "!isLong(value)", "!isNil(value)"})
        protected static final void doArrayOfLongs(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromLongsToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = {"obj.isDoubleType()", "!isDoubleNilTag(value)"})
        protected static final void doArrayOfDoubles(final ArrayObject obj, final long index, final double value) {
            UnsafeUtils.putDouble(obj.getStorage(), index, value);
        }

        @Specialization(guards = {"obj.isDoubleType()", "isDoubleNilTag(value)"})
        protected static final void doArrayOfDoublesNilTagClash(final ArrayObject obj, final long index, final double value) {
            // `value` happens to be double nil tag, need to despecialize to be able store it.
            obj.transitionFromDoublesToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isDoubleType()")
        protected static final void doArrayOfDoubles(final ArrayObject obj, final long index, @SuppressWarnings("unused") final NilObject value) {
            UnsafeUtils.putDouble(obj.getStorage(), index, ArrayObject.DOUBLE_NIL_TAG);
        }

        @Specialization(guards = {"obj.isDoubleType()", "!isDouble(value)", "!isNil(value)"})
        protected static final void doArrayOfDoubles(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromDoublesToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final void doArrayOfNativeObjects(final ArrayObject obj, final long index, final NativeObject value) {
            obj.getNativeObjectStorage()[(int) index] = value;
        }

        @Specialization(guards = "obj.isNativeObjectType()")
        protected static final void doArrayOfNativeObjects(final ArrayObject obj, final long index, @SuppressWarnings("unused") final NilObject value) {
            obj.getNativeObjectStorage()[(int) index] = ArrayObject.NATIVE_OBJECT_NIL_TAG;
        }

        @Specialization(guards = {"obj.isNativeObjectType()", "!isNativeObject(value)", "!isNil(value)"})
        protected static final void doArrayOfNativeObjects(final ArrayObject obj, final long index, final Object value) {
            obj.transitionFromNativesToObjects();
            doArrayOfObjects(obj, index, value);
        }

        @Specialization(guards = "obj.isObjectType()")
        protected static final void doArrayOfObjects(final ArrayObject obj, final long index, final Object value) {
            UnsafeUtils.putObject(obj.getStorage(), index, value);
        }
    }
}
