/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.List;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.graal.squeak.model.BooleanObject;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitive;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

public final class CroquetPlugin extends AbstractPrimitiveFactoryHolder {
    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveGatherEntropy")
    protected abstract static class PrimGatherEntropyNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected PrimGatherEntropyNode(final CompiledCodeObject method) {
            super(method);
        }

        @Specialization(guards = "byteArray.isByteType()")
        protected static final Object doGather(@SuppressWarnings("unused") final Object receiver, final NativeObject byteArray) {
            ArrayUtils.fillRandomly(byteArray.getByteStorage());
            return BooleanObject.TRUE;
        }
    }

    // TODO: implement other primitives?

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return CroquetPluginFactory.getFactories();
    }
}
