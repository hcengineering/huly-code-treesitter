package com.hulylabs.intellij.plugins.treesitter.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

// Empty commenter to trigger multiLangCommenter detection
public class TreeSitterCommenter implements Commenter {
    @Override
    public @Nullable String getLineCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getBlockCommentPrefix() {
        return "";
    }

    @Override
    public @Nullable String getBlockCommentSuffix() {
        return "";
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }
}
