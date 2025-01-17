package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.codeInsight.editorActions.CodeBlockProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class TreeSitterCodeBlockProvider : CodeBlockProvider {
    override fun getCodeBlockRange(editor: Editor, psiFile: PsiFile): TextRange? {
        val languageTree =
            TreeSitterStorageUtil.getTreeForTimestamp(editor.document, editor.document.modificationStamp) ?: return null
        val offset = editor.caretModel.offset
        val query = languageTree.language.foldQuery ?: return null
        var minRange: TextRange? = null
        for (match in query.getMatches(languageTree.tree, languageTree.tree.rootNode, offset, offset)) {
            var startOffset: Int? = null
            var endOffset: Int? = null
            for (capture in match.captures) {
                if (capture.index == languageTree.language.foldCaptureId) {
                    startOffset = startOffset ?: (capture.node.startByte / 2)
                    endOffset = endOffset ?: (capture.node.endByte / 2)
                } else if (capture.index == languageTree.language.foldStartCaptureId) {
                    startOffset = capture.node.endByte / 2
                } else if (capture.index == languageTree.language.foldEndCaptureId) {
                    endOffset = capture.node.startByte / 2
                }
            }
            if (startOffset == null || endOffset == null) {
                continue
            }
            val newRange = TextRange(startOffset, endOffset)
            if (minRange == null || minRange.contains(newRange)) {
                minRange = newRange
                continue
            }
        }

        return minRange
    }
}
