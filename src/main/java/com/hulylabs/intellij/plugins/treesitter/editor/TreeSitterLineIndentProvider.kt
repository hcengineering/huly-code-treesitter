package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage
import com.hulylabs.treesitter.language.LanguageRegistry
import com.intellij.application.options.CodeStyle
import com.intellij.formatting.IndentInfo
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil
import org.treesitter.TSPoint
import kotlin.math.min

private operator fun TSPoint.compareTo(other: TSPoint): Int {
    if (row == other.row) {
        return column.compareTo(other.column)
    } else {
        return row.compareTo(other.row)
    }
}

private fun TSPoint.toCharOffsets(): TSPoint {
    return TSPoint(row, column / 2)
}

data class IndentRange(val start: TSPoint, var end: TSPoint) : Comparable<IndentRange> {
    override fun compareTo(other: IndentRange): Int {
        return start.compareTo(other.start)
    }

    fun contains(point: TSPoint): Boolean {
        return (start <= point && point < end)
    }
}

private fun max(a: TSPoint, b: TSPoint): TSPoint {
    return if (a > b) {
        a
    } else {
        b
    }
}

private fun getIndentInfo(
    document: Document, line: Int, indentSpaces: Int, tabSize: Int
): IndentInfo {
    val text = document.immutableCharSequence
    val lineStartOffset = document.getLineStartOffset(line)
    val lineEndOffset = document.getLineEndOffset(line)
    val lineIndentOffset = CharArrayUtil.shiftForward(text, lineStartOffset, lineEndOffset, " \t")
    val spacesSize = text.substring(lineStartOffset, lineIndentOffset)
        .replace("\t", StringUtil.repeatSymbol(' ', tabSize)).length + indentSpaces
    return IndentInfo(0, spacesSize, 0, false)
}

class TreeSitterLineIndentProvider : LineIndentProvider {
    override fun getLineIndent(project: Project, editor: Editor, language: Language?, offset: Int): String? {
        val tree =
            TreeSitterStorageUtil.getTreeForTimestamp(editor.document, editor.document.modificationStamp) ?: return null
        val tsLanguage = editor.virtualFile.extension?.let {
            ApplicationManager.getApplication().getService(LanguageRegistry::class.java).getLanguage(it)
        }
        if (tsLanguage == null) {
            return null
        }
        val document = editor.document
        val query = tsLanguage.indentQuery ?: return null
        val line = document.getLineNumber(offset)
        val previousContentLine =
            IntProgression.fromClosedRange(line - 1, 0, -1).find { !DocumentUtil.isLineEmpty(document, it) }

        val indentRangesStartOffset = document.getLineStartOffset(previousContentLine ?: line)
        val indentRangesEndOffset = document.getLineEndOffset(line)
        val indentRanges: MutableList<IndentRange> = mutableListOf()
        for (match in query.getMatches(tree, tree.rootNode, indentRangesStartOffset, indentRangesEndOffset)) {
            var startPoint: TSPoint? = null
            var endPoint: TSPoint? = null
            for (capture in match.captures) {
                if (capture.index == tsLanguage.indentCaptureId) {
                    startPoint = startPoint ?: capture.node.startPoint
                    endPoint = endPoint ?: capture.node.endPoint
                } else if (capture.index == tsLanguage.indentStartCaptureId) {
                    startPoint = capture.node.endPoint
                } else if (capture.index == tsLanguage.indentEndCaptureId) {
                    endPoint = capture.node.startPoint
                }
            }
            if (startPoint == null || endPoint == null || startPoint.row == endPoint.row) {
                continue
            }
            val range = IndentRange(startPoint.toCharOffsets(), endPoint.toCharOffsets())
            val searchResult = indentRanges.binarySearch(range)
            if (searchResult >= 0) {
                indentRanges[searchResult].end = max(indentRanges[searchResult].end, range.end)
            } else {
                indentRanges.add(-(searchResult + 1), range)
            }
        }
        val indentPoint = TSPoint(
            line, DocumentUtil.getIndentLength(document, document.getLineStartOffset(line))
        )
        val previousLine = previousContentLine ?: 0
        val previousLineIndentPoint = TSPoint(previousLine, DocumentUtil.getIndentLength(document, document.getLineStartOffset(previousLine)))

        var addIndent = false
        var outdentToLine: Int? = null
        for (range in indentRanges) {
            if (range.start.row == previousLine && range.end > indentPoint) {
                addIndent = true
            }
            if (range.end > previousLineIndentPoint && range.end <= indentPoint) {
                outdentToLine = outdentToLine?.let { min(it, range.start.row) } ?: range.start.row
            }
        }

        var baseLine: Int = previousLine
        var indentSpaces = 0

        val indentOptions = CodeStyle.getIndentOptions(project, document)
        if (addIndent) {
            baseLine = previousLine
            indentSpaces = indentOptions.INDENT_SIZE
        } else if (outdentToLine != null && outdentToLine < previousLine) {
            baseLine = outdentToLine
        }

        return getIndentInfo(document, baseLine, indentSpaces, indentOptions.TAB_SIZE).generateNewWhiteSpace(
            indentOptions
        )
    }

    override fun isSuitableFor(language: Language?): Boolean {
        return language != null && language is TreeSitterLanguage
    }
}