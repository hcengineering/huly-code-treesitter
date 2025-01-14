package com.hulylabs.treesitter.query;

import org.treesitter.TSNode;
import org.treesitter.TSQueryMatch;
import org.treesitter.TSQueryPredicateStep;
import org.treesitter.TSQueryPredicateStepType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class QueryPredicate {
    protected WeakReference<Query> query;

    public QueryPredicate(Query query) {
        this.query = new WeakReference<>(query);
    }

    protected List<TSNode> getCaptureNodes(TSQueryMatch match, int captureId) {
        List<TSNode> captureNodes = new ArrayList<>();
        for (var capture : match.getCaptures()) {
            if (capture.getIndex() == captureId) {
                captureNodes.add(capture.getNode());
            }
        }
        return captureNodes;
    }

    public abstract String getName();

    public abstract boolean checkMatch(String text, TSQueryMatch match);

    public abstract static class Factory {
        protected static boolean isValidCaptureStep(Query query, TSQueryPredicateStep step) {
            return step.getType() == TSQueryPredicateStepType.TSQueryPredicateStepTypeCapture && step.getValueId() <= query.getCaptureNamesCount();
        }

        protected static boolean isValidStringStep(Query query, TSQueryPredicateStep step) {
            return step.getType() == TSQueryPredicateStepType.TSQueryPredicateStepTypeString && step.getValueId() <= query.getStringValuesCount();
        }

        public abstract QueryPredicate tryParse(Query query, TSQueryPredicateStep[] steps);
    }
}
