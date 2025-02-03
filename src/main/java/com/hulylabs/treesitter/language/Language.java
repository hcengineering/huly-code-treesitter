package com.hulylabs.treesitter.language;

public class Language {
    private final String languageName;
    private String[] nativeHighlights;
    private final long nativeLanguageId;

    public Language(String languageName, long nativeLanguageId) {
        this.nativeLanguageId = nativeLanguageId;
        this.languageName = languageName;
    }

    public String getName() {
        return this.languageName;
    }

    public long getNativeLanguageId() {
        return nativeLanguageId;
    }

    void setNativeHighlights(String[] nativeHighlights) {
        this.nativeHighlights = nativeHighlights;
    }

    public String[] getNativeHighlights() {
        return nativeHighlights;
    }
}
