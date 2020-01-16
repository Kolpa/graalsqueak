package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.*;

public class ArgumentAndReturnValues {
    private Set<String> arguments;
    private Set<String> returnValues;

    public ArgumentAndReturnValues() {
        this.arguments = new HashSet<>();
        this.returnValues = new HashSet<>();
    }

    public List<String> getArguments() {
        return new ArrayList<>(this.arguments);
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    public void addArguments(String[] arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public List<String> getReturnValues() {
        return new ArrayList<>(this.returnValues);
    }

    public void addReturnValue(String returnValue) {
        this.returnValues.add(returnValue);
    }
}
