package de.hpi.swa.graal.squeak.nodes.plugins;

import de.hpi.swa.graal.squeak.model.CompiledMethodObject;

public interface AddCallTypeListenerService {
    void addListenerForMethodCall(CompiledMethodObject target);
    ArgumentAndReturnValues getArgumentsForMethod(CompiledMethodObject target);
    void clearListeners();
}
