package com.hulylabs.treesitter.rusty;

import com.hulylabs.treesitter.language.InputEdit;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.Range;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;

public class TreeSitterNativeSyntaxSnapshot {
    private static final Cleaner CLEANER = Cleaner.create();
    // handle is used exclusively by native code
    @SuppressWarnings("unused")
    private final long handle;
    private final long baseLanguageId;

    // Should only be called from native code as it knows lifetime of the handle
    private TreeSitterNativeSyntaxSnapshot(long handle, long baseLanguageId) {
        this.handle = handle;
        this.baseLanguageId = baseLanguageId;
        CLEANER.register(this, new NativeSyntaxSnapshotCleanAction(handle));
    }

    private static native TreeSitterNativeSyntaxSnapshot nativeParse(char[] text, long baseLanguageId);

    private static native TreeSitterNativeSyntaxSnapshot nativeParseWithOld(char[] text, TreeSitterNativeSyntaxSnapshot oldSnapshot);

    private static native TreeSitterNativeSyntaxSnapshot nativeEdit(TreeSitterNativeSyntaxSnapshot snapshot, InputEdit edit);

    private static native void nativeDestroy(long handle);

    private static native Range[] nativeGetChangedRanges(TreeSitterNativeSyntaxSnapshot oldSnapshot, TreeSitterNativeSyntaxSnapshot snapshot);

    public static @Nullable TreeSitterNativeSyntaxSnapshot parse(@NotNull CharSequence text, Language baseLanguage) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeParse(chars, baseLanguage.getNativeLanguageId());
    }

    public static @Nullable TreeSitterNativeSyntaxSnapshot parse(@NotNull CharSequence text, TreeSitterNativeSyntaxSnapshot oldSnapshot) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeParseWithOld(chars, oldSnapshot);
    }

    public static @NotNull Range[] getChangedRanges(@NotNull TreeSitterNativeSyntaxSnapshot oldSnapshot, @NotNull TreeSitterNativeSyntaxSnapshot newSnapshot) {
        if (oldSnapshot.baseLanguageId != newSnapshot.baseLanguageId) {
            throw new IllegalArgumentException("Snapshots must be from the same language");
        }
        return nativeGetChangedRanges(oldSnapshot, newSnapshot);
    }

    public @NotNull TreeSitterNativeSyntaxSnapshot withEdit(@NotNull InputEdit edit) {
        return nativeEdit(this, edit);
    }

    private static class NativeSyntaxSnapshotCleanAction implements Runnable {
        private final long handle;

        public NativeSyntaxSnapshotCleanAction(long handle) {
            this.handle = handle;
        }

        @Override
        public void run() {
            nativeDestroy(handle);
        }
    }
}
