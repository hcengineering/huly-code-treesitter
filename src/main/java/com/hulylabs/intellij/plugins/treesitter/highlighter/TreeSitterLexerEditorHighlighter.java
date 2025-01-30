// Copyright 2024 Huly Labs
// Includes code from IntelliJ IDEA Community Edition https://www.jetbrains.com/idea/
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.treesitter.highlighter;

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil;
import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterCaptureElementType;
import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterSyntaxHighlighter;
import com.hulylabs.treesitter.language.InputEdit;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.Point;
import com.hulylabs.treesitter.language.SyntaxSnapshot;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.PrioritizedDocumentListener;
import com.intellij.openapi.editor.ex.util.SegmentArrayWithData;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterClient;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.EditorDocumentPriorities;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.ImmutableCharSequence;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

public class TreeSitterLexerEditorHighlighter implements EditorHighlighter, PrioritizedDocumentListener {
    private EditorColorsScheme myScheme;
    @NotNull
    private final TreeSitterSyntaxHighlighter myHighlighter;
    private HighlighterClient myEditor;
    private CharSequence myText;
    private SegmentArrayWithData mySegments;
    private final Map<IElementType, TextAttributes> myAttributesMap = new HashMap<>();
    private final Map<IElementType, TextAttributesKey[]> myKeysMap = new HashMap<>();
    // Temporary data holder to store the tree before the editor is attached
    private DataHolder dataHolder = new DataHolder();

    private static class DataHolder extends UserDataHolderBase { }

    public TreeSitterLexerEditorHighlighter(Language language, EditorColorsScheme scheme) {
        myHighlighter = new TreeSitterSyntaxHighlighter(language);
        mySegments = createSegments();
        myScheme = scheme;
    }

    public SegmentArrayWithData createSegments() {
        return new SegmentArrayWithData(new TreeSitterCaptureStorage());
    }

    @Override
    public @NotNull HighlighterIterator createIterator(int startOffset) {
        synchronized (this) {
            if (!isInSyncWithDocument()) {
                Document document = getDocument();
                assert document != null;
                if (document.isInBulkUpdate()) {
                    //noinspection deprecation
                    document.setInBulkUpdate(false); // bulk mode failed
                }
                doSetText(document.getImmutableCharSequence());
            }

            int latestValidOffset = mySegments.getLastValidOffset();
            return new HighlighterIteratorImpl(Math.max(0, Math.min(startOffset, latestValidOffset)));
        }
    }


    private boolean isInSyncWithDocument() {
        Document document = getDocument();
        return document == null || document.getTextLength() == 0 || mySegments.getSegmentCount() > 0;

    }

    @Override
    public void setEditor(@NotNull HighlighterClient editor) {
        this.myEditor = editor;
        Document document = getDocument();
        if (document != null && Comparing.equal(myText, document.getImmutableCharSequence())) {
            TreeSitterStorageUtil.INSTANCE.moveSnapshotToDocument(dataHolder, document);
        }
    }

    @Override
    public void setColorScheme(@NotNull EditorColorsScheme scheme) {
        myScheme = scheme;
        myAttributesMap.clear();
    }

    private @Nullable Document getDocument() {
        return myEditor != null ? myEditor.getDocument() : null;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        try {
            Document document = event.getDocument();
            if (document.isInBulkUpdate()) {
                myText = null;
                mySegments.removeAll();
                return;
            }
            InputEdit edit = toInputEdit(event);
            incrementalUpdate(document, edit, event.getOldTimeStamp());
        } catch (ProcessCanceledException e) {
            myText = null;
            mySegments.removeAll();
            throw e;
        }
    }

    private static class TreeSitterPositionCounter implements IntConsumer {
        private int line;
        private int column;

        public TreeSitterPositionCounter(int line, int column) {
            this.line = line;
            this.column = column;
        }

        @Override
        public void accept(int value) {
            // TreeSitter column is a byte offset
            if (value == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        }

        private Point getPoint() {
            return new Point(line, column);
        }
    }

    private static InputEdit toInputEdit(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        CharSequence text = document.getImmutableCharSequence();

        int startLine = document.getLineNumber(event.getOffset());
        int startLineOffset = document.getLineStartOffset(startLine);
        var startPointCounter = new TreeSitterPositionCounter(startLine, 0);
        text.subSequence(startLineOffset, event.getOffset()).chars().forEachOrdered(startPointCounter);
        var oldEndPointCounter = new TreeSitterPositionCounter(startPointCounter.line, startPointCounter.column);
        var newEndPointCounter = new TreeSitterPositionCounter(startPointCounter.line, startPointCounter.column);
        event.getOldFragment().chars().forEachOrdered(oldEndPointCounter);
        event.getNewFragment().chars().forEachOrdered(newEndPointCounter);
        return new InputEdit(event.getOffset(), (event.getOffset() + event.getOldLength()), (event.getOffset() + event.getNewLength()), startPointCounter.getPoint(), oldEndPointCounter.getPoint(), newEndPointCounter.getPoint());
    }

