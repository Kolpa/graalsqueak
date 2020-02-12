/*
 * Copyright (c) 2017-2019 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.graal.squeak.nodes.plugins;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;

import de.hpi.swa.graal.squeak.exceptions.PrimitiveExceptions.PrimitiveFailed;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.nodes.EnterCodeNode;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitive;
import de.hpi.swa.graal.squeak.nodes.primitives.PrimitiveInterfaces.UnaryPrimitiveWithoutFallback;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

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
            final CallTarget callTarget = value.getCallTarget();

            try {
                final Class<?> optimizedCallTargetClass = Class.forName(
                                "org.graalvm.compiler.truffle.runtime.OptimizedCallTarget",
                                false,
                                callTarget.getClass().getClassLoader());

                if (!optimizedCallTargetClass.isInstance(callTarget)) {
                    return method.image.env.asGuestValue(new Object[]{});
                }

                final Field knownCallNodesField = optimizedCallTargetClass.getDeclaredField("knownCallNodes");

                knownCallNodesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                final ArrayList<WeakReference<DirectCallNode>> knownCallNodes = (ArrayList<WeakReference<DirectCallNode>>) knownCallNodesField.get(callTarget);

                final HashSet<CompiledMethodObject> knownCallerMethods = new HashSet<>();
                for (final WeakReference<DirectCallNode> knownCallNode : knownCallNodes) {
                    final DirectCallNode callNode = knownCallNode.get();
                    if (callNode == null) {
                        continue;
                    }
                    final RootNode rootNode = callNode.getRootNode();
                    if (rootNode instanceof EnterCodeNode) {
                        knownCallerMethods.add(((EnterCodeNode) rootNode).getCodeObject().getMethod());
                    }

                }
                return method.image.asArrayOfObjects(knownCallerMethods.toArray());
            } catch (final ReflectiveOperationException e) {
                throw PrimitiveFailed.GENERIC_ERROR;
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "addListenerForMethodCall")
    protected abstract static class AddListenerForMethodCallNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected AddListenerForMethodCallNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object addListenerForMethodCall(final Object receiver, final CompiledMethodObject value) {
            final Map<String, Instrument> instruments = Context.getCurrent().getEngine().getInstruments();

            final AddCallTypeListenerService service = instruments.get(ReturnAndCallTypeInstrument.ID).lookup(AddCallTypeListenerService.class);

            service.addListenerForMethodCall(value);
            return true;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getArgumentsForMethod")
    protected abstract static class GetArgumentsForMethodNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected GetArgumentsForMethodNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getArgumentsForMethod(final Object receiver, final CompiledMethodObject value) {
            final Map<String, Instrument> instruments = Context.getCurrent().getEngine().getInstruments();

            final AddCallTypeListenerService service = instruments.get(ReturnAndCallTypeInstrument.ID).lookup(AddCallTypeListenerService.class);

            return method.image.env.asGuestValue(service.getArgumentsForMethod(value));
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "clearMethodCallListeners")
    protected abstract static class ClearMethodCallListenersNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        protected ClearMethodCallListenersNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object clearMethodCallListeners(final Object receiver) {
            final Map<String, Instrument> instruments = Context.getCurrent().getEngine().getInstruments();

            final AddCallTypeListenerService service = instruments.get(ReturnAndCallTypeInstrument.ID).lookup(AddCallTypeListenerService.class);

            service.clearListeners();

            return true;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "getJavaArgumentsForMethod")
    protected abstract static class GetJavaArgumentsForMethodNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        protected GetJavaArgumentsForMethodNode(final CompiledMethodObject method) {
            super(method);
        }

        @Specialization
        protected Object getJavaArgumentsForMethod(final Object receiver, final CompiledMethodObject value) {
            final CallTarget callTarget = value.getCallTarget();

            try {
                final Class<?> optimizedCallTargetClass = Class.forName(
                                "org.graalvm.compiler.truffle.runtime.OptimizedCallTarget",
                                false,
                                callTarget.getClass().getClassLoader());

                final Method getProfiledArgumentTypesMethod = optimizedCallTargetClass.getDeclaredMethod("getProfiledArgumentTypes");
                getProfiledArgumentTypesMethod.setAccessible(true);
                return method.image.env.asGuestValue(getProfiledArgumentTypesMethod.invoke(value.getCallTarget()));
            } catch (final ReflectiveOperationException e) {
                throw PrimitiveFailed.GENERIC_ERROR;
            }
        }
    }
}
