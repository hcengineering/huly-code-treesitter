package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.treesitter.language.SyntaxSnapshot
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

class TreeSitterFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val languageTree = TreeSitterStorageUtil.getSnapshotForTimestamp(document, document.modificationStamp) ?: return emptyArray()
        val folds = mutableListOf<FoldingDescriptor>()
        val snapshot = SyntaxSnapshot(languageTree.tree, languageTree.language, document.modificationStamp)
        for (range in snapshot.getFoldRanges(0, document.textLength, false) ?: return emptyArray()) {
            if (range.startOffset == range.endOffset) {
                continue
            }
            folds.add(FoldingDescriptor(node, TextRange(range.startOffset, range.endOffset)))
        }

        return folds.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "{ ... }"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
