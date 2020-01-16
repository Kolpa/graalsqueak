package de.hpi.swa.graal.squeak.nodes.plugins;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;

import java.util.*;

@Registration(
        id = ReturnAndCallTypeInstrument.ID,
        name = "Return and Call Instrument",
        version = "0.1",
        services = AddCallTypeListenerService.class
)
public class ReturnAndCallTypeInstrument extends TruffleInstrument implements AddCallTypeListenerService {
    public static final String ID = "return-and-call-instrument";

    private Env env;
    private List<EventBinding<ExecutionEventListener>> bindings;
    private Map<CompiledMethodObject, ArgumentAndReturnValues> methodCache;

    @Override
    protected void onCreate(Env env) {
        this.env = env;
        this.bindings = new LinkedList<>();
        this.methodCache = new HashMap<>();

        env.registerService(this);
    }

    @Override
    public void addListenerForMethodCall(CompiledMethodObject target) {
        SourceSectionFilter filter = SourceSectionFilter
                .newBuilder()
                .sourceIs(target.getSource())
                .lineIs(1)
                .build();

        this.bindings.add(this.env.getInstrumenter().attachExecutionEventListener(filter, new ExecutionEventListener() {
            @Override
            public void onEnter(EventContext context, VirtualFrame frame) {
                methodCache.putIfAbsent(target, new ArgumentAndReturnValues());
                methodCache.get(target).addArguments(frame.getArguments());
            }

            @Override
            public void onReturnValue(EventContext context, VirtualFrame frame, Object result) {
                methodCache.putIfAbsent(target, new ArgumentAndReturnValues());
                methodCache.get(target).addReturnValue(result);
            }

            @Override
            public void onReturnExceptional(EventContext context, VirtualFrame frame, Throwable exception) {
                // Do nothing
            }
        }));
    }

    @Override
    public ArgumentAndReturnValues getArgumentsForMethod(CompiledMethodObject target) {
        return this.methodCache.get(target);
    }

    @Override
    public void clearListeners() {
        this.bindings.forEach(EventBinding::dispose);
    }
}
