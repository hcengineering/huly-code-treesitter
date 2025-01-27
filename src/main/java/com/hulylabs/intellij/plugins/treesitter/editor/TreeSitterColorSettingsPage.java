package com.hulylabs.intellij.plugins.treesitter.editor;

import com.intellij.codeHighlighting.RainbowHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class TreeSitterColorSettingsPage implements ColorSettingsPage, DisplayPrioritySortable {
    private static final PlainSyntaxHighlighter PLAIN_SYNTAX_HIGHLIGHTER = new PlainSyntaxHighlighter();
    private static final AttributesDescriptor[] descriptors = {
        new AttributesDescriptor("Variable", TreeSitterHighlightingColors.VARIABLE),
        new AttributesDescriptor("Variable//Builtin", TreeSitterHighlightingColors.VARIABLE_BUILTIN),
        new AttributesDescriptor("Variable//Member", TreeSitterHighlightingColors.VARIABLE_MEMBER),
        new AttributesDescriptor("Variable//Parameter", TreeSitterHighlightingColors.VARIABLE_PARAMETER),
        new AttributesDescriptor("Variable//Parameter//Builtin", TreeSitterHighlightingColors.VARIABLE_PARAMETER_BUILTIN),
        new AttributesDescriptor("Constant", TreeSitterHighlightingColors.CONSTANT),
        new AttributesDescriptor("Constant//Builtin", TreeSitterHighlightingColors.CONSTANT_BUILTIN),
        new AttributesDescriptor("Constant//Macro", TreeSitterHighlightingColors.CONSTANT_MACRO),
        new AttributesDescriptor("Module", TreeSitterHighlightingColors.MODULE),
        new AttributesDescriptor("Module//Builtin", TreeSitterHighlightingColors.MODULE_BUILTIN),
        new AttributesDescriptor("Label", TreeSitterHighlightingColors.LABEL),

        new AttributesDescriptor("String", TreeSitterHighlightingColors.STRING),
        new AttributesDescriptor("String//Documentation", TreeSitterHighlightingColors.STRING_DOC),
        new AttributesDescriptor("String//Escape", TreeSitterHighlightingColors.STRING_ESCAPE),
        new AttributesDescriptor("String//Regexp", TreeSitterHighlightingColors.STRING_REGEXP),
        new AttributesDescriptor("String//Special", TreeSitterHighlightingColors.STRING_SPECIAL),
        new AttributesDescriptor("String//Special//Symbol", TreeSitterHighlightingColors.STRING_SPECIAL_SYMBOL),
        new AttributesDescriptor("String//Special//Url", TreeSitterHighlightingColors.STRING_SPECIAL_URL),
        new AttributesDescriptor("String//Special//Path", TreeSitterHighlightingColors.STRING_SPECIAL_PATH),
        new AttributesDescriptor("Character", TreeSitterHighlightingColors.CHARACTER),
        new AttributesDescriptor("Character//Special", TreeSitterHighlightingColors.CHARACTER_SPECIAL),
        new AttributesDescriptor("Boolean", TreeSitterHighlightingColors.BOOLEAN),
        new AttributesDescriptor("Number", TreeSitterHighlightingColors.NUMBER),
        new AttributesDescriptor("Float", TreeSitterHighlightingColors.FLOAT),

        new AttributesDescriptor("Type", TreeSitterHighlightingColors.TYPE),
        new AttributesDescriptor("Type//Builtin", TreeSitterHighlightingColors.TYPE_BUILTIN),
        new AttributesDescriptor("Type//Definition", TreeSitterHighlightingColors.TYPE_DEFINITION),
        new AttributesDescriptor("Attribute", TreeSitterHighlightingColors.ATTRIBUTE),
        new AttributesDescriptor("Attribute//Builtin", TreeSitterHighlightingColors.ATTRIBUTE_BUILTIN),
        new AttributesDescriptor("Property", TreeSitterHighlightingColors.PROPERTY),
        new AttributesDescriptor("Property//Builtin", TreeSitterHighlightingColors.PROPERTY_BUILTIN),

        new AttributesDescriptor("Comment", TreeSitterHighlightingColors.COMMENT),
        new AttributesDescriptor("Comment//Documentation", TreeSitterHighlightingColors.COMMENT_DOCUMENTATION),

        new AttributesDescriptor("Constructor", TreeSitterHighlightingColors.CONSTRUCTOR),
        new AttributesDescriptor("Constructor//Builtin", TreeSitterHighlightingColors.CONSTRUCTOR_BUILTIN),
        new AttributesDescriptor("Embedded", TreeSitterHighlightingColors.EMBEDDED),
        new AttributesDescriptor("Error", TreeSitterHighlightingColors.ERROR),
        new AttributesDescriptor("Escape", TreeSitterHighlightingColors.ESCAPE),
        new AttributesDescriptor("Function", TreeSitterHighlightingColors.FUNCTION),
        new AttributesDescriptor("Function//Builtin", TreeSitterHighlightingColors.FUNCTION_BUILTIN),
        new AttributesDescriptor("Function//Call", TreeSitterHighlightingColors.FUNCTION_CALL),
        new AttributesDescriptor("Keyword", TreeSitterHighlightingColors.KEYWORD),
        new AttributesDescriptor("Operator", TreeSitterHighlightingColors.OPERATOR),
        new AttributesDescriptor("Tag", TreeSitterHighlightingColors.TAG),

        // Punctuation
        new AttributesDescriptor("Punctuation", TreeSitterHighlightingColors.PUNCTUATION),
        new AttributesDescriptor("Punctuation//Bracket", TreeSitterHighlightingColors.PUNCTUATION_BRACKET),
        new AttributesDescriptor("Punctuation//Delimiter", TreeSitterHighlightingColors.PUNCTUATION_DELIMITER),
        new AttributesDescriptor("Punctuation//Special", TreeSitterHighlightingColors.PUNCTUATION_SPECIAL),
            new AttributesDescriptor("Punctuation//Comma", TreeSitterHighlightingColors.PUNCTUATION_COMMA),
        new AttributesDescriptor("Punctuation//Parentheses", TreeSitterHighlightingColors.PUNCTUATION_PARENTHESES),
        new AttributesDescriptor("Punctuation//Braces", TreeSitterHighlightingColors.PUNCTUATION_BRACES),

        // Markup
        new AttributesDescriptor("Markup//Bold", TreeSitterHighlightingColors.MARKUP_BOLD),
        new AttributesDescriptor("Markup//Heading", TreeSitterHighlightingColors.MARKUP_HEADING),
        new AttributesDescriptor("Markup//Italic", TreeSitterHighlightingColors.MARKUP_ITALIC),
        new AttributesDescriptor("Markup//Link", TreeSitterHighlightingColors.MARKUP_LINK),
        new AttributesDescriptor("Markup//Link//Url", TreeSitterHighlightingColors.MARKUP_LINK_URL),
        new AttributesDescriptor("Markup//List", TreeSitterHighlightingColors.MARKUP_LIST),
        new AttributesDescriptor("Markup//List//Checked", TreeSitterHighlightingColors.MARKUP_LIST_CHECKED),
        new AttributesDescriptor("Markup//List//Numbered", TreeSitterHighlightingColors.MARKUP_LIST_NUMBERED),
        new AttributesDescriptor("Markup//List//UnChecked", TreeSitterHighlightingColors.MARKUP_LIST_UNCHECKED),
        new AttributesDescriptor("Markup//List//UnNumbered", TreeSitterHighlightingColors.MARKUP_LIST_UNNUMBERED),
        new AttributesDescriptor("Markup//Quote", TreeSitterHighlightingColors.MARKUP_QUOTE),
        new AttributesDescriptor("Markup//Raw", TreeSitterHighlightingColors.MARKUP_RAW),
        new AttributesDescriptor("Markup//Raw//Block", TreeSitterHighlightingColors.MARKUP_RAW_BLOCK),
        new AttributesDescriptor("Markup//Raw//Inline", TreeSitterHighlightingColors.MARKUP_RAW_INLINE),
        new AttributesDescriptor("Markup//Strikethrough", TreeSitterHighlightingColors.MARKUP_STRIKETHROUGH),
    };

    @NonNls private static final Map<String, TextAttributesKey> ourTags = RainbowHighlighter.createRainbowHLM();
    static {
        for (AttributesDescriptor descriptor : descriptors) {
            var attributeKey = descriptor.getKey();
            ourTags.put(attributeKey.getExternalName(), attributeKey);
        }
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return PLAIN_SYNTAX_HIGHLIGHTER;
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return "";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ourTags;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return descriptors;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Tree Sitter";
    }

    @Override
    public DisplayPriority getPriority() {
        return DisplayPriority.CODE_SETTINGS;
    }
}
