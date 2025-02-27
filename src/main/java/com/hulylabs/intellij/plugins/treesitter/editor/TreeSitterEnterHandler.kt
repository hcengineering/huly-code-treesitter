package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterFileType
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.EnterHandler
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil

class TreeSitterEnterHandler : EnterHandlerDelegate {
    override fun invokeInsideIndent(newLineCharOffset: Int, editor: Editor, dataContext: DataContext): Boolean {
        val language = EnterHandler.getLanguage(dataContext)
        if (language != TreeSitterLanguage.INSTANCE) {
            return false
        }
        if (editor.document.immutableCharSequence.get(newLineCharOffset) == '\n') {
            return true
        }
        return super.invokeInsideIndent(newLineCharOffset, editor, dataContext)
    }

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (file.fileType !== TreeSitterFileType.INSTANCE) {
            return EnterHandlerDelegate.Result.Continue
        }
        if (!CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
            return EnterHandlerDelegate.Result.Continue
        }
        val document = editor.document as? DocumentEx ?: return EnterHandlerDelegate.Result.Continue
        val text = document.immutableCharSequence
        val snapshot = TreeSitterStorageUtil.getSnapshotForTimestamp(document, document.modificationSequence)
            ?: return EnterHandlerDelegate.Result.Continue

        val originalOffset = caretOffset.get()
        val originalLineStart = document.getLineStartOffset(document.getLineNumber(originalOffset))
        if (originalOffset - 1 >= originalLineStart && text[originalOffset - 1] == '\n') {
            caretOffset.set(originalOffset - 1)
        }
        val offset = caretOffset.get()
        val line = document.getLineNumber(offset)
        val searchStartOffset = DocumentUtil.getLineStartOffset(offset, document)
        val searchEndOffset = DocumentUtil.getLineEndOffset(offset, document)

        for (range in snapshot.getIndentRanges(text, searchStartOffset, searchEndOffset)) {
            if (range.startPoint.row == line && range.endPoint.row == line && range.startOffset <= offset && offset < range.endOffset) {
                originalHandler?.execute(editor, editor.caretModel.currentCaret, dataContext)
                return EnterHandlerDelegate.Result.DefaultForceIndent
            }
        }

        return EnterHandlerDelegate.Result.Continue
    }

    override fun postProcessEnter(
        file: PsiFile,
        editor: Editor,
        dataContext: DataContext
    ): EnterHandlerDelegate.Result? {
        return null
    }
}
