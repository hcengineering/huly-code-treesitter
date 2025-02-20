package com.hulylabs.treesitter.language

import com.hulylabs.treesitter.rusty.TreeSitterNativeHighlightLexer
import com.hulylabs.treesitter.rusty.TreeSitterNativeRangesProvider
import com.hulylabs.treesitter.rusty.TreeSitterNativeSyntaxSnapshot

class Point(val row: Int, val column: Int) : Comparable<Point> {
    override fun compareTo(other: Point): Int {
        return if (row == other.row) {
            column.compareTo(other.column)
        } else {
            row.compareTo(other.row)
        }
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

class Range(val startOffset: Int, val endOffset: Int, val startPoint: Point, val endPoint: Point)

class InputEdit(
    val startOffset: Int,
    val oldEndOffset: Int,
    val newEndOffset: Int,
    val startPoint: Point,
    val oldEndPoint: Point,
    val newEndPoint: Point
)

class SyntaxSnapshot(
    internal val snapshot: TreeSitterNativeSyntaxSnapshot, val language: Language, val timestamp: Long?
) {
    fun withTimestamp(timestamp: Long): SyntaxSnapshot {
        if (this.timestamp == timestamp) {
            return this
        }
        return SyntaxSnapshot(snapshot, language, timestamp)
    }

    fun getIndentRanges(text: CharSequence, startOffset: Int, endOffset: Int): Iterable<Range> {
        return TreeSitterNativeRangesProvider.getIndentRanges(snapshot, text, startOffset, endOffset, true).asIterable()
    }

    fun getFoldRanges(
        text: CharSequence, startOffset: Int, endOffset: Int, useInner: Boolean = true
    ): Iterable<Range> {
        return TreeSitterNativeRangesProvider.getFoldRanges(snapshot, text, startOffset, endOffset, useInner)
            .asIterable()
    }

    fun collectNativeHighlights(
        text: CharSequence, startOffset: Int, endOffset: Int
    ): TreeSitterNativeHighlightLexer.Tokens? {
        return TreeSitterNativeHighlightLexer.collectHighlights(
            snapshot, text, startOffset, endOffset
        )
    }

    fun findNodeRangeAt(offset: Int): Range? {
        return snapshot.findNodeRangeAt(offset)
    }

    companion object {
        @JvmStatic
        fun parse(text: CharSequence, language: Language, timestamp: Long?): SyntaxSnapshot? {
            val nativeSnapshot = TreeSitterNativeSyntaxSnapshot.parse(text, language) ?: return null
            return SyntaxSnapshot(nativeSnapshot, language, timestamp)
        }

        @JvmStatic
        fun parse(
            text: CharSequence, oldSnapshot: SyntaxSnapshot, edit: InputEdit, newTimestamp: Long
        ): Pair<SyntaxSnapshot, Iterable<Range>>? {
            val (nativeSnapshot, changedRanges) = TreeSitterNativeSyntaxSnapshot.parse(text, oldSnapshot.snapshot, edit)
                ?: return null
            val newSnapshot = SyntaxSnapshot(nativeSnapshot, oldSnapshot.language, newTimestamp)
            return Pair(newSnapshot, changedRanges.asIterable())
        }
    }
}
