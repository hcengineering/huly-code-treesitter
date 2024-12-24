// Copyright 2024 Huly Labs
// Includes code from IntelliJ IDEA Community Edition https://www.jetbrains.com/idea/
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.treesitter.highlighter;

import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterLexer;
import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterSyntaxHighlighter;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.LanguageSymbol;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.PrioritizedDocumentListener;
import com.intellij.openapi.editor.ex.util.*;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterClient;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.EditorDocumentPriorities;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.ImmutableCharSequence;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TreeSitterLexerEditorHighlighter implements EditorHighlighter, PrioritizedDocumentListener {
    private final EditorColorsScheme myScheme;
    private final Language myLanguage;
    @NotNull
    private final TreeSitterSyntaxHighlighter myHighlighter;
    private HighlighterClient myEditor;
    private CharSequence myText;
    private SegmentArrayWithData mySegments;
    private final TreeSitterLexer myLexer;
    private final Map<IElementType, TextAttributes> myAttributesMap = new HashMap<>();
    private final Map<IElementType, TextAttributesKey[]> myKeysMap = new HashMap<>();

    public TreeSitterLexerEditorHighlighter(Language language, Map<LanguageSymbol, String> highlights, EditorColorsScheme scheme) {
        myLanguage = language;
        myHighlighter = new TreeSitterSyntaxHighlighter(language, highlights);
        var knownSymbols = myHighlighter.getKnownSymbols();
        mySegments = createSegments();
        myScheme = scheme;
        myLexer = new TreeSitterLexer(language, knownSymbols);
    }

    public SegmentArrayWithData createSegments() {
        return new SegmentArrayWithData(new ShortBasedStorage());
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
    }

    private HighlighterClient getClient() {
        return myEditor;
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
            incrementalUpdate(event.getOffset(), event.getOldLength(), event.getNewLength(), document);
        } catch (ProcessCanceledException e) {
            myText = null;
            mySegments.removeAll();
            throw e;
        }
    }

    private static boolean segmentsEqual(@NotNull SegmentArrayWithData a1,
                                         int idx1,
                                         @NotNull SegmentArrayWithData a2,
                                         int idx2,
                                         int offsetShift) {
        return a1.getSegmentStart(idx1) + offsetShift == a2.getSegmentStart(idx2) &&
                a1.getSegmentEnd(idx1) + offsetShift == a2.getSegmentEnd(idx2) &&
                a1.getSegmentData(idx1) == a2.getSegmentData(idx2);
    }


    private void incrementalUpdate(int eventOffset, int eventOldLength, int eventNewLength, @NotNull Document document) {
        CharSequence text = document.getImmutableCharSequence();

        if (mySegments.getSegmentCount() == 0 || mySegments.getLastValidOffset() < eventOffset) {
            setText(text);
            return;
        }

        myText = text;

        int segmentIndex = mySegments.findSegmentIndex(eventOffset) - 2;
        int oldStartIndex = Math.max(0, segmentIndex);
        int startIndex = oldStartIndex;

        int data;
        do {
            data = mySegments.getSegmentData(startIndex);
            if (data >= 0 || startIndex == 0) break;
            startIndex--;
        }
        while (true);

        int startOffset = mySegments.getSegmentStart(startIndex);
        int textLength = text.length();

        myLexer.start(text, startOffset, textLength, eventOffset, eventOffset + eventOldLength, eventOffset + eventNewLength, data);
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
            if (mySegments.getSegmentStart(startIndex) != tokenStart ||
                    mySegments.getSegmentEnd(startIndex) != tokenEnd ||
                    mySegments.getSegmentData(startIndex) != data) {
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
        int shift = eventNewLength - eventOldLength;
        int newEndOffset = eventOffset + eventNewLength;
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

        if (insertSegmentCount != 0 &&
                (oldEndIndex != startIndex + 1 || insertSegmentCount != 1 || data != mySegments.getSegmentData(startIndex))) {
            myEditor.repaint(startOffset, repaintEnd);
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

        SegmentArrayWithData tempSegments = createSegments();
        ValidatingLexerWrapper lexerWrapper = new ValidatingLexerWrapper(myLexer);
        lexerWrapper.start(text, 0, text.length(), 0);
        int i = 0;
        while (true) {
            IElementType tokenType = lexerWrapper.getTokenType();
            if (tokenType == null) break;
            if (lexerWrapper.getTokenStart() == lexerWrapper.getTokenEnd()) {
                lexerWrapper.advance();
                continue;
            }

            int state = lexerWrapper.getState();
            int data = tempSegments.packData(tokenType, state, state == 0);
            tempSegments.setElementAt(i, lexerWrapper.getTokenStart(), lexerWrapper.getTokenEnd(), data);
            i++;
            if (i % 1024 == 0) {
                ProgressManager.checkCanceled();
            }
            lexerWrapper.advance();
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

    @NotNull
    TextAttributes convertAttributes(TextAttributesKey @NotNull [] keys) {
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

        public int currentIndex() {
            return mySegmentIndex;
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

        public HighlighterClient getClient() {
            return TreeSitterLexerEditorHighlighter.this.getClient();
        }

    }
}
