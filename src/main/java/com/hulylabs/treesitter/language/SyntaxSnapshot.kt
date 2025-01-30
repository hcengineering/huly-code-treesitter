package com.hulylabs.treesitter.language

import com.hulylabs.treesitter.TreeSitterParsersPool.withParser
import com.hulylabs.treesitter.query.Query
import com.hulylabs.treesitter.rusty.TreeSitterNativeHighlightLexer
import org.treesitter.*

class Point(val row: Int, val column: Int) : Comparable<Point> {
    internal constructor(point: TSPoint) : this(point.row, point.column / 2)

    override fun compareTo(other: Point): Int {
        return if (row == other.row) {
            column.compareTo(other.column)
        } else {
            row.compareTo(other.row)
        }
    }

    internal fun toTSPoint(): TSPoint {
        return TSPoint(row, column * 2)
    }

    companion object {
        fun max(a: Point, b: Point): Point {
            return if (a > b) {
                a
            } else {
                b
            }
        }
    }
}

class Range(val startOffset: Int, val endOffset: Int, val startPoint: Point, val endPoint: Point) {
    internal constructor(range: TSRange) : this(
        range.startByte / 2, range.endByte / 2, Point(range.startPoint), Point(range.endPoint)
    )
}

class InputEdit(
    val startOffset: Int,
    val oldEndOffset: Int,
    val newEndOffset: Int,
    val startPoint: Point,
    val oldEndPoint: Point,
    val newEndPoint: Point
) {
    internal fun toTSInputEdit(): TSInputEdit {
        return TSInputEdit(
            startOffset * 2,
            oldEndOffset * 2,
            newEndOffset * 2,
            startPoint.toTSPoint(),
            oldEndPoint.toTSPoint(),
            newEndPoint.toTSPoint()
        )
    }
}

class RangesQueryIterable(
    private val matches: Query.QueryMatchesIterator,
    private val captureId: Int,
    private val startCaptureId: Int,
    private val endCaptureId: Int,
    private val useInner: Boolean
) : Iterable<Range> {
    override fun iterator(): Iterator<Range> {
        return object : Iterator<Range> {
            var nextMatch: Range? = null

            fun populateNextMatch() {
                while (matches.hasNext()) {
                    val match = matches.next()
                    var startOffset: Int? = null
                    var endOffset: Int? = null
                    var startPoint: Point? = null
                    var endPoint: Point? = null
                    for (capture in match.captures) {
                        if (capture.index == captureId) {
                            startOffset = startOffset ?: (capture.node.startByte / 2)
                            endOffset = endOffset ?: (capture.node.endByte / 2)
                            startPoint = startPoint ?: Point(capture.node.startPoint)
                            endPoint = endPoint ?: Point(capture.node.endPoint)
                        } else if (useInner) {
                            if (capture.index == startCaptureId) {
                                startOffset = capture.node.endByte / 2
                                startPoint = Point(capture.node.endPoint)
                            } else if (capture.index == endCaptureId) {
                                endOffset = capture.node.startByte / 2
                                endPoint = Point(capture.node.startPoint)
                            }
                        }
                    }
                    if (startOffset != null && endOffset != null && startPoint != null && endPoint != null) {
                        nextMatch = Range(startOffset, endOffset, startPoint, endPoint)
                        return
                    }
                }
            }

            override fun hasNext(): Boolean {
                if (nextMatch != null) {
                    return true
                }
                populateNextMatch()
                return nextMatch != null
            }

            override fun next(): Range {
                if (nextMatch == null) {
                    populateNextMatch()
                }
                if (nextMatch != null) {
                    val result = nextMatch!!
                    nextMatch = null
                    return result
                } else {
                    throw NoSuchElementException()
                }
            }
        }
    }
}

class SyntaxSnapshot(internal val tree: TSTree, val language: Language, val timestamp: Long?) {
    fun withTimestamp(timestamp: Long): SyntaxSnapshot {
        if (this.timestamp == timestamp) {
            return this
        }
        return SyntaxSnapshot(tree, language, timestamp)
    }

    fun applyEdit(edit: InputEdit, newTimestamp: Long): SyntaxSnapshot {
        val newTree = tree.copy()
        newTree.edit(edit.toTSInputEdit())
        return SyntaxSnapshot(newTree, language, newTimestamp)
    }

    fun getIndentRanges(startOffset: Int, endOffset: Int): Iterable<Range>? {
        val query = language.indentQuery ?: return null
        val matches = query.getMatches(tree, tree.rootNode, startOffset, endOffset)
        return RangesQueryIterable(
            matches, language.indentCaptureId, language.indentStartCaptureId, language.indentEndCaptureId, true
        )
    }

    fun getFoldRanges(startOffset: Int, endOffset: Int, useInner: Boolean = true): Iterable<Range>? {
        val query = language.foldQuery ?: return null
        val matches = query.getMatches(tree, tree.rootNode, startOffset, endOffset)
        return RangesQueryIterable(
            matches, language.foldCaptureId, language.foldStartCaptureId, language.foldEndCaptureId, useInner
        )
    }

    fun collectNativeHighlights(
        text: CharSequence, startOffset: Int, endOffset: Int
    ): TreeSitterNativeHighlightLexer.Tokens? {
        if (language.nativeLanguageId == null) {
            return null
        }
        return TreeSitterNativeHighlightLexer.collectHighlights(
            language.nativeLanguageId, tree, text, startOffset, endOffset
        )
    }

    companion object {
        @JvmStatic
        fun getChangedRanges(oldSnapshot: SyntaxSnapshot, newSnapshot: SyntaxSnapshot): Iterable<Range> {
            return TSTree.getChangedRanges(oldSnapshot.tree, newSnapshot.tree).map { Range(it) }
        }

        @JvmStatic
        fun parse(text: CharSequence, language: Language, timestamp: Long?): SyntaxSnapshot? {
            val newTree = withParser<TSTree?> { parser: TSParser ->
                language.applyToParser(parser)
                parser.parseStringEncoding(null, text.toString(), TSInputEncoding.TSInputEncodingUTF16)
            } ?: return null
            return SyntaxSnapshot(newTree, language, timestamp)
        }

        @JvmStatic
        fun parse(text: CharSequence, oldSnapshot: SyntaxSnapshot): SyntaxSnapshot? {
            val newTree = withParser<TSTree?> { parser: TSParser ->
                oldSnapshot.language.applyToParser(parser)
                parser.parseStringEncoding(oldSnapshot.tree, text.toString(), TSInputEncoding.TSInputEncodingUTF16)
            } ?: return null
            return SyntaxSnapshot(newTree, oldSnapshot.language, oldSnapshot.timestamp)
        }
    }
}
