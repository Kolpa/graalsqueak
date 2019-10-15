/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.UnaryPrimitiveWithoutFallback;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

public final class Win32OSProcessPlugin extends AbstractOSProcessPlugin {

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        final List<NodeFactory<? extends AbstractPrimitiveNode>> factories = new ArrayList<>();
        factories.addAll(Win32OSProcessPluginFactory.getFactories());
        factories.addAll(AbstractOSProcessPluginFactory.getFactories());
        return factories;
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveGetEnvironmentStrings")
    protected abstract static class PrimGetEnvironmentStringNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        protected PrimGetEnvironmentStringNode(final CompiledCodeObject method) {
            super(method);
        }

        @Specialization
        @TruffleBoundary
        protected final Object doGet(@SuppressWarnings("unused") final Object receiver) {
            final Map<String, String> envMap = method.image.env.getEnvironment();
            final List<String> strings = new ArrayList<>();
            for (final Map.Entry<String, String> entry : envMap.entrySet()) {
                strings.add(entry.getKey() + "=" + entry.getValue());
            }
            return method.image.asByteString(String.join("\n", strings));
        }
    }
}
