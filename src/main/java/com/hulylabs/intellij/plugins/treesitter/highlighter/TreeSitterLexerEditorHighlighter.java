// Copyright 2024 Huly Labs
// Includes code from IntelliJ IDEA Community Edition https://www.jetbrains.com/idea/
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.treesitter.highlighter;

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil;
import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterLexer;
import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterSyntaxHighlighter;
import com.hulylabs.treesitter.TreeSitterParsersPool;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.LanguageGeneratedTree;
import com.hulylabs.treesitter.rusty.TreeSitterNativeHighlightLexer;
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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.ImmutableCharSequence;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.treesitter.*;

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
    private final TreeSitterLexer myLexer;
    private final Map<IElementType, TextAttributes> myAttributesMap = new HashMap<>();
    private final Map<IElementType, TextAttributesKey[]> myKeysMap = new HashMap<>();
    // Temporary data holder to store the tree before the editor is attached
    private DataHolder dataHolder = new DataHolder();

    private static class DataHolder extends UserDataHolderBase { }

    public TreeSitterLexerEditorHighlighter(Language language, EditorColorsScheme scheme) {
        myHighlighter = new TreeSitterSyntaxHighlighter(language);
        myLexer = myHighlighter.getHighlightingLexer();
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
            TreeSitterStorageUtil.INSTANCE.moveTreeToDocument(dataHolder, document);
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
            TSInputEdit edit = toTSInputEdit(event);
            incrementalUpdate(document, edit, event.getOldTimeStamp());
        } catch (ProcessCanceledException e) {
            myText = null;
            mySegments.removeAll();
            throw e;
        }
    }

    private static boolean segmentsEqual(@NotNull SegmentArrayWithData a1, int idx1, @NotNull SegmentArrayWithData a2, int idx2, int offsetShift) {
        return a1.getSegmentStart(idx1) + offsetShift == a2.getSegmentStart(idx2) && a1.getSegmentEnd(idx1) + offsetShift == a2.getSegmentEnd(idx2) && a1.getSegmentData(idx1) == a2.getSegmentData(idx2);
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
                column += 2;
            }
        }

        private TSPoint getPoint() {
            return new TSPoint(line, column);
        }
    }

    private static TSInputEdit toTSInputEdit(@NotNull DocumentEvent event) {
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
        return new TSInputEdit(
                event.getOffset() * 2,
                (event.getOffset() + event.getOldLength()) * 2,
                (event.getOffset() + event.getNewLength()) * 2,
                startPointCounter.getPoint(),
                oldEndPointCounter.getPoint(),
                newEndPointCounter.getPoint()
        );
    }

    private void incrementalUpdate(@NotNull Document document, TSInputEdit edit, long oldTimestamp) {
        CharSequence text = document.getImmutableCharSequence();
        LanguageGeneratedTree languageTree = TreeSitterStorageUtil.INSTANCE.getTreeForTimestamp(document, oldTimestamp);
        int eventOffset = edit.getStartByte() / 2;
        if (mySegments.getSegmentCount() == 0 || mySegments.getLastValidOffset() < eventOffset || languageTree == null) {
            setText(text);
            return;
        }

        myText = text;
        var copiedTree = languageTree.getTree().copy();
        copiedTree.edit(edit);
        var newTree = TreeSitterParsersPool.INSTANCE.withParser((TSParser parser) -> {
            languageTree.getLanguage().applyToParser(parser);
            return parser.parseStringEncoding(copiedTree, text.toString(), TSInputEncoding.TSInputEncodingUTF16);
        });
        var rangeStart = eventOffset;
        var rangeEnd = edit.getNewEndByte() / 2;
        if (newTree == null) {
            return;
        }
        TreeSitterStorageUtil.INSTANCE.setCurrentTree(document, new LanguageGeneratedTree(newTree, languageTree.getLanguage()));
        if (languageTree.getLanguage().getNativeHighlights() != null) {
            var changedRanges = TSTree.getChangedRanges(copiedTree, newTree);
            for (var range : changedRanges) {
                if (range.getStartByte() / 2 < rangeStart) {
                    rangeStart = range.getStartByte() / 2;
                }
                if (range.getEndByte() / 2 > rangeEnd) {
                    rangeEnd = range.getEndByte() / 2;
                }
            }
            var tokens = TreeSitterNativeHighlightLexer.collectHighlights(languageTree.getLanguage().getNativeLanguageId(), newTree, text.toString(), rangeStart, rangeEnd);
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
            int shift = (edit.getNewEndByte() - edit.getOldEndByte()) / 2;
            if (invalidatedStart == -1) {
                int segmentIndexStart = mySegments.findSegmentIndex(rangeStart);
                if (rangeEnd <= mySegments.getSegmentEnd(segmentIndexStart)) {
                    int data = mySegments.getSegmentData(segmentIndexStart);
                    int segmentStart = mySegments.getSegmentStart(segmentIndexStart);
                    int segmentEnd = mySegments.getSegmentEnd(segmentIndexStart) + shift;
                    mySegments.setElementAt(segmentIndexStart, segmentStart, segmentEnd, data);
                    if (segmentIndexStart + 1 < mySegments.getSegmentCount()) {
                        mySegments.shiftSegments(segmentIndexStart + 1, shift);
                    }
                    myEditor.repaint(segmentStart, segmentEnd);
                }
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
                myEditor.repaint(invalidatedStart, invalidatedEnd);
            }
        } else {
            int segmentIndex = mySegments.findSegmentIndex(eventOffset) - 2;
            int oldStartIndex = Math.max(0, segmentIndex);
            int startIndex = oldStartIndex;

            int data;
            do {
                data = mySegments.getSegmentData(startIndex);
                if (data >= 0 || startIndex == 0) break;
                startIndex--;
            } while (true);

            int startOffset = mySegments.getSegmentStart(startIndex);
            int textLength = text.length();

            myLexer.start(newTree, startOffset, textLength);
            for (IElementType tokenType = myLexer.getTokenType(); tokenType != null; tokenType = myLexer.getTokenType()) {
                if (startIndex >= oldStartIndex) break;
                if (myLexer.getTokenStart() == myLexer.getTokenEnd()) {
                    myLexer.advance();
                    continue;
                }

                int lexerState = myLexer.getState();
                int tokenStart = myLexer.getTokenStart();
                int tokenEnd = myLexer.getTokenEnd();

                data = mySegments.packData(tokenType, lexerState, lexerState == 0);
                if (mySegments.getSegmentStart(startIndex) != tokenStart || mySegments.getSegmentEnd(startIndex) != tokenEnd || mySegments.getSegmentData(startIndex) != data) {
                    break;
                }
                startIndex++;
                myLexer.advance();
            }
            startOffset = mySegments.getSegmentStart(startIndex);
            SegmentArrayWithData insertSegments = new SegmentArrayWithData(mySegments.createStorage());

            int repaintEnd = -1;
            int insertSegmentCount = 0;
            int oldEndIndex = -1;
            int shift = (edit.getNewEndByte() - edit.getOldEndByte()) / 2;
            int newEndOffset = edit.getNewEndByte() / 2;
            int lastSegmentOffset = mySegments.getLastValidOffset();
            for (IElementType tokenType = myLexer.getTokenType(); tokenType != null; tokenType = myLexer.getTokenType()) {
                int lexerState = myLexer.getState();
                int tokenStart = myLexer.getTokenStart();
                int tokenEnd = myLexer.getTokenEnd();
                if (tokenStart == tokenEnd) {
                    myLexer.advance();
                    continue;
                }

                data = mySegments.packData(tokenType, lexerState, lexerState == 0);
                int shiftedTokenStart = tokenStart - shift;
                if (tokenStart >= newEndOffset && shiftedTokenStart < lastSegmentOffset && lexerState == 0) {
                    int index = mySegments.findSegmentIndex(shiftedTokenStart);
                    if (mySegments.getSegmentStart(index) == shiftedTokenStart && mySegments.getSegmentData(index) == data) {
                        repaintEnd = tokenStart;
                        oldEndIndex = index;
                        break;
                    }
                }
                insertSegments.setElementAt(insertSegmentCount, tokenStart, tokenEnd, data);
                insertSegmentCount++;
                myLexer.advance();
            }

            if (repaintEnd > 0) {
                while (insertSegmentCount > 0 && oldEndIndex > startIndex) {
                    if (!segmentsEqual(mySegments, oldEndIndex - 1, insertSegments, insertSegmentCount - 1, shift)) {
                        break;
                    }
                    insertSegmentCount--;
                    oldEndIndex--;
                    repaintEnd = insertSegments.getSegmentStart(insertSegmentCount);
                    insertSegments.remove(insertSegmentCount, insertSegmentCount + 1);
                }
            }

            if (repaintEnd == -1) {
                repaintEnd = textLength;
            }

            if (oldEndIndex < 0) {
                oldEndIndex = mySegments.getSegmentCount();
            }
            mySegments.shiftSegments(oldEndIndex, shift);
            mySegments.replace(startIndex, oldEndIndex, insertSegments);

            if (insertSegmentCount != 0 && (oldEndIndex != startIndex + 1 || insertSegmentCount != 1 || data != mySegments.getSegmentData(startIndex))) {
                myEditor.repaint(startOffset, repaintEnd);
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
        String str = text.toString();
        Language language = myHighlighter.getLanguage();
        @Nullable TSTree newTree = TreeSitterParsersPool.INSTANCE.withParser((TSParser parser) -> {
            language.applyToParser(parser);
            return parser.parseStringEncoding(null, str, TSInputEncoding.TSInputEncodingUTF16);
        });

        SegmentArrayWithData tempSegments = createSegments();
        Document document = getDocument();
        UserDataHolder activeDataHolder = document != null ? document : dataHolder;
        if (newTree == null) {
            return;
        }
        TreeSitterStorageUtil.INSTANCE.setCurrentTree(activeDataHolder, new LanguageGeneratedTree(newTree, language));
        if (language.getNativeHighlights() != null) {
            var tokens = TreeSitterNativeHighlightLexer.collectHighlights(language.getNativeLanguageId(), newTree, str, 0, textLength);
            int i = 0;
            for (var token : tokens) {
                var elementType = myHighlighter.getTokenType(token);
                int data = tempSegments.packData(elementType, 0, true);
                tempSegments.setElementAt(i, token.startOffset(), token.endOffset(), data);
                i++;
            }
        } else {
            myLexer.start(newTree, 0, textLength);
            int i = 0;
            while (true) {
                IElementType tokenType = myLexer.getTokenType();
                if (tokenType == null) break;
                if (myLexer.getTokenStart() == myLexer.getTokenEnd()) {
                    myLexer.advance();
                    continue;
                }
                int state = myLexer.getState();
                int data = tempSegments.packData(tokenType, state, state == 0);
                tempSegments.setElementAt(i, myLexer.getTokenStart(), myLexer.getTokenEnd(), data);
                i++;
                if (i % 1024 == 0) {
                    ProgressManager.checkCanceled();
                }
            }
        }
        myText = text;
        mySegments = tempSegments;

        if (textLength > 0 && (mySegments.getSegmentCount() == 0 || mySegments.getLastValidOffset() != textLength)) {
            throw new IllegalStateException("Unexpected termination offset for lexer " + myLexer);
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
