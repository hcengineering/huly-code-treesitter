package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage;
import com.intellij.psi.tree.IElementType;

import java.util.Objects;

public class TreeSitterElementType extends IElementType {
    private final int treeSitterSymbol;
    private final boolean isNodeStart;
    private final boolean isNodeEnd;
    private final int depth;
    public TreeSitterElementType(int treeSitterSymbol, boolean isNodeStart, boolean isNodeEnd, int depth) {
        super("TREE_SITTER_TOKEN", TreeSitterLanguage.INSTANCE);
        this.treeSitterSymbol = treeSitterSymbol;
        this.isNodeStart = isNodeStart;
        this.isNodeEnd = isNodeEnd;
        this.depth = depth;
    }

    public int getTreeSitterSymbol() {
        return treeSitterSymbol;
    }

    public boolean isNodeStart() {
        return isNodeStart;
    }

    public boolean isNodeEnd() {
        return isNodeEnd;
    }

    public int getDepth() {
        return depth;
    }

    public boolean equals(int pTreeSitterSymbol, boolean pIsNodeStart, boolean pIsNodeEnd) {
        return isNodeStart == pIsNodeStart && isNodeEnd == pIsNodeEnd && treeSitterSymbol == pTreeSitterSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TreeSitterElementType that = (TreeSitterElementType) o;
        return isNodeStart == that.isNodeStart && isNodeEnd == that.isNodeEnd && depth == that.depth && treeSitterSymbol == that.treeSitterSymbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), treeSitterSymbol, isNodeStart, isNodeEnd, depth);
    }

    @Override
    public String toString() {
        return "TreeSitterElementType{" +
                "treeSitterSymbol='" + treeSitterSymbol + '\'' +
                ", isNodeStart=" + isNodeStart +
                ", isNodeEnd=" + isNodeEnd +
                ", depth=" + depth +
                '}';
    }
}
