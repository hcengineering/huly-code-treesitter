package com.hulylabs.intellij.plugins.treesitter.editor;

import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage;
import com.hulylabs.treesitter.language.LanguageRegistry;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeSitterCommenterProvider implements MultipleLangCommentProvider {
    private MultipleLangCommentProvider textMateCommentProvider = null;

    @Override
    public @Nullable Commenter getLineCommenter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Language lineStartLanguage, @NotNull Language lineEndLanguage) {
        LanguageRegistry registry = ApplicationManager.getApplication().getService(LanguageRegistry.class);
        VirtualFile virtualFile = file.getVirtualFile();
        com.hulylabs.treesitter.language.Language language = null;
        if (virtualFile != null) {
            String extension = virtualFile.getExtension();
            if (extension != null) {
                language = registry.getLanguage(extension);
            }
        }
        if (language != null) {
            com.hulylabs.treesitter.language.Language.CommenterConfig commenterConfig = language.getCommenterConfig();
            return new Commenter() {
                final String lineCommentPrefix = commenterConfig.lineCommentPrefix();
                final String blockCommentPrefix = commenterConfig.blockCommentPrefix();
                final String blockCommentSuffix = commenterConfig.blockCommentSuffix();
                final String commentedBlockCommentPrefix = commenterConfig.commentedBlockCommentPrefix();
                final String commentedBlockCommentSuffix = commenterConfig.commentedBlockCommentSuffix();

                @Override
                public @Nullable String getLineCommentPrefix() {
                    return lineCommentPrefix;
                }

                @Override
                public @Nullable String getBlockCommentPrefix() {
                    return blockCommentPrefix;
                }

                @Override
                public @Nullable String getBlockCommentSuffix() {
                    return blockCommentSuffix;
                }

                @Override
                public @Nullable String getCommentedBlockCommentPrefix() {
                    return commentedBlockCommentPrefix;
                }

                @Override
                public @Nullable String getCommentedBlockCommentSuffix() {
                    return commentedBlockCommentSuffix;
                }
            };
        }

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
