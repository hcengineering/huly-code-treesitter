package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.editor.TreeSitterHighlightingColors;
import com.hulylabs.treesitter.language.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TreeSitterSyntaxHighlighter {
    private final Language language;
    private final Map<Short, TextAttributesKey[]> attributes;
    private final TreeSitterCaptureElementType[] symbolElementMap;
    private TreeSitterLexer lexer;

    public TreeSitterSyntaxHighlighter(Language language) {
        super();
        this.language = language;

        TreeSitterHighlightingColors colors = TreeSitterHighlightingColors.getInstance();
        this.attributes = new HashMap<>();
        this.symbolElementMap = new TreeSitterCaptureElementType[language.getVisibleSymbolCount()];
        for (Map.Entry<Integer, String> entry : language.getHighlights().entrySet()) {
            var elementType = TreeSitterCaptureElementType.findOrCreate(entry.getValue());
            symbolElementMap[entry.getKey()] = elementType;
            if (!attributes.containsKey(elementType.getGroupId())) {
                attributes.put(elementType.getGroupId(), new TextAttributesKey[]{colors.getTextAttributesKey(entry.getValue())});
            }
        }
    }

    public @NotNull TreeSitterLexer getHighlightingLexer() {
        if (lexer == null) {
            lexer = new TreeSitterLexer(language, symbolElementMap);
        }
        return lexer;
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType instanceof TreeSitterCaptureElementType) {
            TreeSitterCaptureElementType treeSitterElementType = (TreeSitterCaptureElementType) tokenType;
            TextAttributesKey[] symbolAttributes = this.attributes.get(treeSitterElementType.getGroupId());
            if (symbolAttributes != null) {
                return symbolAttributes;
            }
        }
        return new TextAttributesKey[0];
    }
}
