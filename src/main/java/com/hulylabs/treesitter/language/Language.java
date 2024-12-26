package com.hulylabs.treesitter.language;

import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import org.treesitter.TSSymbolType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Language {
    private static final int ERROR_SYMBOL = 65535;
    private final TSLanguage language;
    private final String languageName;
    private final ArrayList<Integer> visibleSymbols;
    private final int[] visibleSymbolLookup;
    private final HashMap<Integer, String> highlights;

    public Language(TSLanguage language, String languageName, HashMap<LanguageSymbol, String> highlights) {
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

    public String getName() {
        return this.languageName;
    }

    public TSParser createParser() {
        TSParser parser = new TSParser();
        parser.setLanguage(language);
        return parser;
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
