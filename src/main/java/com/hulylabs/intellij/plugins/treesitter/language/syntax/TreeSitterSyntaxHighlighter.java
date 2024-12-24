package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.editor.TreeSitterHighlightingColors;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.LanguageSymbol;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeSitterSyntaxHighlighter extends SyntaxHighlighterBase {
    private final Language language;
    private final TextAttributesKey[][] attributes;
    private final Set<Integer> knownSymbols;
    private TreeSitterLexer lexer;

    public TreeSitterSyntaxHighlighter(Language language, Map<LanguageSymbol, String> highlights) {
        super();
        this.language = language;
        this.attributes = new TextAttributesKey[language.getVisibleSymbolCount()][];
        this.knownSymbols = new HashSet<>();
        TreeSitterHighlightingColors colors = TreeSitterHighlightingColors.getInstance();
        for (LanguageSymbol symbol : highlights.keySet()) {
            int symbolId = language.symbolForName(symbol.getName(), symbol.isNamed());
            this.knownSymbols.add(symbolId);
            attributes[symbolId] = new TextAttributesKey[]{colors.getTextAttributesKey(highlights.get(symbol))};
        }
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        if (lexer == null) {
            lexer = new TreeSitterLexer(language, knownSymbols);
        }
        return lexer;
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType instanceof TreeSitterElementType) {
            TreeSitterElementType treeSitterElementType = (TreeSitterElementType) tokenType;
            int tokenSymbol = treeSitterElementType.getTreeSitterSymbol();
            TextAttributesKey[] symbolAttributes;
            symbolAttributes = this.attributes[tokenSymbol];
            if (!treeSitterElementType.isNodeStart() && symbolAttributes != null) {
                return symbolAttributes;
            }
        }
        return new TextAttributesKey[0];
    }

    public Set<Integer> getKnownSymbols() {
        return IntStream.range(0, this.attributes.length).filter(j -> this.attributes[j] != null).boxed().collect(Collectors.toSet());
    }
}
