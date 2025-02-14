package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage
import com.hulylabs.treesitter.language.Point
import com.hulylabs.treesitter.language.Range
import com.intellij.application.options.CodeStyle
import com.intellij.formatting.IndentInfo
import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil
import kotlin.math.min

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
        val snapshot = TreeSitterStorageUtil.getSnapshotForTimestamp(editor.document, editor.document.modificationStamp)
            ?: return null
        val document = editor.document
        val line = document.getLineNumber(offset)
        val previousContentLine =
            IntProgression.fromClosedRange(line - 1, 0, -1).find { !DocumentUtil.isLineEmpty(document, it) }

        val indentRangesStartOffset = document.getLineStartOffset(previousContentLine ?: line)
        val indentRangesEndOffset = document.getLineEndOffset(line)
        val indentRanges: MutableList<Range> = mutableListOf()
        for (range in snapshot.getIndentRanges(
            document.immutableCharSequence, indentRangesStartOffset, indentRangesEndOffset
        )) {
            if (range.startPoint.row == range.endPoint.row) {
                continue
            }
            val searchResult = indentRanges.binarySearchBy(range.startPoint) { it.startPoint }
            if (searchResult >= 0) {
                val existingRange = indentRanges[searchResult]
                indentRanges[searchResult] = Range(
                    existingRange.startOffset,
                    Math.max(existingRange.endOffset, range.endOffset),
                    existingRange.startPoint,
                    Point.max(existingRange.endPoint, range.endPoint)
                )
            } else {
                indentRanges.add(-(searchResult + 1), range)
            }
        }
        val indentPoint = getLineIndentPoint(document,line)
        val previousLine = previousContentLine ?: 0
        val previousLineIndentPoint = getLineIndentPoint(document, previousLine)

        var addIndent = false
        var outdentToLine: Int? = null
        for (range in indentRanges) {
            if (range.startPoint.row == previousLine && range.endPoint > indentPoint) {
                addIndent = true
            }
            if (range.endPoint > previousLineIndentPoint && range.endPoint <= indentPoint) {
                outdentToLine = outdentToLine?.let { min(it, range.startPoint.row) } ?: range.startPoint.row
            }
        }

        var baseLine: Int = previousLine
        var indentSpaces = 0

        val indentOptions = CodeStyle.getIndentOptions(project, document)
        if (addIndent && (outdentToLine == null || outdentToLine != previousLine)) {
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

    private fun getLineIndentPoint(document: Document, line: Int): Point {
        return Point(
            line, CharArrayUtil.shiftForward(document.immutableCharSequence, document.getLineStartOffset(line), " \t")
        )
    }
}