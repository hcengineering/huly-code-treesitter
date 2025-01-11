package com.hulylabs.intellij.plugins.treesitter.highlighter;

import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.LanguageRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TreeSitterEditorHighlighterProvider implements EditorHighlighterProvider {
    private final HashMap<String, EditorHighlighter> highlighters = new HashMap<>();

    @Override
    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @NotNull FileType fileType, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        if (virtualFile == null) {
            SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, null);
            return new LexerEditorHighlighter(highlighter != null ? highlighter : new PlainSyntaxHighlighter(), colors);
        }
        synchronized (highlighters) {
            if (highlighters.containsKey(virtualFile.getUrl())) {
                return highlighters.get(virtualFile.getUrl());
            }
        }

        String extension = virtualFile.getExtension();


        if (extension != null) {
            Language language = ApplicationManager.getApplication().getService(LanguageRegistry.class).getLanguage(extension);
            if (language != null) {
                var highlighter = new TreeSitterLexerEditorHighlighter(language, colors);
                synchronized (highlighters) {
                    highlighters.put(virtualFile.getUrl(), highlighter);
                }
                return highlighter;
            }
        }
        SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, virtualFile);
        return new LexerEditorHighlighter(highlighter != null ? highlighter : new PlainSyntaxHighlighter(), colors);
    }
}
