package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.treesitter.language.Language;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.treesitter.*;

public class TreeSitterLexer {
    private final TSParser parser;
    private final TreeSitterCaptureElementType[] symbolElementMap;
    private final Language language;

    private int parsedEndOffset;
    private int endOffset;
    private int currentOffset;
    private TSTree tree;

    boolean nodeStarted;
    boolean nodeFinished;
    private TSNode node;
    private TSTreeCursor cursor;

    private IElementType currentToken;
    private int currentTokenStart;
    private int currentTokenEnd;

    public TreeSitterLexer(Language language, TreeSitterCaptureElementType[] symbolElementMap) {
        this.language = language;
        this.parser = language.createParser();
        this.symbolElementMap = symbolElementMap;
    }

    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset) {
        if (startOffset == endOffset) {
            this.endOffset = endOffset;
            this.currentOffset = startOffset;
            currentToken = null;
            currentTokenStart = startOffset;
            currentTokenEnd = startOffset;
            return;
        }

        if (startOffset == 0) {
            tree = null;
            node = null;
            cursor = null;
        }
        if (tree != null) {
            tree.edit(new TSInputEdit(startOffset * 2, parsedEndOffset * 2, endOffset * 2, new TSPoint(0, 0), new TSPoint(0, 0), new TSPoint(0, 0)));
        }
        startImpl(buffer, startOffset, endOffset);
    }

    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int eventOffset, int eventOldLength, int eventNewLength) {
        if (startOffset == endOffset) {
            this.endOffset = endOffset;
            this.currentOffset = startOffset;
            currentToken = null;
            currentTokenStart = startOffset;
            currentTokenEnd = startOffset;
            return;
        }
        if (tree != null) {
            tree.edit(new TSInputEdit(eventOffset * 2, (eventOffset + eventOldLength) * 2, (eventOffset + eventNewLength) * 2, new TSPoint(0, 0), new TSPoint(0, 0), new TSPoint(0, 0)));
        }
        startImpl(buffer, startOffset, endOffset);
    }

    private void startImpl(@NotNull CharSequence buffer, int startOffset, int endOffset) {
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        String str = buffer.toString();
        parser.reset();
        tree = parser.parseStringEncoding(tree, str, TSInputEncoding.TSInputEncodingUTF16);
        parsedEndOffset = endOffset;
        node = tree.getRootNode();
        cursor = new TSTreeCursor(node);

        currentToken = null;
        currentTokenStart = startOffset;
        currentTokenEnd = startOffset;
        nodeStarted = false;
        nodeFinished = false;

        advance();
    }

    public int getState() {
        return 0;
    }

    public @Nullable IElementType getTokenType() {
        return currentToken;
    }

    public int getTokenStart() {
        return currentTokenStart;
    }

    public int getTokenEnd() {
        return currentTokenEnd;
    }

    private IElementType getElementType(int symbol) {
        if (symbol == Language.ERROR_SYMBOL) {
            return TokenType.BAD_CHARACTER;
        }
        int id = language.getVisibleSymbolId(symbol);
        if (symbolElementMap[id] != null) {
            return symbolElementMap[id];
        }
        return TreeSitterCaptureElementType.NONE;
    }

    private void emitLeafNodeToken(TSNode node) {
        this.node = node;
        currentToken = getElementType(node.getSymbol());
        currentTokenStart = currentOffset;
        currentTokenEnd = Math.min(node.getEndByte() / 2, endOffset);
        nodeStarted = true;
        nodeFinished = true;
    }

    private void emitWhitespaceTokenBeforeNodeStart(TSNode node) {
        this.node = node;
        currentToken = TokenType.WHITE_SPACE;
        currentTokenStart = currentOffset;
        currentTokenEnd = Math.min(node.getStartByte() / 2, endOffset);
        nodeStarted = false;
        nodeFinished = false;
    }

    private void emitNodeStartToken(TSNode node) {
        this.node = node;
        currentToken = getElementType(node.getSymbol());
        currentTokenStart = currentOffset;
        currentTokenEnd = currentOffset;
        nodeStarted = true;
        nodeFinished = false;
    }

    private void emitNodeEndToken(TSNode node) {
        this.node = node;
        currentToken = getElementType(node.getSymbol());
        currentTokenStart = currentOffset;
        currentTokenEnd = Math.min(node.getEndByte() / 2, endOffset);
        nodeStarted = true;
        nodeFinished = true;
    }

    private void emitNextNode(TSNode node) {
        if (node.getStartByte() / 2 > currentOffset) {
            emitWhitespaceTokenBeforeNodeStart(node);
        } else if (node.getChildCount() > 0 && shouldDescend(node)) {
            emitNodeStartToken(node);
        } else {
            emitLeafNodeToken(node);
        }
    }

    private void setupInitialState() {
        int startByteOffset = currentOffset * 2;
        // Tokens for nodes that started before startOffset should be already emitted
        while (node.getStartByte() < startByteOffset) {
            int childIdx = cursor.gotoFirstChildForByte(startByteOffset);
            if (childIdx == -1 || cursor.currentNode().getEndByte() == startByteOffset) {
                // No child nodes containing startByteOffset, find nearest node that starts after startByteOffset
                if (!cursor.gotoFirstChild()) {
                    // Started in the middle of the leaf node, recover broken token
                    //          v
                    // [ [start .. end] ]
                    emitLeafNodeToken(cursor.currentNode());
                    return;
                }
                while (cursor.currentNode().getStartByte() < startByteOffset) {
                    if (!cursor.gotoNextSibling()) {
                        cursor.gotoParent();
                        // No child between startByteOffset and parent end byte, emit whitespace token
                        //        v
                        // [[] (whitespace)]
                        emitNodeEndToken(cursor.currentNode());
                        return;
                    }
                }
            }
            node = cursor.currentNode();
        }
        emitNextNode(node);
    }

    boolean shouldDescend(TSNode node) {
        int symbol = node.getSymbol();
        if (symbol == Language.ERROR_SYMBOL) {
            return true;
        }
        int id = language.getVisibleSymbolId(symbol);
        return symbolElementMap[id] == null;
    }

    public void advance() {
        boolean repeatAdvance;
        do {
            repeatAdvance = false;
            currentOffset = currentTokenEnd;
            if (currentOffset >= endOffset) {
                currentToken = null;
                currentTokenStart = currentOffset;
                currentTokenEnd = endOffset;
                return;
            }
            if (currentToken == null) {
                setupInitialState();
                return;
            }
            if (nodeFinished) {
                if (cursor.gotoNextSibling()) {
                    emitNextNode(cursor.currentNode());
                } else {
                    cursor.gotoParent();
                    if (cursor.currentNode().getEndByte() / 2 > currentOffset) {
                        emitNodeEndToken(cursor.currentNode());
                    } else {
                        this.node = cursor.currentNode();
                        nodeStarted = true;
                        nodeFinished = true;
                        repeatAdvance = true;
                    }
                }
            } else if (nodeStarted) {
                if (!cursor.gotoFirstChild()) {
                    // Faulty state, node started with children, but cursor can't find any children
                    currentOffset = Math.min(node.getEndByte() / 2, endOffset);
                    emitNodeEndToken(node);
                    return;
                }
                emitNextNode(cursor.currentNode());
            } else {
                if (node.getChildCount() > 0 && shouldDescend(node)) {
                    emitNodeStartToken(node);
                } else {
                    emitLeafNodeToken(node);
                }
            }
        } while (repeatAdvance);
    }
}
