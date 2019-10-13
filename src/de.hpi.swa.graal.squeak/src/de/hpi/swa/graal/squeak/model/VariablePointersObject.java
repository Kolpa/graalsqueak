/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.model;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.image.reading.SqueakImageChunk;
import de.hpi.swa.graal.squeak.nodes.ObjectGraphNode.ObjectTracer;
import de.hpi.swa.graal.squeak.nodes.accessing.AbstractPointersObjectNodes.AbstractPointersObjectWriteNode;
import de.hpi.swa.graal.squeak.nodes.accessing.UpdateSqueakObjectHashNode;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

public final class VariablePointersObject extends AbstractPointersObject {
    @CompilationFinal(dimensions = 0) public Object[] variablePart;

    public VariablePointersObject(final SqueakImageContext image, final long hash, final ClassObject classObject) {
        super(image, hash, classObject);
    }

    public VariablePointersObject(final SqueakImageContext image, final ClassObject classObject, final int variableSize) {
        super(image, classObject);
        variablePart = ArrayUtils.withAll(variableSize, NilObject.SINGLETON);
    }

    private VariablePointersObject(final VariablePointersObject original) {
        super(original);
        variablePart = original.variablePart.clone();
    }

    @Override
    public void fillin(final SqueakImageChunk chunk) {
        final AbstractPointersObjectWriteNode writeNode = AbstractPointersObjectWriteNode.getUncached();
        final Object[] pointersObject = chunk.getPointers();
        initializeLayoutAndExtensionsUnsafe();
        final int instSize = getSqueakClass().getBasicInstanceSize();
        for (int i = 0; i < instSize; i++) {
            writeNode.execute(this, i, pointersObject[i]);
        }
        variablePart = Arrays.copyOfRange(pointersObject, instSize, pointersObject.length);
        assert size() == pointersObject.length;
    }

    public void become(final VariablePointersObject other) {
        becomeLayout(other);
        final Object[] otherVariablePart = other.variablePart;
        /*
         * Keep outer arrays and only copy contents as variablePart is marked
         * with @CompilationFinal(dimensions = 0).
         */
        System.arraycopy(variablePart, 0, other.variablePart, 0, variablePart.length);
        System.arraycopy(otherVariablePart, 0, variablePart, 0, otherVariablePart.length);
    }

    @Override
    public int size() {
        return instsize() + variablePart.length;
    }

    public void pointersBecomeOneWay(final UpdateSqueakObjectHashNode updateHashNode, final Object[] from, final Object[] to, final boolean copyHash) {
        layoutValuesBecomeOneWay(updateHashNode, from, to, copyHash);
        final int variableSize = variablePart.length;
        if (variableSize > 0) {
            for (int i = 0; i < from.length; i++) {
                final Object fromPointer = from[i];
                for (int j = 0; j < variableSize; j++) {
                    final Object object = getFromVariablePart(j);
                    if (object == fromPointer) {
                        putIntoVariablePart(j, to[i]);
                        updateHashNode.executeUpdate(fromPointer, to[i], copyHash);
                    }
                }
            }
        }
    }

    public boolean pointsTo(final Object thang) {
        return layoutValuesPointTo(thang) || ArrayUtils.contains(variablePart, thang);
    }

    public Object getFromVariablePart(final int index) {
        return variablePart[index];
    }

    public void putIntoVariablePart(final int index, final Object value) {
        variablePart[index] = value;
    }

    public Object at0(final int i) {
        final int instsize = instsize();
        if (i < instsize) {
            if (!getLayout().isValid()) {
                updateLayout();
            }
            return getLayout().getLocation(i).read(this);
        } else {
            return variablePart[i - instsize];
        }
    }

    public void atput0(final int i, final Object value) {
        assert value != null : "Unexpected `null` value";
        final int instsize = instsize();
        if (i < instsize) {
            if (!getLayout().getLocation(i).canStore(value)) {
                updateLayout(i, value);
            }
            getLayout().getLocation(i).writeMustSucceed(this, value);
        } else {
            variablePart[i - instsize] = value;
        }
    }

    public void atputNil0(final int i) {
        atput0(i, NilObject.SINGLETON);
    }

    public VariablePointersObject shallowCopy() {
        return new VariablePointersObject(this);
    }

    public void traceObjects(final ObjectTracer tracer) {
        super.traceLayoutObjects(tracer);
        for (final Object object : variablePart) {
            tracer.addIfUnmarked(object);
        }
    }
}
