package com.hulylabs.intellij.plugins.treesitter.editor;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import java.util.Collection;
import java.util.HashMap;

public class TreeSitterHighlightingColors {
    private final HashMap<String, TextAttributesKey> attributes;
    // Identifiers
    public static final TextAttributesKey VARIABLE = TextAttributesKey.createTextAttributesKey("ts.capture.variable", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey VARIABLE_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.variable.builtin", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey VARIABLE_MEMBER = TextAttributesKey.createTextAttributesKey("ts.capture.variable.member", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey VARIABLE_PARAMETER = TextAttributesKey.createTextAttributesKey("ts.capture.variable.parameter", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey VARIABLE_PARAMETER_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.variable.parameter.builtin", VARIABLE_PARAMETER);
    public static final TextAttributesKey CONSTANT = TextAttributesKey.createTextAttributesKey("ts.capture.constant", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey CONSTANT_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.constant.builtin", CONSTANT);
    public static final TextAttributesKey CONSTANT_MACRO = TextAttributesKey.createTextAttributesKey("ts.capture.constant.macro", CONSTANT);
    public static final TextAttributesKey MODULE = TextAttributesKey.createTextAttributesKey("ts.capture.module", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey MODULE_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.module.builtin", MODULE);
    public static final TextAttributesKey LABEL = TextAttributesKey.createTextAttributesKey("ts.capture.label", DefaultLanguageHighlighterColors.LABEL);

    // Literals
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("ts.capture.string", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey STRING_DOC = TextAttributesKey.createTextAttributesKey("ts.capture.string.documentation", STRING);
    public static final TextAttributesKey STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("ts.capture.string.escape", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey STRING_REGEXP = TextAttributesKey.createTextAttributesKey("ts.capture.string.regexp", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey STRING_SPECIAL = TextAttributesKey.createTextAttributesKey("ts.capture.string.special", STRING);
    public static final TextAttributesKey STRING_SPECIAL_SYMBOL = TextAttributesKey.createTextAttributesKey("ts.capture.string.special.symbol", STRING_SPECIAL);
    public static final TextAttributesKey STRING_SPECIAL_URL = TextAttributesKey.createTextAttributesKey("ts.capture.string.special.url", STRING_SPECIAL);
    public static final TextAttributesKey STRING_SPECIAL_PATH = TextAttributesKey.createTextAttributesKey("ts.capture.string.special.path", STRING_SPECIAL);
    public static final TextAttributesKey CHARACTER = TextAttributesKey.createTextAttributesKey("ts.capture.character", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey CHARACTER_SPECIAL = TextAttributesKey.createTextAttributesKey("ts.capture.character.special", CHARACTER);
    public static final TextAttributesKey BOOLEAN = TextAttributesKey.createTextAttributesKey("ts.capture.boolean", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("ts.capture.number", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey FLOAT = TextAttributesKey.createTextAttributesKey("ts.capture.number.float", NUMBER);

    // Types
    public static final TextAttributesKey TYPE = TextAttributesKey.createTextAttributesKey("ts.capture.type", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey TYPE_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.type.builtin", TYPE);
    public static final TextAttributesKey TYPE_DEFINITION = TextAttributesKey.createTextAttributesKey("ts.capture.type.definition", TYPE);
    public static final TextAttributesKey ATTRIBUTE = TextAttributesKey.createTextAttributesKey("ts.capture.attribute", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey ATTRIBUTE_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.attribute.builtin", ATTRIBUTE);
    public static final TextAttributesKey PROPERTY = TextAttributesKey.createTextAttributesKey("ts.capture.property", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey PROPERTY_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.property.builtin", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey("ts.capture.comment", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey COMMENT_DOCUMENTATION = TextAttributesKey.createTextAttributesKey("ts.capture.comment.documentation", DefaultLanguageHighlighterColors.DOC_COMMENT);

    public static final TextAttributesKey CONSTRUCTOR = TextAttributesKey.createTextAttributesKey("ts.capture.constructor", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey CONSTRUCTOR_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.constructor.builtin", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey EMBEDDED = TextAttributesKey.createTextAttributesKey("ts.capture.embedded", (TextAttributesKey) null);
    public static final TextAttributesKey ERROR = TextAttributesKey.createTextAttributesKey("ts.capture.error", (TextAttributesKey) null);
    public static final TextAttributesKey ESCAPE = TextAttributesKey.createTextAttributesKey("ts.capture.escape", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey FUNCTION = TextAttributesKey.createTextAttributesKey("ts.capture.function", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey FUNCTION_BUILTIN = TextAttributesKey.createTextAttributesKey("ts.capture.function.builtin", DefaultLanguageHighlighterColors.STATIC_METHOD);
    public static final TextAttributesKey FUNCTION_CALL = TextAttributesKey.createTextAttributesKey("ts.capture.function.call", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("ts.capture.keyword", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey("ts.capture.operator", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey TAG = TextAttributesKey.createTextAttributesKey("ts.capture.tag", DefaultLanguageHighlighterColors.MARKUP_TAG);
    public static final TextAttributesKey TAG_ATTRIBUTE = TextAttributesKey.createTextAttributesKey("ts.capture.tag.attribute", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE);
    public static final TextAttributesKey TAG_CONSTRUCTOR = TextAttributesKey.createTextAttributesKey("ts.capture.tag.constructor", TreeSitterHighlightingColors.CONSTRUCTOR);
    public static final TextAttributesKey TAG_PROPERTY = TextAttributesKey.createTextAttributesKey("ts.capture.tag.property", TreeSitterHighlightingColors.PROPERTY);

    // Punctuation
    public static final TextAttributesKey PUNCTUATION = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey PUNCTUATION_BRACKET = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.bracket", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PUNCTUATION_DELIMITER = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.delimiter", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey PUNCTUATION_SPECIAL = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.special", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey PUNCTUATION_PARENTHESES = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.bracket.parentheses", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey PUNCTUATION_BRACES = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.bracket.braces", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey PUNCTUATION_COMMA = TextAttributesKey.createTextAttributesKey("ts.capture.punctuation.delimiter.comma", DefaultLanguageHighlighterColors.COMMA);

    // Markup
    public static final TextAttributesKey MARKUP_BOLD = TextAttributesKey.createTextAttributesKey("ts.capture.markup.bold", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_HEADING = TextAttributesKey.createTextAttributesKey("ts.capture.markup.heading", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_ITALIC = TextAttributesKey.createTextAttributesKey("ts.capture.markup.italic", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_LINK = TextAttributesKey.createTextAttributesKey("ts.capture.markup.link", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey MARKUP_LINK_URL = TextAttributesKey.createTextAttributesKey("ts.capture.markup.link.url", MARKUP_LINK);
    public static final TextAttributesKey MARKUP_LIST = TextAttributesKey.createTextAttributesKey("ts.capture.markup.list", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_LIST_CHECKED = TextAttributesKey.createTextAttributesKey("ts.capture.markup.list.checked", MARKUP_LIST);
    public static final TextAttributesKey MARKUP_LIST_NUMBERED = TextAttributesKey.createTextAttributesKey("ts.capture.markup.list.numbered", MARKUP_LIST);
    public static final TextAttributesKey MARKUP_LIST_UNCHECKED = TextAttributesKey.createTextAttributesKey("ts.capture.markup.list.unchecked", MARKUP_LIST);
    public static final TextAttributesKey MARKUP_LIST_UNNUMBERED = TextAttributesKey.createTextAttributesKey("ts.capture.markup.list.unnumbered", MARKUP_LIST);
    public static final TextAttributesKey MARKUP_QUOTE = TextAttributesKey.createTextAttributesKey("ts.capture.markup.quote", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_RAW = TextAttributesKey.createTextAttributesKey("ts.capture.markup.raw", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_RAW_BLOCK = TextAttributesKey.createTextAttributesKey("ts.capture.markup.raw.block", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_RAW_INLINE = TextAttributesKey.createTextAttributesKey("ts.capture.markup.raw.inline", HighlighterColors.TEXT);
    public static final TextAttributesKey MARKUP_STRIKETHROUGH = TextAttributesKey.createTextAttributesKey("ts.capture.markup.strikethrough", HighlighterColors.TEXT);
    private static TreeSitterHighlightingColors INSTANCE;

    public static TreeSitterHighlightingColors getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TreeSitterHighlightingColors();
        }
        return INSTANCE;
    }

    public void addTextAttributesKey(TextAttributesKey key) {
        this.attributes.put(key.getExternalName().replace("ts.capture.", ""), key);
    }

    private TreeSitterHighlightingColors() {
        this.attributes = new HashMap<>();
        addTextAttributesKey(VARIABLE);
        addTextAttributesKey(VARIABLE_BUILTIN);
        addTextAttributesKey(VARIABLE_MEMBER);
        addTextAttributesKey(VARIABLE_PARAMETER);
        addTextAttributesKey(VARIABLE_PARAMETER_BUILTIN);
        addTextAttributesKey(CONSTANT);
        addTextAttributesKey(CONSTANT_BUILTIN);
        addTextAttributesKey(CONSTANT_MACRO);
        addTextAttributesKey(MODULE);
        addTextAttributesKey(MODULE_BUILTIN);
        addTextAttributesKey(LABEL);
        addTextAttributesKey(STRING);
        addTextAttributesKey(STRING_DOC);
        addTextAttributesKey(STRING_ESCAPE);
        addTextAttributesKey(STRING_REGEXP);
        addTextAttributesKey(STRING_SPECIAL);
        addTextAttributesKey(STRING_SPECIAL_SYMBOL);
        addTextAttributesKey(STRING_SPECIAL_URL);
        addTextAttributesKey(STRING_SPECIAL_PATH);
        addTextAttributesKey(CHARACTER);
        addTextAttributesKey(CHARACTER_SPECIAL);
        addTextAttributesKey(BOOLEAN);
        addTextAttributesKey(NUMBER);
        addTextAttributesKey(FLOAT);

        addTextAttributesKey(TYPE);
        addTextAttributesKey(TYPE_BUILTIN);
        addTextAttributesKey(TYPE_DEFINITION);
        addTextAttributesKey(ATTRIBUTE);
        addTextAttributesKey(ATTRIBUTE_BUILTIN);
        addTextAttributesKey(PROPERTY);
        addTextAttributesKey(PROPERTY_BUILTIN);

        addTextAttributesKey(COMMENT);
        addTextAttributesKey(COMMENT_DOCUMENTATION);

        addTextAttributesKey(CONSTRUCTOR);
        addTextAttributesKey(CONSTRUCTOR_BUILTIN);
        addTextAttributesKey(EMBEDDED);
        addTextAttributesKey(ERROR);
        addTextAttributesKey(ESCAPE);
        addTextAttributesKey(FUNCTION);
        addTextAttributesKey(FUNCTION_BUILTIN);
        addTextAttributesKey(FUNCTION_CALL);
        addTextAttributesKey(KEYWORD);
        addTextAttributesKey(OPERATOR);
        addTextAttributesKey(TAG);
        addTextAttributesKey(TAG_ATTRIBUTE);
        addTextAttributesKey(TAG_CONSTRUCTOR);
        addTextAttributesKey(TAG_PROPERTY);

        // Punctuation
        addTextAttributesKey(PUNCTUATION);
        addTextAttributesKey(PUNCTUATION_BRACKET);
        addTextAttributesKey(PUNCTUATION_DELIMITER);
        addTextAttributesKey(PUNCTUATION_SPECIAL);
        addTextAttributesKey(PUNCTUATION_PARENTHESES);
        addTextAttributesKey(PUNCTUATION_BRACES);
        addTextAttributesKey(PUNCTUATION_COMMA);

        // Markup
        addTextAttributesKey(MARKUP_BOLD);
        addTextAttributesKey(MARKUP_HEADING);
        addTextAttributesKey(MARKUP_ITALIC);
        addTextAttributesKey(MARKUP_LINK);
        addTextAttributesKey(MARKUP_LINK_URL);
        addTextAttributesKey(MARKUP_LIST);
        addTextAttributesKey(MARKUP_LIST_CHECKED);
        addTextAttributesKey(MARKUP_LIST_NUMBERED);
        addTextAttributesKey(MARKUP_LIST_UNCHECKED);
        addTextAttributesKey(MARKUP_LIST_UNNUMBERED);
        addTextAttributesKey(MARKUP_QUOTE);
        addTextAttributesKey(MARKUP_RAW);
        addTextAttributesKey(MARKUP_RAW_BLOCK);
        addTextAttributesKey(MARKUP_RAW_INLINE);
        addTextAttributesKey(MARKUP_STRIKETHROUGH);
    }

    public TextAttributesKey getTextAttributesKey(String key) {
        if (attributes.containsKey(key)) {
            return attributes.get(key);
        }
        String[] split = key.split("\\.");
        StringBuilder builder = new StringBuilder(split[0]);
        String bestMatch = null;
        for (int i = 1; i < split.length; i++) {
            if (attributes.containsKey(builder.toString())) {
                bestMatch = builder.toString();
            }
            builder.append(".").append(split[i]);
        }
        return attributes.get(bestMatch);
    }

    public static class IndexedAccessor {
        private final TextAttributesKey[] attributes;

        public IndexedAccessor(TreeSitterHighlightingColors colors, Collection<String> captureNames) {
            this.attributes = new TextAttributesKey[captureNames.size()];
            int i = 0;
            for (String captureName : captureNames) {
                attributes[i++] = colors.getTextAttributesKey(captureName);
            }
        }

        public TextAttributesKey getTextAttributesKey(int captureId) {
            return attributes[captureId];
        }
    }
}
