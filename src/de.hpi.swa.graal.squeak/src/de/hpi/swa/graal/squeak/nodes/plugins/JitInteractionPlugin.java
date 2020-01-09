/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import de.hpi.swa.graal.squeak.exceptions.PrimitiveExceptions.PrimitiveFailed;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitive;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

import java.lang.reflect.Field;
import java.util.List;

public final class JitInteractionPlugin extends AbstractPrimitiveFactoryHolder {

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return JitInteractionPluginFactory.getFactories();
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getCallTarget")
    protected abstract static class GetCallTargetNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected GetCallTargetNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getCallTarget(final Object receiver, final CompiledMethodObject value) {
            return method.image.env.asGuestValue(value.getCallTarget());
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getKnownCallNodes")
    protected abstract static class GetKnownCallNodesNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected GetKnownCallNodesNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getKnownCallNodes(final Object receiver, final CompiledMethodObject value) {
            CallTarget callTarget = value.getCallTarget();

            try {
                Class<?> optimizedCallTargetClass = Class.forName(
                        "org.graalvm.compiler.truffle.runtime.OptimizedCallTarget",
                        false,
                        callTarget.getClass().getClassLoader()
                );

                if (!optimizedCallTargetClass.isInstance(callTarget)) {
                    return method.image.env.asGuestValue(new Object[]{});
                }

                Field knownCallNodesField = optimizedCallTargetClass.getDeclaredField("knownCallNodes");

                knownCallNodesField.setAccessible(true);

                return method.image.env.asGuestValue(knownCallNodesField.get(callTarget));
            } catch (ReflectiveOperationException e) {
                throw PrimitiveFailed.GENERIC_ERROR;
            }
        }
    }
}
