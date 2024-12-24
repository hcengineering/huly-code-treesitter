package com.hulylabs.intellij.plugins.treesitter.language;

import com.hulylabs.treesitter.language.LanguageRegistry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.fileTypes.impl.DetectedByContentFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TreeSitterFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
    public static final TreeSitterFileType INSTANCE = new TreeSitterFileType();

    private TreeSitterFileType() {
        super(TreeSitterLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "TreeSitter";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Text;
    }

    @Override
    public boolean isMyFileType(@NotNull VirtualFile file) {
        if (file.isDirectory()) {
            return false;
        }
        String fileName = file.getName();
        FileType originalFileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
        String extension = file.getExtension();
        if (isUndefinedFileType(originalFileType) && extension != null) {
            LanguageRegistry registry = ApplicationManager.getApplication().getService(LanguageRegistry.class);
            return registry.getLanguage(extension) != null;
        }
        return false;
    }

    private static boolean isUndefinedFileType(@NotNull FileType fileType) {
        return fileType == UnknownFileType.INSTANCE || fileType == INSTANCE || fileType == PlainTextFileType.INSTANCE || fileType == DetectedByContentFileType.INSTANCE;
    }

    static final class TreeSitterFileTypeDetector implements FileTypeRegistry.FileTypeDetector {
        @Override
        public @Nullable FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText) {
            if (file.isDirectory()) {
                return null;
            }
            if (firstCharsIfText == null) {
                return null;
            }
            String fileName = file.getName();
            FileType originalFileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
            String extension = file.getExtension();
            if (isUndefinedFileType(originalFileType) &&extension != null) {
                LanguageRegistry registry = ApplicationManager.getApplication().getService(LanguageRegistry.class);
                return registry.getLanguage(extension) != null ? INSTANCE : null;
            }
            return null;
        }

        @Override
        public int getDesiredContentPrefixLength() {
            return 0;
        }
    }
}
