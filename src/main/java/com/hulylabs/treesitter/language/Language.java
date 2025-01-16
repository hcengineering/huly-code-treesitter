package com.hulylabs.treesitter.language;

import com.hulylabs.treesitter.query.Query;
import org.jetbrains.annotations.Nullable;
import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import org.treesitter.TSSymbolType;

import java.util.*;

public class Language {
    public static final int ERROR_SYMBOL = 65535;
    private final TSLanguage language;
    private final String languageName;
    private final ArrayList<Integer> visibleSymbols;
    private final int[] visibleSymbolLookup;
    private final Map<Integer, String> highlights;
    private Query indentQuery;
    private int indentCaptureId = -1;
    private int indentStartCaptureId = -1;
    private int indentEndCaptureId = -1;
    private Query foldQuery;
    private int foldCaptureId = -1;

    public Language(TSLanguage language, String languageName, Map<LanguageSymbol, String> highlights) {
        this.language = language;
        this.languageName = languageName;
        int symbolCount = language.symbolCount();
        this.visibleSymbols = new ArrayList<>();
        this.visibleSymbolLookup = new int[language.symbolCount()];
        for (int i = 0; i < symbolCount; i++) {
            TSSymbolType symbolType = language.symbolType(i);
            if (symbolType == TSSymbolType.TSSymbolTypeRegular || symbolType == TSSymbolType.TSSymbolTypeAnonymous) {
                this.visibleSymbolLookup[i] = visibleSymbols.size();
                this.visibleSymbols.add(i);
            }
        }
        this.highlights = new HashMap<>(highlights.size());
        for (Map.Entry<LanguageSymbol, String> entry : highlights.entrySet()) {
            LanguageSymbol symbol = entry.getKey();
            this.highlights.put(symbolForName(symbol.getName(), symbol.isNamed()), entry.getValue());
        }
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

    public @Nullable Query getIndentQuery() {
        return this.indentQuery;
    }

    public int getIndentCaptureId() {
        return this.indentCaptureId;
    }

    public int getIndentStartCaptureId() {
        return this.indentStartCaptureId;
    }

    public int getIndentEndCaptureId() {
        return this.indentEndCaptureId;
    }

    void setFoldQuery(Query foldQuery) {
        this.foldQuery = foldQuery;
        int captureId = 0;
        for (String captureName : foldQuery.getCaptureNames()) {
            if (captureName.equals("fold")) {
                this.foldCaptureId = captureId;
                break;
            }
            captureId++;
        }
    }

    public @Nullable Query getFoldQuery() {
        return this.foldQuery;
    }

    public int getFoldCaptureId() {
        return this.foldCaptureId;
    }

    public String getName() {
        return this.languageName;
    }

    public void applyToParser(TSParser parser) {
        parser.setLanguage(this.language);
    }

    public int getVisibleSymbolCount() {
        return this.visibleSymbols.size();
    }

    public int symbolForName(String name, boolean isNamed) {
        return this.visibleSymbolLookup[this.language.symbolForName(name, isNamed)];
    }

    public int getVisibleSymbolId(int id) {
        if (id == ERROR_SYMBOL) {
            return id;
        }
        return this.visibleSymbolLookup[id];
    }

    public Map<Integer, String> getHighlights() {
        return highlights;
    }
}
