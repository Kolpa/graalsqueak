package de.hpi.swa.trufflesqueak.nodes.process;

import de.hpi.swa.trufflesqueak.SqueakImageContext;
import de.hpi.swa.trufflesqueak.model.ObjectLayouts.PROCESS_SCHEDULER;
import de.hpi.swa.trufflesqueak.model.PointersObject;

public class GetActiveProcessNode extends AbstractProcessNode {
    @Child private GetSchedulerNode getSchedulerNode;

    public static GetActiveProcessNode create(SqueakImageContext image) {
        return new GetActiveProcessNode(image);
    }

    protected GetActiveProcessNode(SqueakImageContext image) {
        super(image);
        getSchedulerNode = GetSchedulerNode.create(image);
    }

    public PointersObject executeGet() {
        PointersObject scheduler = getSchedulerNode.executeGet();
        return (PointersObject) scheduler.at0(PROCESS_SCHEDULER.ACTIVE_PROCESS);
    }
}