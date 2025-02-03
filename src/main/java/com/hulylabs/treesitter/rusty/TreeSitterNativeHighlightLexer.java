package com.hulylabs.treesitter.rusty;

import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TreeSitterNativeHighlightLexer {
    public record Token(int startOffset, int endOffset, short captureId, short nodeKind) {
    }

    public static class Tokens implements Iterable<Token> {
        private final int startOffset;
        private final int[] tokenLengths;
        private final short[] nodeKinds;
        private final short[] captureIds;

        Tokens(int startOffset, int[] tokenLengths, short[] nodeKinds, short[] captureIds) {
            this.startOffset = startOffset;
            this.tokenLengths = tokenLengths;
            this.nodeKinds = nodeKinds;
            this.captureIds = captureIds;
        }

        @Override
        public @NotNull Iterator<Token> iterator() {
            return new Iterator<>() {
                private int index = 0;
                private int tokenOffset = startOffset;

                @Override
                public boolean hasNext() {
                    return index < tokenLengths.length;
                }

                @Override
                public Token next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    var token = new Token(tokenOffset, tokenOffset + tokenLengths[index], captureIds[index], nodeKinds[index]);
                    tokenOffset += tokenLengths[index];
                    index++;
                    return token;
                }
            };
        }
    }

    private static native Tokens nativeCollectHighlights(TreeSitterNativeSyntaxSnapshot snapshot, char[] text, int startOffset, int endOffset);

    public static Tokens collectHighlights(@NotNull TreeSitterNativeSyntaxSnapshot snapshot, @NotNull CharSequence text, int startOffset, int endOffset) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeCollectHighlights(snapshot, chars, startOffset, endOffset);
    }
}
