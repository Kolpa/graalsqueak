/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitive;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

import java.util.List;

public final class JitInteractionPlugin extends AbstractPrimitiveFactoryHolder {

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return JitInteractionPluginFactory.getFactories();
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getJitInfo")
    protected abstract static class GetJitInfoNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected GetJitInfoNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getJitInfo(final Object receiver, final CompiledMethodObject value) {
            return method.image.env.asGuestValue(value.getCallTarget());
        }
    }
}
