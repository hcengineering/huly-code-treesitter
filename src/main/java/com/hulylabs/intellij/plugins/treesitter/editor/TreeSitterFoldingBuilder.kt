package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.intellij.plugins.treesitter.language.psi.TreeSitterFile
import com.hulylabs.treesitter.language.LanguageRegistry
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

class TreeSitterFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val tree = TreeSitterStorageUtil.getTreeForTimestamp(document, document.modificationStamp) ?: return emptyArray()
        val psiFile = node.psi.containingFile as? TreeSitterFile ?: return emptyArray()
        val tsLanguage = psiFile.virtualFile.extension?.let {
            ApplicationManager.getApplication().getService(LanguageRegistry::class.java).getLanguage(it)
        } ?: return emptyArray()
        val foldQuery = tsLanguage.foldQuery ?: return emptyArray()
        val folds = mutableListOf<FoldingDescriptor>()
        for (match in foldQuery.getMatches(tree, tree.rootNode)) {
            for (capture in match.captures) {
                if (capture.index == tsLanguage.foldCaptureId) {
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
