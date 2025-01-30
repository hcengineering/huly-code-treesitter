package com.hulylabs.treesitter.language;

import com.hulylabs.treesitter.query.Query;
import org.jetbrains.annotations.Nullable;
import org.treesitter.TSLanguage;
import org.treesitter.TSParser;

public class Language {
    private final TSLanguage language;
    private final String languageName;
    private String[] nativeHighlights;
    private Query indentQuery;
    private int indentCaptureId = -1;
    private int indentStartCaptureId = -1;
    private int indentEndCaptureId = -1;
    private Query foldQuery;
    private int foldCaptureId = -1;
    private int foldStartCaptureId = -1;
    private int foldEndCaptureId = -1;
    private Long nativeLanguageId;

    public Language(TSLanguage language, String languageName) {
        this.language = language;
        this.languageName = languageName;
    }

    void setIndentQuery(Query query) {
        this.indentQuery = query;
        int captureId = 0;
        for (String captureName : query.getCaptureNames()) {
            switch (captureName) {
                case "indent":
                    this.indentCaptureId = captureId;
                    break;
                case "start":
                    this.indentStartCaptureId = captureId;
                    break;
                case "end":
                    this.indentEndCaptureId = captureId;
                    break;
            }
            captureId++;
        }
    }

    @Nullable Query getIndentQuery() {
        return this.indentQuery;
    }

    int getIndentCaptureId() {
        return this.indentCaptureId;
    }

    int getIndentStartCaptureId() {
        return this.indentStartCaptureId;
    }

    int getIndentEndCaptureId() {
        return this.indentEndCaptureId;
    }

    void setFoldQuery(Query foldQuery) {
        this.foldQuery = foldQuery;
        int captureId = 0;
        for (String captureName : foldQuery.getCaptureNames()) {
            switch (captureName) {
                case "fold":
                    this.foldCaptureId = captureId;
                    break;
                case "start":
                    this.foldStartCaptureId = captureId;
                    break;
                case "end":
                    this.foldEndCaptureId = captureId;
                    break;
            }
            captureId++;
        }
    }

    @Nullable Query getFoldQuery() {
        return this.foldQuery;
    }

    int getFoldCaptureId() {
        return this.foldCaptureId;
    }

    int getFoldStartCaptureId() {
        return this.foldStartCaptureId;
    }

    int getFoldEndCaptureId() {
        return this.foldEndCaptureId;
    }

    public String getName() {
        return this.languageName;
    }

    public void applyToParser(TSParser parser) {
        parser.setLanguage(this.language);
    }

    public Long getNativeLanguageId() {
        return nativeLanguageId;
    }

    void setNativeLanguageId(long nativeLanguageId) {
        this.nativeLanguageId = nativeLanguageId;
    }

    void setNativeHighlights(String[] nativeHighlights) {
        this.nativeHighlights = nativeHighlights;
    }

    public String[] getNativeHighlights() {
        return nativeHighlights;
    }
}
