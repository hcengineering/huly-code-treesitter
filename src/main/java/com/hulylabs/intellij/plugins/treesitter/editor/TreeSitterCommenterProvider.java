package com.hulylabs.intellij.plugins.treesitter.editor;

import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeSitterCommenterProvider implements MultipleLangCommentProvider {
    private MultipleLangCommentProvider textMateCommentProvider = null;

    @Override
    public @Nullable Commenter getLineCommenter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Language lineStartLanguage, @NotNull Language lineEndLanguage) {
        if (textMateCommentProvider == null) {
            for (MultipleLangCommentProvider provider : MultipleLangCommentProvider.EP_NAME.getExtensions()) {
                if (provider.getClass().getName().equals("org.jetbrains.plugins.textmate.editor.TextMateCommentProvider")) {
                    textMateCommentProvider = provider;
                    break;
                }
            }
        }
        if (textMateCommentProvider != null) {
            return textMateCommentProvider.getLineCommenter(file, editor, lineStartLanguage, lineEndLanguage);
        }
        return null;
    }

    @Override
    public boolean canProcess(@NotNull PsiFile file, @NotNull FileViewProvider viewProvider) {
        return viewProvider.getBaseLanguage() == TreeSitterLanguage.INSTANCE;
    }
}
