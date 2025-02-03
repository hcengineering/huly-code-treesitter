package com.hulylabs.treesitter.rusty;

import com.hulylabs.treesitter.language.Language;
import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import org.treesitter.TSLanguage;
import org.treesitter.utils.NativeUtils;

import java.util.HashMap;

@Service
public final class TreeSitterNativeLanguageRegistry {
    public static final long UNDEFINED_LANGUAGE_ID = -1;
    private final HashMap<String, Long> languageIds = new HashMap<>();

    static {
        NativeUtils.loadLib("lib/tree-sitter");
    }

    private static native long nativeRegisterLanguage(String languageName, TSLanguage language);

    private static native String[] nativeAddHighlightQuery(long languageId, byte[] queryData);
    private static native void nativeAddFoldQuery(long languageId, byte[] queryData);
    private static native void nativeAddIndentQuery(long languageId, byte[] queryData);
    private static native void nativeAddInjectionQuery(long languageId, byte[] queryData);


    public long registerLanguage(@NotNull String languageName, @NotNull TSLanguage language) {
        synchronized (languageIds) {
            if (languageIds.containsKey(languageName)) {
                return languageIds.get(languageName);
            }
        }
        long languageId = nativeRegisterLanguage(languageName, language);
        synchronized (languageIds) {
            languageIds.put(languageName, languageId);
        }
        return languageId;
    }

    public String @NotNull [] addHighlightQuery(Language language, byte @NotNull [] queryData) {
        return nativeAddHighlightQuery(language.getNativeLanguageId(), queryData);
    }

    public void addFoldQuery(Language language, byte @NotNull [] queryData) {
        nativeAddFoldQuery(language.getNativeLanguageId(), queryData);
    }

    public void addIndentQuery(Language language, byte @NotNull [] queryData) {
        nativeAddIndentQuery(language.getNativeLanguageId(), queryData);
    }

    public void addInjectionQuery(Language language, byte @NotNull [] queryData) {
         nativeAddInjectionQuery(language.getNativeLanguageId(), queryData);
    }

    public long getLanguageId(String languageName) {
        synchronized (languageIds) {
            return languageIds.getOrDefault(languageName, UNDEFINED_LANGUAGE_ID);
        }
    }
}
