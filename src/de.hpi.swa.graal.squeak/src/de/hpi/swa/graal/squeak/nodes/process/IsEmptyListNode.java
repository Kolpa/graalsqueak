package de.hpi.swa.graal.squeak.nodes.process;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.image.SqueakImageContext;
import de.hpi.swa.graal.squeak.model.AbstractSqueakObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.LINKED_LIST;
import de.hpi.swa.graal.squeak.nodes.AbstractNodeWithImage;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectAt0Node;

public abstract class IsEmptyListNode extends AbstractNodeWithImage {
    @Child private SqueakObjectAt0Node at0Node = SqueakObjectAt0Node.create();

    public static IsEmptyListNode create(final SqueakImageContext image) {
        return IsEmptyListNodeGen.create(image);
    }

    protected IsEmptyListNode(final SqueakImageContext image) {
        super(image);
    }

    public abstract boolean executeIsEmpty(VirtualFrame frame, Object list);

    @Specialization
    protected final boolean executeIsEmpty(final VirtualFrame frame, final AbstractSqueakObject list) {
        return at0Node.execute(frame, list, LINKED_LIST.FIRST_LINK) == image.nil;
    }

    @Fallback
    protected static final boolean doFallback(final Object list) {
        throw new SqueakException("Unexpected list object:", list);
    }
}
