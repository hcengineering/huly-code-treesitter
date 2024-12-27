package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.editor.TreeSitterHighlightingColors;
import com.hulylabs.treesitter.language.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TreeSitterSyntaxHighlighter extends SyntaxHighlighterBase {
    private final Language language;
    private final TextAttributesKey[][] attributes;
    private final short[] symbolHighlightMap;
    private TreeSitterLexer lexer;

    public TreeSitterSyntaxHighlighter(Language language) {
        super();
        this.language = language;

        Set<String> captureNames = language.getCaptureNames();
        this.attributes = new TextAttributesKey[captureNames.size()][];
        TreeSitterHighlightingColors colors = TreeSitterHighlightingColors.getInstance();
        HashMap<String, Short> highlightsIndexes = new HashMap<>();
        short idx = 0;
        for (String captureName : captureNames) {
            highlightsIndexes.put(captureName, idx);
            attributes[idx++] = new TextAttributesKey[]{colors.getTextAttributesKey(captureName)};
        }
        this.symbolHighlightMap = new short[language.getVisibleSymbolCount()];
        Arrays.fill(symbolHighlightMap, (short) -1);
        for (Map.Entry<Integer, String> entry : language.getHighlights().entrySet()) {
            symbolHighlightMap[entry.getKey()] = highlightsIndexes.get(entry.getValue());
        }
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        if (lexer == null) {
            lexer = new TreeSitterLexer(language, symbolHighlightMap);
        }
        return lexer;
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType instanceof TreeSitterElementType) {
            TreeSitterElementType treeSitterElementType = (TreeSitterElementType) tokenType;
            if (treeSitterElementType.isNodeStart()) {
                return new TextAttributesKey[0];
            }
            int tokenSymbol = treeSitterElementType.getTreeSitterSymbol();
            short symbolHighlight = symbolHighlightMap[tokenSymbol];
            if (symbolHighlight != -1) {
                return this.attributes[symbolHighlight];
            }
        }
        return new TextAttributesKey[0];
    }
}
