package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

class TreeSitterFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val languageTree = TreeSitterStorageUtil.getTreeForTimestamp(document, document.modificationStamp) ?: return emptyArray()
        val foldQuery = languageTree.language.foldQuery ?: return emptyArray()
        val folds = mutableListOf<FoldingDescriptor>()
        for (match in foldQuery.getMatches(languageTree.tree, languageTree.tree.rootNode)) {
            for (capture in match.captures) {
                if (capture.index == languageTree.language.foldCaptureId) {
                    folds.add(FoldingDescriptor(node, TextRange(capture.node.startByte / 2, capture.node.endByte / 2)))
                    break
                }
            }
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
