package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.util.TextRange

class TreeSitterFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val documentEx = document as? DocumentEx ?: return emptyArray()
        val snapshot =
            TreeSitterStorageUtil.getSnapshotForTimestamp(documentEx, documentEx.modificationSequence) ?: return emptyArray()
        val folds = mutableListOf<FoldingDescriptor>()
        for (range in snapshot.getFoldRanges(documentEx.immutableCharSequence, 0, documentEx.textLength, false)) {
            if (range.range.startPoint.row == range.range.endPoint.row) {
                continue
            }
            folds.add(
                FoldingDescriptor(
                    node,
                    TextRange(range.range.startOffset, range.range.endOffset),
                    null,
                    emptySet(),
                    false,
                    range.collapsedText,
                    range.collapsedByDefault,
                )
            )
        }
        return folds.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
