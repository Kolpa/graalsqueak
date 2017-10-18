package de.hpi.swa.trufflesqueak.model;

import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;
import de.hpi.swa.trufflesqueak.SqueakImageContext;

// refer to com.oracle.truffle.api.interop.Message for documentation

@MessageResolution(receiverType = BaseSqueakObject.class)
public class BaseSqueakObjectMessageResolution {
    
    @Resolve(message = "WRITE")
    public abstract static class BaseSqueakObjectWriteNode extends Node {
        public Object access(Object receiver, Object name, Object value) {
            throw new RuntimeException("not yet implemented");
        }
    }
    
    @Resolve(message = "READ")
    public abstract static class BaseSqueakObjectReadNode extends Node {
        public Object access(Object receiver, int index) {
            System.err.println("Object>>READ " + receiver.toString() + " " + index);
            if (receiver instanceof AbstractPointersObject) {
                return ((AbstractPointersObject) receiver).at0(index).toString();
            }
            throw UnknownIdentifierException.raise("cannot read on " + receiver.getClass().toString());
        }
        
        public Object access(Object receiver, String name) {
            System.err.println("Object>>READ " + receiver.toString() + " " + name);
            if (name.equals("a")) {
                return 42;
            } else if (name.equals("b")) {
                return 43;
            } else if (name.equals("c")) {
                return 44;
            }
            throw UnknownIdentifierException.raise("foo");
        }
    }
    
    @Resolve(message = "HAS_SIZE")
    public abstract static class BaseSqueakObjectHasSizeNode extends Node {
        public Object access(Object receiver) {
            return (receiver instanceof AbstractPointersObject);
        }
    }
    
    @Resolve(message = "GET_SIZE")
    public abstract static class BaseSqueakObjectGetSizeNode extends Node {
        public Object access(Object receiver) {
            System.err.println("GET_SIZE " + receiver.toString());
            if (receiver instanceof AbstractPointersObject) {
                System.err.println("GET_SIZE is " + ((AbstractPointersObject) receiver).pointers.length);
                return ((AbstractPointersObject) receiver).pointers.length;
            }
            return 0;
        }
    }
    
    
    @Resolve(message = "INVOKE")
    public abstract static class BaseSqueakObjectInvokeNode extends Node {
        public Object access(Object receiver, String name, Object[] arguments) {
            System.err.println("Object>>INVOKE " + receiver.toString() + " " + name.toString());
            return "BaseSqueakObjectInvokeNode";
        }
    }
    
    @Resolve(message = "KEY_INFO")
    public abstract static class BaseSqueakObjectPropertyInfoNode extends Node {
        public int access(Object receiver, Object name) {
            System.err.println("Object>>KEYS_INFO " + receiver.toString() + " " + name.toString());
            return 0;
        }
    }
    
    @Resolve(message = "KEYS")
    public abstract static class BaseSqueakObjectPropertiesNode extends Node {
        public Object access(BaseSqueakObject receiver) {
            System.err.println("BaseSqueakObject>>KEYS " + receiver.getClass().toString() + " " + receiver.toString());
            return new ListObject(SqueakImageContext.SINGLETON, SqueakImageContext.SINGLETON.arrayClass, new Object[] {"a", "b", "c"});
        }
        public Object access(Object receiver) {
            System.err.println("Object>>KEYS " + receiver.toString());
            return receiver;
        }
    }
}
