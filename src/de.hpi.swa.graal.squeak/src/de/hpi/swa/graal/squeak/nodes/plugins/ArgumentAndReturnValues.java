package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.*;

public class ArgumentAndReturnValues {
    private Set<Object> arguments;
    private Set<Object> returnValues;

    public ArgumentAndReturnValues() {
        this.arguments = new HashSet<>();
        this.returnValues = new HashSet<>();
    }

    public Object[] getArguments() {
        return arguments.toArray();
    }

    public void addArgument(Object argument) {
        this.arguments.add(argument);
    }

    public void addArguments(Object[] arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public Object[] getReturnValues() {
        return arguments.toArray();
    }

    public void addReturnValue(Object returnValue) {
        this.returnValues.add(returnValue);
    }
}
