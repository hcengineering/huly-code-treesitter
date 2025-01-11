package com.hulylabs.intellij.plugins.treesitter.language.psi;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;

public class TreeSitterPsiElement extends CompositePsiElement {
    public TreeSitterPsiElement(IElementType type) {
        super(type);
    }
}
