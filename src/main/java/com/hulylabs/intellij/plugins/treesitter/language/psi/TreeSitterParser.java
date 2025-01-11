package com.hulylabs.intellij.plugins.treesitter.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class TreeSitterParser implements PsiParser {
    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        PsiBuilder.Marker mark = builder.mark();
        while (!builder.eof()) {
            builder.advanceLexer();
        }
        mark.done(root);
        return builder.getTreeBuilt();
    }
}
