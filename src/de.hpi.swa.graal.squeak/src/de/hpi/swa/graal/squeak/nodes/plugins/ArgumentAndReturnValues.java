package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.*;

public class ArgumentAndReturnValues {
    private List<List<String>> arguments;
    private Set<String> returnValues;

    public ArgumentAndReturnValues() {
        this.arguments = new ArrayList<>();
        this.returnValues = new HashSet<>();
    }

    public List<List<String>> getArguments() {
        return this.arguments;
    }

    public void addArguments(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            if (this.arguments.size() - 1 != i) {
                this.arguments.add(new ArrayList<>());
            }
            List<String> argument = this.arguments.get(i);
            if (!argument.contains(arguments[i])) {
                argument.add(arguments[i]);
            }
        }
    }

    public List<String> getReturnValues() {
        return new ArrayList<>(this.returnValues);
    }

    public void addReturnValue(String returnValue) {
        this.returnValues.add(returnValue);
    }
}
