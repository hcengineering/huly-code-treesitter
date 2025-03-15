package com.hulylabs.treesitter.rusty;


import com.hulylabs.treesitter.language.FoldRange;
import com.hulylabs.treesitter.language.Point;
import com.hulylabs.treesitter.language.Range;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TreeSitterNativeRangesProvider {
    public static Range @NotNull [] getIndentRanges(@NotNull TreeSitterNativeSyntaxSnapshot snapshot, @NotNull CharSequence text, int startOffset, int endOffset, boolean useInner) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeGetIndentRanges(snapshot, chars, startOffset, endOffset, useInner);
    }

    public static FoldRange @NotNull [] getFoldRanges(@NotNull TreeSitterNativeSyntaxSnapshot snapshot, @NotNull CharSequence text, int startOffset, int endOffset, boolean useInner) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeGetFoldRanges(snapshot, chars, startOffset, endOffset, useInner);
    }

    private static native Range[] nativeGetIndentRanges(TreeSitterNativeSyntaxSnapshot snapshot, char[] text, int startOffset, int endOffset, boolean useInner);

    private static native FoldRange[] nativeGetFoldRanges(TreeSitterNativeSyntaxSnapshot snapshot, char[] text, int startOffset, int endOffset, boolean useInner);

    public static class Ranges implements Iterable<Range> {
        private final int[] startOffsets;
        private final int[] endOffsets;
        private final Point[] startPoints;
        private final Point[] endPoints;

        public Ranges(int[] startOffsets, int[] endOffsets, Point[] startPoints, Point[] endPoints) {
            this.startOffsets = startOffsets;
            this.endOffsets = endOffsets;
            this.startPoints = startPoints;
            this.endPoints = endPoints;
        }

        @Override
        public @NotNull Iterator<Range> iterator() {
            return new Iterator<Range>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < startOffsets.length;
                }

                @Override
                public Range next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Range range = new Range(startOffsets[index], endOffsets[index], startPoints[index], endPoints[index]);
                    index++;
                    return range;
                }
            };
        }
    }
}
