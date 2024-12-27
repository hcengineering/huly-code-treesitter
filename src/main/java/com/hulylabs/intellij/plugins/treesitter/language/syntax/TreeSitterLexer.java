package com.hulylabs.intellij.plugins.treesitter.language.syntax;

import com.hulylabs.treesitter.language.Language;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.treesitter.*;

import java.util.HashMap;
import java.util.Objects;

public class TreeSitterLexer extends LexerBase {
    private final TSParser parser;
    private final short[] symbolHighlightMap;
    private final Language language;
    HashMap<ElementTypeKey, TreeSitterElementType> elementTypes;

    private CharSequence buffer;
    private int startOffset;
    private int parsedEndOffset;
    private int endOffset;
    private int state;
    private int currentOffset;
    private TSTree tree;

    boolean nodeStarted;
    boolean nodeFinished;
    private TSNode node;
    private TSTreeCursor cursor;

    private IElementType currentToken;
    private int currentTokenStart;
    private int currentTokenEnd;

    public TreeSitterLexer(Language language, short[] symbolHighlightMap) {
        this.language = language;
        this.parser = language.createParser();
        this.elementTypes = new HashMap<>();
        this.symbolHighlightMap = symbolHighlightMap;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.state = initialState;
        if (startOffset == endOffset) {
            this.startOffset = startOffset;
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
        startImpl(buffer, startOffset, endOffset, initialState);
    }

    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int eventOffset, int eventOldLength, int eventNewLength, int initialState) {
        this.state = initialState;
        if (startOffset == endOffset) {
            this.startOffset = startOffset;
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
        startImpl(buffer, startOffset, endOffset, initialState);
    }

    private void startImpl(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
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

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return currentToken;
    }

    @Override
    public int getTokenStart() {
        return currentTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return currentTokenEnd;
    }

    private static class ElementTypeKey {
        final int symbol;
        final boolean isNodeStart;
        final boolean isNodeEnd;

        public ElementTypeKey(int symbol, boolean isNodeStart, boolean isNodeEnd) {
            this.symbol = symbol;
            this.isNodeStart = isNodeStart;
            this.isNodeEnd = isNodeEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ElementTypeKey that = (ElementTypeKey) o;
            return symbol == that.symbol && isNodeStart == that.isNodeStart && isNodeEnd == that.isNodeEnd;
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbol, isNodeStart, isNodeEnd);
        }
    }

    private IElementType getElementType(int symbol, boolean isNodeStart, boolean isNodeEnd) {
        if (symbol == 65535) {
            return TokenType.BAD_CHARACTER;
        }
        int id = language.getVisibleSymbolId(symbol);
        ElementTypeKey key = new ElementTypeKey(id, isNodeStart, isNodeEnd);
        if (elementTypes.containsKey(key)) {
            return elementTypes.get(key);
        } else {
            TreeSitterElementType elementType = new TreeSitterElementType(id, isNodeStart, isNodeEnd);
            elementTypes.put(key, elementType);
            return elementType;
        }
    }

    private void emitLeafNodeToken(TSNode node) {
        this.node = node;
        currentToken = getElementType(node.getSymbol(), false, false);
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
        currentToken = getElementType(node.getSymbol(), true, false);
        currentTokenStart = currentOffset;
        currentTokenEnd = currentOffset;
        nodeStarted = true;
        nodeFinished = false;
    }

    private void emitNodeEndToken(TSNode node) {
        this.node = node;
        currentToken = getElementType(node.getSymbol(), false, true);
        currentTokenStart = currentOffset;
        currentTokenEnd = Math.min(node.getEndByte() / 2, endOffset);
        nodeStarted = true;
        nodeFinished = true;
    }

    private void emitNextNode(TSNode node) {
        if (node.getStartByte() / 2 > currentOffset) {
            emitWhitespaceTokenBeforeNodeStart(node);
        } else if (node.getChildCount() > 0 && !isHighlighted(node)) {
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

    boolean isHighlighted(TSNode node) {
        int symbol = node.getSymbol();
        if (symbol == 65535) {
            return false;
        }
        int id = language.getVisibleSymbolId(symbol);
        short symbolHighlight = symbolHighlightMap[id];
        return symbolHighlight != -1;
    }

    @Override
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
                if (node.getChildCount() > 0 && !isHighlighted(node)) {
                    emitNodeStartToken(node);
                } else {
                    emitLeafNodeToken(node);
                }
            }
        } while (repeatAdvance);
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }
}
