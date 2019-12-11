/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitiveWithoutFallback;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class GraalSqueakPlugin extends AbstractPrimitiveFactoryHolder {

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return GraalSqueakPluginFactory.getFactories();
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "debugPrint")
    protected abstract static class PrimPrintArgsNode extends AbstractPrimitiveNode implements BinaryPrimitiveWithoutFallback {
        protected PrimPrintArgsNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object printArgs(final Object receiver, final Object value) {
            method.image.printToStdOut(value);
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getJitInfo")
    protected abstract static class GetJitInfoNode extends AbstractPrimitiveNode implements BinaryPrimitiveWithoutFallback {
        protected GetJitInfoNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getJitInfo(final Object receiver, final CompiledMethodObject value) {
            try {
                ClassLoader loader = value.getCallTarget().getClass().getClassLoader();

                Class<?> optimizedCallTarget = Class.forName(
                        "org.graalvm.compiler.truffle.runtime.OptimizedCallTarget",
                        true,
                        loader
                );

                Class<?> optimizedCompilationProfile = Class.forName(
                        "org.graalvm.compiler.truffle.runtime.OptimizedCompilationProfile",
                        true,
                        loader
                );

                Method getCompilationProfile = optimizedCallTarget.getDeclaredMethod("getCompilationProfile");
                Method getCallCount = optimizedCompilationProfile.getDeclaredMethod("getCallCount");

                Object optimizedCompilationProfileInstance = getCompilationProfile.invoke(value.getCallTarget());

                Integer callCount = (Integer) getCallCount.invoke(optimizedCompilationProfileInstance);

                return callCount;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                return SqueakExceptions.SqueakException.create("Failed to create JitInfoObject", e);
            }
        }
    }
}
