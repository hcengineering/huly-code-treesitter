package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.codeInsight.editorActions.CodeBlockProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class TreeSitterCodeBlockProvider : CodeBlockProvider {
    override fun getCodeBlockRange(editor: Editor, psiFile: PsiFile): TextRange? {
        val document = editor.document as? DocumentEx ?: return null
        val snapshot =
            TreeSitterStorageUtil.getSnapshotForTimestamp(document, document.modificationSequence)
                ?: return null
        val offset = editor.caretModel.offset
        var minRange: TextRange? = null
        val text = document.immutableCharSequence
        for (range in snapshot.getFoldRanges(text, offset, offset)) {
            val newRange = TextRange(range.range.startOffset, range.range.endOffset)
            if (minRange == null || minRange.contains(newRange)) {
                minRange = newRange
                continue
            }
        }

        return minRange
    }
}
