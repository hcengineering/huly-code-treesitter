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
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil

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
        val document = editor.document
        val text = document.immutableCharSequence
        val languageTree = TreeSitterStorageUtil.getTreeForTimestamp(document, document.modificationStamp)
            ?: return EnterHandlerDelegate.Result.Continue

        val language = languageTree.language
        if (caretOffset.get() - 1 >= 0 && text[caretOffset.get() - 1] == '\n') {
            caretOffset.set(caretOffset.get() - 1)
        }
        val line = document.getLineNumber(caretOffset.get())
        val query = language.indentQuery ?: return EnterHandlerDelegate.Result.Continue
        val queryStartOffset = DocumentUtil.getLineStartOffset(caretOffset.get(), document)
        val queryEndOffset = DocumentUtil.getLineEndOffset(caretOffset.get(), document)
        val matchesIterator = query.getMatches(languageTree.tree, languageTree.tree.rootNode, queryStartOffset, queryEndOffset)
        val offset = caretOffset.get()
        val leftOffset = CharArrayUtil.shiftBackward(text, offset, " \t")
        val rightOffset = CharArrayUtil.shiftForward(text, offset, " \t")
        for (match in matchesIterator) {
            var startIndent: Int? = null
            var endIndent: Int? = null
            var startLine: Int? = null
            var endLine: Int? = null
            for (capture in match.captures) {
                if (capture.index == language.indentCaptureId) {
                    startIndent = startIndent ?: (capture.node.startByte / 2)
                    endIndent = endIndent ?: (capture.node.endByte / 2)
                    startLine = startLine ?: capture.node.startPoint.row
                    endLine = endLine ?: capture.node.endPoint.row
                } else if (capture.index == language.indentStartCaptureId) {
                    startIndent = capture.node.endByte / 2
                    startLine = capture.node.endPoint.row
                } else if (capture.index == language.indentEndCaptureId) {
                    endIndent = capture.node.startByte / 2
                    endLine = capture.node.startPoint.row
                }
            }
            if (startIndent == null || endIndent == null) {
                continue
            }
            if (startLine == line && endLine == line && startIndent == leftOffset && rightOffset == endIndent) {
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
