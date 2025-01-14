package com.hulylabs.intellij.plugins.treesitter.editor;

import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterCaptureElementType;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeSitterPairedBraceMatcher implements PairedBraceMatcher {
    private final BracePair[] bracePairs;

    TreeSitterPairedBraceMatcher() {
        bracePairs = new BracePair[]{
                new BracePair(TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.left"), TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.right"), true),
                new BracePair(TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.braces.left"), TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.braces.right"), true),
                new BracePair(TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.parentheses.left"), TreeSitterCaptureElementType.findOrCreate("punctuation.bracket.parentheses.right"), true),
        };
    }

    @Override
    public BracePair @NotNull [] getPairs() {
        return bracePairs;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
