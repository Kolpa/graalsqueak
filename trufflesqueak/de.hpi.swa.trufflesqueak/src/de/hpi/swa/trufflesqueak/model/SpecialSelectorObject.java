package de.hpi.swa.trufflesqueak.model;

import de.hpi.swa.trufflesqueak.SqueakImageContext;

public class SpecialSelectorObject extends NativeObject {
    private final int numArguments;
    private final int primitiveIndex;

    public SpecialSelectorObject(SqueakImageContext img, int elementSize, int numArguments, int primitiveIndex) {
        super(img, (byte) elementSize);
        this.numArguments = numArguments;
        this.primitiveIndex = primitiveIndex;
    }

    public SpecialSelectorObject(SqueakImageContext img, int elementSize, int numArguments) {
        this(img, elementSize, numArguments, -1);
    }

    public int getNumArguments() {
        return numArguments;
    }

    public int getPrimitiveIndex() {
        return primitiveIndex;
    }

}