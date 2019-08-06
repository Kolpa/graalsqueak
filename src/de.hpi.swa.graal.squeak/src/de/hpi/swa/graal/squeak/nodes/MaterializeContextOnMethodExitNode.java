package de.hpi.swa.graal.squeak.nodes;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.ContextObject;

public abstract class MaterializeContextOnMethodExitNode extends AbstractNodeWithCode {
    protected MaterializeContextOnMethodExitNode(final CompiledCodeObject code) {
        super(code);
    }

    public static MaterializeContextOnMethodExitNode create(final CompiledCodeObject code) {
        return MaterializeContextOnMethodExitNodeGen.create(code);
    }

    public abstract void execute(VirtualFrame frame);

    @Specialization
    protected final void doMaterialize(final VirtualFrame frame,
                    @Cached("createBinaryProfile()") final ConditionProfile lastSeenContextNullProfile,
                    @Cached("create(code)") final GetOrCreateContextNode getOrCreateContextNode) {
        final ContextObject lastSeenContext = code.image.lastSeenContext;
        final ContextObject context = getOrCreateContextNode.executeGet(frame);
        context.markEscaped();
        if (lastSeenContextNullProfile.profile(lastSeenContext == null)) {
            code.image.lastSeenContext = context;
        } else {
            assert context != lastSeenContext;
            assert context.hasTruffleFrame();
            if (lastSeenContext != null && !lastSeenContext.hasMaterializedSender()) {
                lastSeenContext.setSender(context);
            }
            if (!context.isTerminated() && context.hasEscaped()) {
                // Materialization needs to continue in parent frame.
                code.image.lastSeenContext = context;
            } else {
                // If context has not escaped, materialization can terminate here.
                code.image.lastSeenContext = null;
            }
        }
    }
}
