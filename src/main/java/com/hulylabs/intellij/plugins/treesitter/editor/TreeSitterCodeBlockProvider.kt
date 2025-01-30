package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.treesitter.language.SyntaxSnapshot
import com.intellij.codeInsight.editorActions.CodeBlockProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class TreeSitterCodeBlockProvider : CodeBlockProvider {
    override fun getCodeBlockRange(editor: Editor, psiFile: PsiFile): TextRange? {
        val languageTree =
            TreeSitterStorageUtil.getSnapshotForTimestamp(editor.document, editor.document.modificationStamp) ?: return null
        val snapshot = SyntaxSnapshot(languageTree.tree, languageTree.language, editor.document.modificationStamp)
        val offset = editor.caretModel.offset
        var minRange: TextRange? = null
        for (range in snapshot.getFoldRanges(offset.toInt(), offset.toInt()) ?: return null) {
            val newRange = TextRange(range.startOffset, range.endOffset)
            if (minRange == null || minRange.contains(newRange)) {
                minRange = newRange
                continue
            }
        }

        return minRange
    }
}
