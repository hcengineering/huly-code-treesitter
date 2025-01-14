package com.hulylabs.treesitter.query;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.treesitter.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class Query {
    private static final Logger LOG = Logger.getInstance(Query.class);
    private final TSQuery query;
    private final String[] captureNames;
    private final String[] stringValues;
    private final Pattern[] patterns;

    public static final class QueryCapture {
        private final int captureId;
        private final TSNode node;

        public QueryCapture(int captureId, TSNode node) {
            this.captureId = captureId;
            this.node = node;
        }

        public int captureId() {
            return captureId;
        }

        public TSNode node() {
            return node;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (QueryCapture) obj;
            return this.captureId == that.captureId &&
                    Objects.equals(this.node, that.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(captureId, node);
        }

        @Override
        public String toString() {
            return "QueryCapture[" +
                    "captureId=" + captureId + ", " +
                    "node=" + node + ']';
        }

    }

    private static class Pattern {
        private final QueryPredicate[] predicates;

        public Pattern(int startOffset, int endOffset, QueryPredicate[] predicates) {
            this.predicates = predicates;
        }

        public boolean checkMatch(String text, TSQueryMatch match) {
            if (predicates == null) {
                return true;
            }
            for (QueryPredicate predicate : predicates) {
                if (!predicate.checkMatch(text, match)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class QueryCapturesIterator implements Iterator<QueryCapture>, Iterable<QueryCapture> {
        private final Query query;
        private final String text;
        private final TSTree tree;
        private final TSQueryCursor.TSMatchIterator matchIterator;
        private TSQueryMatch nextSuccessfulMatch;
        private final HashMap<Integer, Boolean> matchesPredicates;

        private QueryCapturesIterator(Query query, TSTree tree, TSNode node, String text, int startOffset, int endOffset) {
            this.query = query;
            TSQueryCursor cursor = new TSQueryCursor();
            cursor.setByteRange(startOffset * 2, endOffset * 2);
            this.text = text;
            this.tree = tree;
            cursor.exec(query.query, node);
            this.matchIterator = cursor.getCaptures();
            this.matchesPredicates = new HashMap<>();
        }

        @Override
        public boolean hasNext() {
            if (nextSuccessfulMatch != null) {
                return true;
            }
            while (matchIterator.hasNext()) {
                nextSuccessfulMatch = matchIterator.next();
                Boolean cachedResult = matchesPredicates.get(nextSuccessfulMatch.getId());
                if (cachedResult != null) {
                    if (cachedResult) {
                        return true;
                    }
                } else {
                    boolean predicateResult = query.checkMatch(text, nextSuccessfulMatch);
                    matchesPredicates.put(nextSuccessfulMatch.getId(), predicateResult);
                    if (predicateResult) {
                        return true;
                    }
                }
            }
            nextSuccessfulMatch = null;
            return false;
        }

        @Override
        public QueryCapture next() {
            while (nextSuccessfulMatch == null && matchIterator.hasNext()) {
                var match = matchIterator.next();
                Boolean cachedResult = matchesPredicates.get(match.getId());
                if (cachedResult != null) {
                    if (cachedResult) {
                        nextSuccessfulMatch = match;
                        break;
                    }
                } else {
                    boolean predicateResult = query.checkMatch(text, match);
                    matchesPredicates.put(match.getId(), predicateResult);
                    if (predicateResult) {
                        nextSuccessfulMatch = match;
                        break;
                    }
                }
            }
            if (nextSuccessfulMatch != null) {
                var match = nextSuccessfulMatch;
                nextSuccessfulMatch = null;
                var capture = match.getCaptures()[match.getCaptureIndex()];
                return new QueryCapture(capture.getIndex(), capture.getNode());
            }
            return null;
        }

        @Override
        public @NotNull Iterator<QueryCapture> iterator() {
            return this;
        }
    }

    public static class QueryMatchesIterator implements Iterator<TSQueryMatch>, Iterable<TSQueryMatch> {
        private final Query query;
        private final TSTree tree;
        private final TSQueryCursor.TSMatchIterator matchIterator;

        private QueryMatchesIterator(Query query, TSTree tree, TSNode node, int startOffset, int endOffset) {
            this.query = query;
            TSQueryCursor cursor = new TSQueryCursor();
            cursor.setByteRange(startOffset * 2, endOffset * 2);
            this.tree = tree;
            cursor.exec(query.query, node);
            this.matchIterator = cursor.getMatches();
        }

        @Override
        public boolean hasNext() {
            return this.matchIterator.hasNext();
        }

        @Override
        public TSQueryMatch next() {
            return this.matchIterator.next();
        }

        @Override
        public @NotNull Iterator<TSQueryMatch> iterator() {
            return this;
        }
    }

    public QueryCapturesIterator getCaptures(TSTree tree, TSNode node, String text) {
        return new QueryCapturesIterator(this, tree, node, text, node.getStartByte() / 2, node.getEndByte() / 2);
    }

    public QueryCapturesIterator getCaptures(TSTree tree, TSNode node, String text, int startOffset, int endOffset) {
        return new QueryCapturesIterator(this, tree, node, text, startOffset, endOffset);
    }

    public QueryMatchesIterator getMatches(TSTree tree, TSNode node, int startOffset, int endOffset) {
        return new QueryMatchesIterator(this, tree, node, startOffset, endOffset);
    }

    public QueryMatchesIterator getMatches(TSTree tree, TSNode node) {
        return new QueryMatchesIterator(this, tree, node, node.getStartByte() / 2, node.getEndByte() / 2);
    }

    private boolean checkMatch(String text, TSQueryMatch match) {
        int patternIndex = match.getPatternIndex();
        if (patternIndex > patterns.length) {
            return false;
        }
        Pattern pattern = patterns[patternIndex];
        return pattern.checkMatch(text, match);
    }

    public Query(TSLanguage language, byte[] query, Map<String, QueryPredicate.Factory> supportedPredicates) throws IllegalArgumentException {
        this.query = new TSQuery(language, query);
        int captureCount = this.query.getCaptureCount();
        this.captureNames = new String[captureCount];
        IntStream.range(0, captureCount).forEach(i -> this.captureNames[i] = this.query.getCaptureNameForId(i));
        int stringCount = this.query.getStringCount();
        this.stringValues = new String[stringCount];
        IntStream.range(0, stringCount).forEach(i -> stringValues[i] = this.query.getStringValueForId(i));
        int patternsCount = this.query.getPatternCount();
        this.patterns = new Pattern[patternsCount];
        for (int i = 0; i < patternsCount; i++) {
            int startOffset = this.query.getStartByteForPattern(i);
            int endOffset = this.query.getEndByteForPattern(i);
            TSQueryPredicateStep[] steps = this.query.getPredicateForPattern(i);
            ArrayList<QueryPredicate> predicates = new ArrayList<>();
            int startPredicate = 0;

            while (startPredicate < steps.length) {
                int endPredicate = startPredicate;
                while (endPredicate < steps.length && steps[endPredicate].getType() != TSQueryPredicateStepType.TSQueryPredicateStepTypeDone) {
                    endPredicate++;
                }
                int nextPredicate = endPredicate < steps.length ? endPredicate + 1 : endPredicate;

                TSQueryPredicateStep startStep = steps[startPredicate];
                if (startStep.getType() != TSQueryPredicateStepType.TSQueryPredicateStepTypeString || startStep.getValueId() > this.stringValues.length) {
                    this.query.disablePattern(i);
                    startPredicate = nextPredicate;
                    continue;
                }

                String predicateName = stringValues[startStep.getValueId()];
                var factory = supportedPredicates.get(predicateName);
                if (factory == null) {
                    this.query.disablePattern(i);
                    startPredicate = nextPredicate;
                    continue;
                }
                QueryPredicate predicate = null;
                try {
                    predicate = factory.tryParse(this, Arrays.copyOfRange(steps, startPredicate, endPredicate));
                } catch (Exception e) {
                    String pattern = new String(query, startOffset, endOffset - startOffset, StandardCharsets.UTF_8);
                    LOG.warn("Error while parsing predicate for pattern " + pattern, e);
                }
                if (predicate == null) {
                    this.query.disablePattern(i);
                    startPredicate = nextPredicate;
                    continue;
                }
                predicates.add(predicate);
                startPredicate = nextPredicate;
            }

            patterns[i] = new Pattern(startOffset, endOffset, predicates.isEmpty() ? null : predicates.toArray(new QueryPredicate[0]));
        }

    }

    public int getCaptureNamesCount() {
        return captureNames.length;
    }

    public String getCaptureName(int captureId) throws IndexOutOfBoundsException {
        return captureNames[captureId];
    }

    public Collection<String> getCaptureNames() {
        return Arrays.asList(captureNames);
    }

    public int getStringValuesCount() {
        return stringValues.length;
    }

    public String getStringValue(int valueId) throws IndexOutOfBoundsException {
        return stringValues[valueId];
    }
}
