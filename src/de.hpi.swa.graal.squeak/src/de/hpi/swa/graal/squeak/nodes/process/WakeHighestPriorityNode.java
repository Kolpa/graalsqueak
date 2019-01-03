package de.hpi.swa.graal.squeak.nodes.process;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import de.hpi.swa.graal.squeak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.PROCESS_SCHEDULER;
import de.hpi.swa.graal.squeak.model.PointersObject;
import de.hpi.swa.graal.squeak.nodes.AbstractNodeWithImage;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectAt0Node;
import de.hpi.swa.graal.squeak.nodes.accessing.SqueakObjectSizeNode;

public final class WakeHighestPriorityNode extends AbstractNodeWithImage {
    private final BranchProfile errorProfile = BranchProfile.create();
    @Child private SqueakObjectAt0Node at0Node = SqueakObjectAt0Node.create();
    @Child private SqueakObjectSizeNode sizeNode = SqueakObjectSizeNode.create();
    @Child private RemoveFirstLinkOfListNode removeFirstLinkOfListNode;
    @Child private GetActiveProcessNode getActiveProcessNode;
    @Child private IsEmptyListNode isEmptyListNode;
    @Child private TransferToNode transferToNode;

    public static WakeHighestPriorityNode create(final CompiledCodeObject code) {
        return new WakeHighestPriorityNode(code);
    }

    protected WakeHighestPriorityNode(final CompiledCodeObject code) {
        super(code.image);
        removeFirstLinkOfListNode = RemoveFirstLinkOfListNode.create(image);
        getActiveProcessNode = GetActiveProcessNode.create(image);
        isEmptyListNode = IsEmptyListNode.create(image);
        transferToNode = TransferToNode.create(code);
    }

    public void executeWake(final VirtualFrame frame) {
        // Return the highest priority process that is ready to run.
        // Note: It is a fatal VM error if there is no runnable process.
        final Object schedLists = image.getScheduler().at0(PROCESS_SCHEDULER.PROCESS_LISTS);
        long p = sizeNode.execute(schedLists) - 1;  // index of last indexable field
        Object processList;
        do {
            if (p < 0) {
                errorProfile.enter();
                throw new SqueakException("scheduler could not find a runnable process");
            }
            processList = at0Node.execute(frame, schedLists, p--);
        } while (isEmptyListNode.executeIsEmpty(frame, processList));
        final PointersObject activeProcess = getActiveProcessNode.executeGet();
        final Object newProcess = removeFirstLinkOfListNode.executeRemove(frame, processList);
        transferToNode.executeTransferTo(frame, activeProcess, newProcess);
    }
}
