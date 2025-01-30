package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.treesitter.language.TreeSitterTreeWalkLexer;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.SyntaxSnapshot;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

public class TreeSitterLexer {
    private final TreeSitterCaptureElementType[] symbolElementMap;
    private final TreeSitterTreeWalkLexer treeWalkLexer;
    private final Language language;

    public TreeSitterLexer(Language language, TreeSitterCaptureElementType[] symbolElementMap) {
        this.language = language;
        this.symbolElementMap = symbolElementMap;
        boolean [] highlightedSymbols = new boolean[symbolElementMap.length];
        for (int i = 0; i < symbolElementMap.length; i++) {
            highlightedSymbols[i] = symbolElementMap[i] != null;
        }
        this.treeWalkLexer = new TreeSitterTreeWalkLexer(language, highlightedSymbols);
    }

    public void start(SyntaxSnapshot snapshot, int startOffset, int endOffset) {
        treeWalkLexer.start(snapshot, startOffset, endOffset);
    }

    public @Nullable IElementType getTokenType() {
        var tokenType = treeWalkLexer.getTokenType();
        if (tokenType != null) {
            return getElementType(tokenType);
        } else {
            return null;
        }
    }

    public int getTokenStart() {
        return treeWalkLexer.getTokenStart();
    }

    public int getTokenEnd() {
        return treeWalkLexer.getTokenEnd();
    }

    private IElementType getElementType(int symbol) {
        if (symbol == Language.ERROR_SYMBOL) {
            return TokenType.BAD_CHARACTER;
        } else if (symbol == Language.WHITESPACE_SYMBOL) {
            return TokenType.WHITE_SPACE;
        } else if (symbol == -1) {
            return TreeSitterCaptureElementType.NONE;
        }
        int id = language.getVisibleSymbolId(symbol);
        if (symbolElementMap[id] != null) {
            return symbolElementMap[id];
        }
        return TreeSitterCaptureElementType.NONE;
    }

    public void advance() {
        treeWalkLexer.advance();
    }
}