    private void incrementalUpdate(@NotNull Document document, InputEdit edit, long oldTimestamp) {
        CharSequence text = document.getImmutableCharSequence();
        SyntaxSnapshot snapshot = TreeSitterStorageUtil.INSTANCE.getSnapshotForTimestamp(document, oldTimestamp);

        int eventOffset = edit.getStartOffset();
        if (mySegments.getSegmentCount() == 0 || mySegments.getLastValidOffset() < eventOffset || snapshot == null) {
            setText(text);
            return;
        }
        myText = text;
        var editedSnapshot = snapshot.applyEdit(edit, document.getModificationStamp());
        var newSnapshot = SyntaxSnapshot.parse(text, editedSnapshot);
        var rangeStart = eventOffset;
        var rangeEnd = edit.getNewEndOffset();
        if (newSnapshot == null) {
            return;
        }
        TreeSitterStorageUtil.INSTANCE.setCurrentSnapshot(document, newSnapshot);
        var changedRanges = SyntaxSnapshot.getChangedRanges(editedSnapshot, newSnapshot);
        for (var range : changedRanges) {
            if (range.getStartOffset() < rangeStart) {
                rangeStart = range.getStartOffset();
            }
            if (range.getEndOffset() > rangeEnd) {
                rangeEnd = range.getEndOffset();
            }
        }
        var tokens = newSnapshot.collectNativeHighlights(text, rangeStart, rangeEnd);
        if (tokens != null) {
            SegmentArrayWithData insertSegments = new SegmentArrayWithData(mySegments.createStorage());
            int invalidatedStart = -1;
            int invalidatedEnd = -1;
            for (var token : tokens) {
                if (invalidatedStart == -1) {
                    invalidatedStart = token.startOffset();
                }
                invalidatedEnd = token.endOffset();
                var elementType = myHighlighter.getTokenType(token);
                int data = mySegments.packData(elementType, 0, true);
                insertSegments.setElementAt(insertSegments.getSegmentCount(), token.startOffset(), token.endOffset(), data);
            }
            int shift = edit.getNewEndOffset() - edit.getOldEndOffset();
            if (invalidatedStart == -1) {
                setText(text);
            } else {
                int segmentIndexStart = mySegments.findSegmentIndex(invalidatedStart);
                int oldEndIndex = mySegments.findSegmentIndex(invalidatedEnd - shift);
                if (segmentIndexStart < oldEndIndex) {
                    mySegments.shiftSegments(oldEndIndex, shift);
                    mySegments.replace(segmentIndexStart, oldEndIndex, insertSegments);
                } else {
                    if (segmentIndexStart + 1 < mySegments.getSegmentCount()) {
                        mySegments.shiftSegments(segmentIndexStart + 1, shift);
                    }
                    int oldInvalidatedEnd = invalidatedEnd - shift;
                    int segmentStart = mySegments.getSegmentStart(segmentIndexStart);
                    int segmentEnd = mySegments.getSegmentEnd(segmentIndexStart);
                    int segmentData = mySegments.getSegmentData(segmentIndexStart);
                    if (segmentStart < invalidatedStart && oldInvalidatedEnd < segmentEnd) {
                        insertSegments.setElementAt(insertSegments.getSegmentCount(), invalidatedEnd, segmentEnd + shift, segmentData);
                        mySegments.setElementAt(segmentIndexStart, segmentStart, invalidatedStart, segmentData);
                        mySegments.insert(insertSegments, segmentIndexStart + 1);
                    } else if (segmentStart == invalidatedStart) {
                        mySegments.setElementAt(segmentIndexStart, invalidatedEnd, segmentEnd + shift, segmentData);
                        mySegments.insert(insertSegments, segmentIndexStart);
                    } else if (segmentEnd == oldInvalidatedEnd) {
                        mySegments.setElementAt(segmentIndexStart, segmentStart, invalidatedStart, segmentData);
                        mySegments.insert(insertSegments, segmentIndexStart + 1);
                    }
                }
                if (myEditor != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
                    int finalInvalidatedStart = invalidatedStart;
                    int finalInvalidatedEnd = invalidatedEnd;
                    UIUtil.invokeLaterIfNeeded(() -> myEditor.repaint(finalInvalidatedStart, finalInvalidatedEnd));
                }
            }
        } else {
            mySegments.removeAll();
            int data = mySegments.packData(TreeSitterCaptureElementType.NONE, 0, true);
            mySegments.setElementAt(0, 0, document.getTextLength(), data);
            if (myEditor != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
                int textLength = document.getTextLength();
                UIUtil.invokeLaterIfNeeded(() -> myEditor.repaint(0, textLength));
            }
        }
    }

