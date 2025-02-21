package com.hulylabs.treesitter.rusty;

import com.hulylabs.treesitter.language.InputEdit;
import com.hulylabs.treesitter.language.Language;
import com.hulylabs.treesitter.language.Range;
import com.intellij.util.text.CharArrayUtil;
import kotlin.Pair;
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

    private static native Pair<TreeSitterNativeSyntaxSnapshot, Range[]> nativeParseWithOld(char[] text, TreeSitterNativeSyntaxSnapshot oldSnapshot, InputEdit edit);

    private static native void nativeDestroy(long handle);

    private native @Nullable Pair<Range, Boolean> nativeFindNodeRangeAt(int offset);

    public static @Nullable TreeSitterNativeSyntaxSnapshot parse(@NotNull CharSequence text, Language baseLanguage) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeParse(chars, baseLanguage.getNativeLanguageId());
    }

    public static @Nullable Pair<TreeSitterNativeSyntaxSnapshot, Range[]> parse(@NotNull CharSequence text, TreeSitterNativeSyntaxSnapshot oldSnapshot, InputEdit edit) {
        char[] chars = CharArrayUtil.fromSequence(text);
        return nativeParseWithOld(chars, oldSnapshot, edit);
    }

    public @Nullable Pair<Range, Boolean> findNodeRangeAt(int offset) {
        return nativeFindNodeRangeAt(offset);
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
