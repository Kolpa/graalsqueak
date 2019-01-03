package de.hpi.swa.graal.squeak.nodes.process;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.graal.squeak.exceptions.PrimitiveExceptions.PrimitiveFailed;
import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.model.AbstractSqueakObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.LINKED_LIST;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.PROCESS;
import de.hpi.swa.graal.squeak.nodes.AbstractNodeWithImage;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectAt0Node;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectAtPut0Node;
import de.hpi.swa.graal.squeak.nodes.process.RemoveProcessFromListNodeGen.ExecuteRemoveProcessNodeGen;

public abstract class RemoveProcessFromListNode extends AbstractNodeWithImage {
    @Child private SqueakObjectAtPut0Node atPut0Node = SqueakObjectAtPut0Node.create();
    @Child private SqueakObjectAt0Node at0Node = SqueakObjectAt0Node.create();
    @Child private ExecuteRemoveProcessNode removeNode;

    public static RemoveProcessFromListNode create(final SqueakImageContext image) {
        return RemoveProcessFromListNodeGen.create(image);
    }

    protected RemoveProcessFromListNode(final SqueakImageContext image) {
        super(image);
        removeNode = ExecuteRemoveProcessNodeGen.create(image);
    }

    public abstract void executeRemove(VirtualFrame frame, Object process, Object list);

    protected abstract static class ExecuteRemoveProcessNode extends AbstractNodeWithImage {
        @Child private SqueakObjectAtPut0Node atPut0Node = SqueakObjectAtPut0Node.create();
        @Child private SqueakObjectAt0Node at0Node = SqueakObjectAt0Node.create();

        protected ExecuteRemoveProcessNode(final SqueakImageContext image) {
            super(image);
        }

        protected abstract void execute(VirtualFrame frame, AbstractSqueakObject process, AbstractSqueakObject list, Object first, Object last);

        @Specialization(guards = "process == first")
        protected final void doRemoveEqual(final VirtualFrame frame, final AbstractSqueakObject process, final AbstractSqueakObject list, @SuppressWarnings("unused") final AbstractSqueakObject first,
                        final AbstractSqueakObject last) {
            final Object next = at0Node.execute(frame, process, PROCESS.NEXT_LINK);
            atPut0Node.execute(frame, list, LINKED_LIST.FIRST_LINK, next);
            if (process == last) {
                atPut0Node.execute(frame, list, LINKED_LIST.LAST_LINK, image.nil);
            }
        }

        @Fallback
        protected final void doRemoveNotEqual(final VirtualFrame frame, final AbstractSqueakObject process, final AbstractSqueakObject list, final Object first, final Object last) {
            Object temp = first;
            Object next;
            while (true) {
                if (temp == image.nil) {
                    throw new PrimitiveFailed();
                }
                next = at0Node.execute(frame, temp, PROCESS.NEXT_LINK);
                if (next == process) {
                    break;
                }
                temp = next;
            }
            next = at0Node.execute(frame, process, PROCESS.NEXT_LINK);
            atPut0Node.execute(frame, temp, PROCESS.NEXT_LINK, next);
            if (process == last) {
                atPut0Node.execute(frame, list, LINKED_LIST.LAST_LINK, temp);
            }
        }
    }

    @Specialization
    protected final void executeRemove(final VirtualFrame frame, final AbstractSqueakObject process, final AbstractSqueakObject list) {
        final Object first = at0Node.execute(frame, list, LINKED_LIST.FIRST_LINK);
        final Object last = at0Node.execute(frame, list, LINKED_LIST.LAST_LINK);
        removeNode.execute(frame, process, list, first, last);
        atPut0Node.execute(frame, process, PROCESS.NEXT_LINK, image.nil);
    }

    @Fallback
    protected static final void doFallback(final Object process, final Object list) {
        throw new SqueakException("Unexpected process and list:", process, "and", list);
    }
}
