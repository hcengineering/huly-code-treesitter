package com.hulylabs.intellij.plugins.treesitter.language;

import com.intellij.lang.Language;

public class TreeSitterLanguage extends Language {
    public static final TreeSitterLanguage INSTANCE = new TreeSitterLanguage();
    private TreeSitterLanguage() {
        super("TreeSitter");
    }
}
