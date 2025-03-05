package com.hulylabs.treesitter.language;

import org.jetbrains.annotations.Nullable;

public class Language {
    private final String languageName;
    private String[] nativeHighlights;
    private final long nativeLanguageId;
    private final CommenterConfig commenterConfig;

    public Language(String languageName, long nativeLanguageId, @Nullable CommenterConfig commenterConfig) {
        this.nativeLanguageId = nativeLanguageId;
        this.languageName = languageName;
        this.commenterConfig = commenterConfig;
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

    public CommenterConfig getCommenterConfig() {
        return commenterConfig;
    }

    public record CommenterConfig(@Nullable String lineCommentPrefix, @Nullable String blockCommentPrefix,
                                  @Nullable String blockCommentSuffix, @Nullable String commentedBlockCommentPrefix,
                                  @Nullable String commentedBlockCommentSuffix) {
    }
}
