package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.editor.TreeSitterHighlightingColors;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.rusty.TreeSitterNativeHighlightLexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TreeSitterSyntaxHighlighter {
    private static final TextAttributesKey[] EMPTY_ARRAY = new TextAttributesKey[0];
    private final Language language;
    private final Map<Short, TextAttributesKey[]> attributes;
    private final Map<Short, IElementType> nativeAttributes = new HashMap<>();

    public TreeSitterSyntaxHighlighter(Language language) {
        super();
        this.language = language;

        TreeSitterHighlightingColors colors = TreeSitterHighlightingColors.getInstance();
        this.attributes = new HashMap<>();
        String[] nativeHighlights = language.getNativeHighlights();
        if (nativeHighlights != null) {
            short index = 0;
            for (String captureName : language.getNativeHighlights()) {
                var elementType = TreeSitterCaptureElementType.findOrCreate(captureName);
                nativeAttributes.put(index, elementType);
                if (!attributes.containsKey(elementType.getGroupId())) {
                    attributes.put(elementType.getGroupId(), new TextAttributesKey[]{colors.getTextAttributesKey(captureName)});
                }
                index++;
            }
        }
    }

    public Language getLanguage() {
        return language;
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType instanceof TreeSitterCaptureElementType) {
            TreeSitterCaptureElementType treeSitterElementType = (TreeSitterCaptureElementType) tokenType;
            TextAttributesKey[] symbolAttributes = this.attributes.get(treeSitterElementType.getGroupId());
            if (symbolAttributes != null) {
                return symbolAttributes;
            }
        }
        return EMPTY_ARRAY;
    }

    public IElementType getTokenType(TreeSitterNativeHighlightLexer.Token token) {
        if (token.captureId() < 0) {
            return TreeSitterCaptureElementType.NONE;
        }
        if (nativeAttributes.containsKey(token.captureId())) {
            return nativeAttributes.get(token.captureId());
        } else {
            var nativeHighlights = language.getNativeHighlights();
            if (nativeHighlights != null && nativeHighlights.length > token.captureId()) {
                TreeSitterCaptureElementType elementType = TreeSitterCaptureElementType.findOrCreate(nativeHighlights[token.captureId()]);
                nativeAttributes.put(token.captureId(), elementType);
                if (!attributes.containsKey(elementType.getGroupId())) {
                    TreeSitterHighlightingColors colors = TreeSitterHighlightingColors.getInstance();
                    attributes.put(elementType.getGroupId(), new TextAttributesKey[]{colors.getTextAttributesKey(nativeHighlights[token.captureId()])});
                }
                return elementType;
            } else {
                return TreeSitterCaptureElementType.NONE;
            }
        }
    }
}
