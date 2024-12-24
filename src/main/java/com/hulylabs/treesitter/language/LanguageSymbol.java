package com.hulylabs.treesitter.language;

import java.util.Objects;

public class LanguageSymbol {
    private final String name;
    private final boolean isNamed;

    public LanguageSymbol(String name, boolean isNamed) {
        this.name = name;
        this.isNamed = isNamed;
    }

    public String getName() {
        return name;
    }

    public boolean isNamed() {
        return isNamed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LanguageSymbol that = (LanguageSymbol) o;
        return isNamed == that.isNamed && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isNamed);
    }
}
