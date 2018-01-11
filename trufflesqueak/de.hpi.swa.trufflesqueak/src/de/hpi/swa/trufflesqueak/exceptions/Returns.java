package de.hpi.swa.trufflesqueak.exceptions;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.ControlFlowException;

import de.hpi.swa.trufflesqueak.model.MethodContextObject;

public final class Returns {
    private static abstract class AbstractReturn extends ControlFlowException {
        private static final long serialVersionUID = 1L;
        @CompilationFinal protected final Object returnValue;

        public AbstractReturn(Object result) {
            returnValue = result;
        }

        public Object getReturnValue() {
            return returnValue;
        }
    }

    public static class LocalReturn extends AbstractReturn {
        private static final long serialVersionUID = 1L;

        public LocalReturn(Object result) {
            super(result);
        }

        @Override
        public String toString() {
            return String.format("LR (value: %s)", returnValue);
        }
    }

    public static class NonLocalReturn extends AbstractReturn {
        private static final long serialVersionUID = 1L;
        @CompilationFinal private final MethodContextObject targetContext;
        private boolean arrivedAtTargetContext = false;

        public NonLocalReturn(Object returnValue, MethodContextObject targetContext) {
            super(returnValue);
            this.targetContext = targetContext;
        }

        public MethodContextObject getTargetContext() {
            return targetContext;
        }

        public boolean hasArrivedAtTargetContext() {
            return arrivedAtTargetContext;
        }

        public void setArrivedAtTargetContext() {
            arrivedAtTargetContext = true;
        }

        @Override
        public String toString() {
            return String.format("NLR (value: %s, arrived: %s, target: %s)", returnValue, arrivedAtTargetContext, targetContext);
        }
    }

    public static class NonVirtualContextModification extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class NonVirtualReturn extends AbstractReturn {
        private static final long serialVersionUID = 1L;
        @CompilationFinal private final MethodContextObject targetContext;
        @CompilationFinal private final MethodContextObject currentContext;

        public NonVirtualReturn(Object returnValue, MethodContextObject targetContext, MethodContextObject currentContext) {
            super(returnValue);
            this.targetContext = targetContext;
            this.currentContext = currentContext;
        }

        public MethodContextObject getTargetContext() {
            return targetContext;
        }

        public MethodContextObject getCurrentContext() {
            return currentContext;
        }

        @Override
        public String toString() {
            return String.format("NVR (value: %s, current: %s, target: %s)", returnValue, currentContext, targetContext);
        }
    }

    public static class TopLevelReturn extends AbstractReturn {
        private static final long serialVersionUID = 1L;

        public TopLevelReturn(Object result) {
            super(result);
        }

        @Override
        public String toString() {
            return String.format("TLR (value: %s)", returnValue);
        }
    }
}