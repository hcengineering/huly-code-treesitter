package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.codeInsight.editorActions.CodeBlockProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class TreeSitterCodeBlockProvider : CodeBlockProvider {
    override fun getCodeBlockRange(editor: Editor, psiFile: PsiFile): TextRange? {
        val snapshot =
            TreeSitterStorageUtil.getSnapshotForTimestamp(editor.document, editor.document.modificationStamp)
                ?: return null
        val offset = editor.caretModel.offset
        var minRange: TextRange? = null
        val text = editor.document.immutableCharSequence
        for (range in snapshot.getFoldRanges(text, offset, offset)) {
            val newRange = TextRange(range.startOffset, range.endOffset)
            if (minRange == null || minRange.contains(newRange)) {
                minRange = newRange
                continue
            }
        }

        return minRange
    }
}