    @Override
    public void setText(@NotNull CharSequence text) {
        synchronized (this) {
            doSetText(text);
        }
    }

    private void doSetText(CharSequence text) {
        if (Comparing.equal(myText, text)) return;
        text = ImmutableCharSequence.asImmutable(text);
        int textLength = text.length();
        Language language = myHighlighter.getLanguage();

        SegmentArrayWithData tempSegments = createSegments();
        Document document = getDocument();
        UserDataHolder activeDataHolder = document != null ? document : dataHolder;
        Long timestamp = document != null ? document.getModificationStamp() : null;
        var newSnapshot = SyntaxSnapshot.parse(text, language, timestamp);
        if (newSnapshot == null) {
            return;
        }
        TreeSitterStorageUtil.INSTANCE.setCurrentSnapshot(activeDataHolder, newSnapshot);
        var tokens = newSnapshot.collectNativeHighlights(text, 0, textLength);
        if (tokens != null) {
            int i = 0;
            for (var token : tokens) {
                var elementType = myHighlighter.getTokenType(token);
                int data = tempSegments.packData(elementType, 0, true);
                tempSegments.setElementAt(i, token.startOffset(), token.endOffset(), data);
                i++;
            }
        } else {
            int data = tempSegments.packData(TreeSitterCaptureElementType.NONE, 0, true);
            tempSegments.setElementAt(0, 0, textLength, data);
        }
        myText = text;
        mySegments = tempSegments;

        if (textLength > 0 && (mySegments.getSegmentCount() == 0 || mySegments.getLastValidOffset() != textLength)) {
            throw new IllegalStateException("Unexpected termination offset");
        }

        if (myEditor != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
            UIUtil.invokeLaterIfNeeded(() -> myEditor.repaint(0, textLength));
        }

    }

    @Override
    public int getPriority() {
        return EditorDocumentPriorities.LEXER_EDITOR;
    }

    @NotNull TextAttributes convertAttributes(TextAttributesKey @NotNull [] keys) {
        TextAttributes result = new TextAttributes();

        for (TextAttributesKey key : keys) {
            TextAttributes attributes = myScheme.getAttributes(key);
            if (attributes != null) {
                result = TextAttributes.merge(result, attributes);
            }
        }
        return result;
    }

    private @NotNull TextAttributes getAttributes(@NotNull IElementType tokenType) {
        TextAttributes attrs = myAttributesMap.get(tokenType);
        if (attrs == null) {
            // let's fetch syntax highlighter attributes for token and merge them with "TEXT" attribute of current color scheme
            attrs = convertAttributes(getAttributesKeys(tokenType));
            myAttributesMap.put(tokenType, attrs);
        }
        return attrs;
    }

    private TextAttributesKey @NotNull [] getAttributesKeys(@NotNull IElementType tokenType) {
        TextAttributesKey[] attributesKeys = myKeysMap.get(tokenType);
        if (attributesKeys == null) {
            attributesKeys = myHighlighter.getTokenHighlights(tokenType);
            myKeysMap.put(tokenType, attributesKeys);
        }
        return attributesKeys;
    }


    private class HighlighterIteratorImpl implements HighlighterIterator {
        private int mySegmentIndex;

        HighlighterIteratorImpl(int startOffset) {
            if (startOffset < 0 || startOffset > mySegments.getLastValidOffset()) {
                throw new IllegalArgumentException("Invalid offset: " + startOffset + "; mySegments.getLastValidOffset()=" + mySegments.getLastValidOffset());
            }
            mySegmentIndex = mySegments.findSegmentIndex(startOffset);
        }

        @Override
        public TextAttributes getTextAttributes() {
            return getAttributes(getTokenType());
        }

        @Override
        public int getStart() {
            return mySegments.getSegmentStart(mySegmentIndex);
        }

        @Override
        public int getEnd() {
            return mySegments.getSegmentEnd(mySegmentIndex);
        }

        @Override
        public IElementType getTokenType() {
            return mySegments.unpackTokenFromData(mySegments.getSegmentData(mySegmentIndex));
        }

        @Override
        public void advance() {
            mySegmentIndex++;
        }

        @Override
        public void retreat() {
            mySegmentIndex--;
        }

        @Override
        public boolean atEnd() {
            return mySegmentIndex >= mySegments.getSegmentCount() || mySegmentIndex < 0;
        }

        @Override
        public Document getDocument() {
            return TreeSitterLexerEditorHighlighter.this.getDocument();
        }
    }
}
