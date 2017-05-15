package de.hpi.swa.trufflesqueak.test;

import java.math.BigInteger;

import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.ListObject;
import de.hpi.swa.trufflesqueak.model.PointersObject;

public class TestPrimitives extends TestSqueak {
    public void testPrimEquivalent() {
        BaseSqueakObject rcvr = image.specialObjectsArray;
        assertTrue((boolean) runPrim(110, rcvr, rcvr));
        assertFalse((boolean) runPrim(110, rcvr, image.nil));
    }

    public void testPrimReplaceFromTo() {
        BaseSqueakObject rcvr = new ListObject(
                        image,
                        image.arrayClass,
                        new BaseSqueakObject[]{
                                        image.nil,
                                        image.sqFalse,
                                        image.sqTrue,
                                        image.characterClass,
                                        image.metaclass,
                                        image.schedulerAssociation,
                                        image.smallIntegerClass,
                                        image.smalltalk,
                                        image.specialObjectsArray});
        for (int i = 0; i < 8; i++) {
            assertNotSame(rcvr.at0(i), null);
        }
        Object result = runPrim(105, rcvr, image.wrap(1), image.wrap(6), new ListObject(image, image.nilClass, 10), image.wrap(1));
        assertSame(result, rcvr);
        for (int i = 0; i < 6; i++) {
            assertSame(rcvr.at0(i), null);
        }
        for (int i = 7; i < 8; i++) {
            assertNotSame(rcvr.at0(i), null);
        }
    }

    public void testAdd() {
        Object[] calcs = new Object[]{
                        Integer.MAX_VALUE, Integer.MAX_VALUE, 2 * Integer.MAX_VALUE,
                        Long.MAX_VALUE, Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2)),
        };
        for (int i = 0; i < calcs.length + 2; i += 3) {
            BaseSqueakObject rcvr = image.wrap(calcs[i]);
            BaseSqueakObject arg = image.wrap(calcs[i + 1]);
            assertEquals(runPrim(1, rcvr, arg), calcs[i + 2]);
        }
    }
}
