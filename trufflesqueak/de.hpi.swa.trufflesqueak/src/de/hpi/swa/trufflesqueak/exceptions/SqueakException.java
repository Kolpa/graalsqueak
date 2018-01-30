package de.hpi.swa.trufflesqueak.exceptions;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class SqueakException extends ControlFlowException {
    private static final long serialVersionUID = 1L;
    private final String message;

    public SqueakException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}