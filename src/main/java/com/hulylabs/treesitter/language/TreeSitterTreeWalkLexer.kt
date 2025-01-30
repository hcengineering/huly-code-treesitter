package com.hulylabs.treesitter.language

import org.treesitter.TSNode
import org.treesitter.TSTreeCursor
import kotlin.math.min

class TreeSitterTreeWalkLexer(private val language: Language, private val highlightedSymbols: BooleanArray) {
    private var endOffset = 0
    private var currentOffset = 0
    private var snapshot: SyntaxSnapshot? = null

    var nodeStarted: Boolean = false
    var nodeFinished: Boolean = false
    private var node: TSNode? = null
    private var cursor: TSTreeCursor? = null

    var tokenType: Int? = null
        private set
    var tokenStart: Int = 0
        private set
    var tokenEnd: Int = 0
        private set

    fun start(snapshot: SyntaxSnapshot?, startOffset: Int, endOffset: Int) {
        if (startOffset == endOffset) {
            this.endOffset = endOffset
            this.currentOffset = startOffset
            tokenType = null
            tokenStart = startOffset
            tokenEnd = startOffset
            return
        }
        startImpl(snapshot, startOffset, endOffset)
    }

    private fun startImpl(snapshot: SyntaxSnapshot?, startOffset: Int, endOffset: Int) {
        this.endOffset = endOffset
        this.currentOffset = startOffset
        if (snapshot != null) {
            this.snapshot = snapshot
            val tree = snapshot.tree
            node = tree.rootNode
            cursor = TSTreeCursor(node)
        } else {
            this.snapshot = null
            node = null
            cursor = null
        }

        tokenType = null
        tokenStart = startOffset
        tokenEnd = startOffset
        nodeStarted = false
        nodeFinished = false

        advance()
    }

    private fun emitLeafNodeToken(node: TSNode) {
        this.node = node
        tokenType = node.symbol
        tokenStart = currentOffset
        tokenEnd = min((node.endByte / 2).toDouble(), endOffset.toDouble()).toInt()
        nodeStarted = true
        nodeFinished = true
    }

    private fun emitWhitespaceTokenBeforeNodeStart(node: TSNode) {
        this.node = node
        tokenType = Language.WHITESPACE_SYMBOL
        tokenStart = currentOffset
        tokenEnd = min((node.startByte / 2).toDouble(), endOffset.toDouble()).toInt()
        nodeStarted = false
        nodeFinished = false
    }

    private fun emitNodeStartToken(node: TSNode) {
        this.node = node
        tokenType = node.symbol
        tokenStart = currentOffset
        tokenEnd = currentOffset
        nodeStarted = true
        nodeFinished = false
    }

    private fun emitNodeEndToken(node: TSNode) {
        this.node = node
        tokenType = node.symbol
        tokenStart = currentOffset
        tokenEnd = min((node.endByte / 2).toDouble(), endOffset.toDouble()).toInt()
        nodeStarted = true
        nodeFinished = true
    }

    private fun emitNextNode(node: TSNode) {
        if (node.startByte / 2 > currentOffset) {
            emitWhitespaceTokenBeforeNodeStart(node)
        } else if (node.childCount > 0 && shouldDescend(node)) {
            emitNodeStartToken(node)
        } else {
            emitLeafNodeToken(node)
        }
    }

    private fun setupInitialState() {
        if (snapshot == null) {
            tokenType = null
            tokenStart = currentOffset
            tokenEnd = endOffset
            return
        }
        val startByteOffset = currentOffset * 2
        // Tokens for nodes that started before startOffset should be already emitted
        while (node!!.startByte < startByteOffset) {
            val childIdx = cursor!!.gotoFirstChildForByte(startByteOffset)
            if (childIdx == -1 || cursor!!.currentNode().endByte == startByteOffset) {
                // No child nodes containing startByteOffset, find nearest node that starts after startByteOffset
                if (!cursor!!.gotoFirstChild()) {
                    // Started in the middle of the leaf node, recover broken token
                    //          v
                    // [ [start .. end] ]
                    emitLeafNodeToken(cursor!!.currentNode())
                    return
                }
                while (cursor!!.currentNode().startByte < startByteOffset) {
                    if (!cursor!!.gotoNextSibling()) {
                        cursor!!.gotoParent()
                        // No child between startByteOffset and parent end byte, emit whitespace token
                        //        v
                        // [[] (whitespace)]
                        emitNodeEndToken(cursor!!.currentNode())
                        return
                    }
                }
            }
            node = cursor!!.currentNode()
        }
        emitNextNode(node!!)
    }

    private fun shouldDescend(node: TSNode): Boolean {
        val symbol = node.symbol
        if (symbol == Language.ERROR_SYMBOL) {
            return true
        }
        val id = language.getVisibleSymbolId(symbol)
        return !highlightedSymbols[id]
    }

    fun advance() {
        var repeatAdvance: Boolean
        do {
            repeatAdvance = false
            currentOffset = tokenEnd
            if (currentOffset >= endOffset) {
                tokenType = null
                tokenStart = currentOffset
                tokenEnd = endOffset
                return
            }
            if (tokenType == null) {
                setupInitialState()
                return
            }
            if (nodeFinished) {
                if (cursor!!.gotoNextSibling()) {
                    emitNextNode(cursor!!.currentNode())
                } else {
                    cursor!!.gotoParent()
                    if (cursor!!.currentNode().endByte / 2 > currentOffset) {
                        emitNodeEndToken(cursor!!.currentNode())
                    } else {
                        this.node = cursor!!.currentNode()
                        nodeStarted = true
                        nodeFinished = true
                        repeatAdvance = true
                    }
                }
            } else if (nodeStarted) {
                if (!cursor!!.gotoFirstChild()) {
                    // Faulty state, node started with children, but cursor can't find any children
                    currentOffset = min((node!!.endByte / 2).toDouble(), endOffset.toDouble()).toInt()
                    emitNodeEndToken(node!!)
                    return
                }
                emitNextNode(cursor!!.currentNode())
            } else {
                if (node!!.childCount > 0 && shouldDescend(node!!)) {
                    emitNodeStartToken(node!!)
                } else {
                    emitLeafNodeToken(node!!)
                }
            }
        } while (repeatAdvance)
    }
}
