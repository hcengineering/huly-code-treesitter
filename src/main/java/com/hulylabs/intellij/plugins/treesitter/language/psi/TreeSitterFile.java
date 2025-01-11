package com.hulylabs.intellij.plugins.treesitter.language.psi;

import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterFileType;
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;

public class TreeSitterFile extends PsiFileBase {
    public TreeSitterFile(FileViewProvider viewProvider) {
        super(viewProvider, TreeSitterLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TreeSitterFileType.INSTANCE;
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
