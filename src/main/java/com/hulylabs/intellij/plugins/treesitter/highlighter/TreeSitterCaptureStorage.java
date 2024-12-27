package com.hulylabs.intellij.plugins.treesitter.highlighter;

import com.hulylabs.intellij.plugins.treesitter.language.syntax.TreeSitterCaptureElementType;
import com.intellij.openapi.editor.ex.util.ShortBasedStorage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class TreeSitterCaptureStorage extends ShortBasedStorage {
    @Override
    public int packData(@NotNull IElementType tokenType, int state, boolean isRestartableState) {
        if (tokenType instanceof TreeSitterCaptureElementType) {
            return 1 + ((TreeSitterCaptureElementType) tokenType).getGroupId();
        } else {
            return -Math.abs(super.packData(tokenType, state, isRestartableState));
        }
    }

    @Override
    public @NotNull IElementType unpackTokenFromData(int data) {
        if (data < 1) {
            return super.unpackTokenFromData(-data);
        } else {
            return TreeSitterCaptureElementType.find(data - 1);
        }
    }
}
