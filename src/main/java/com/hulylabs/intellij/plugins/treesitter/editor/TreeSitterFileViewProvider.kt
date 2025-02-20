package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement

class TreeSitterFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(
        file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean
    ): FileViewProvider {
        val project = manager.project
        if (!project.isDisposed && !file.isDirectory && file.isValid) {
            return TreeSitterFileViewProvider(manager, file, eventSystemEnabled)
        }
        return SingleRootFileViewProvider(manager, file, eventSystemEnabled)
    }
}

class TreeSitterFileViewProvider(manager: PsiManager, file: VirtualFile, eventSystemEnabled: Boolean) :
    SingleRootFileViewProvider(manager, file, eventSystemEnabled) {
    override fun findElementAt(offset: Int): PsiElement? {
        val currentDocument = document
        val snapshot = TreeSitterStorageUtil.getSnapshotForTimestamp(currentDocument, currentDocument.modificationStamp)
            ?: return super.findElementAt(offset)

        val psiFile = manager.findFile(virtualFile) ?: return super.findElementAt(offset)
        val nodeRange = snapshot.findNodeRangeAt(offset) ?: return super.findElementAt(offset)
        val nodeElementType = TreeSitterFakePsiElement(psiFile, TextRange(nodeRange.startOffset, nodeRange.endOffset))
        return nodeElementType
    }
}

class TreeSitterFakePsiElement(val file: PsiFile, val range: TextRange) : FakePsiElement() {
    var myName: String? = null

    override fun getProject(): Project {
        return file.project
    }

    override fun getContainingFile(): PsiFile {
        return file
    }


    override fun getParent(): PsiElement {
        return file
    }

    override fun isPhysical(): Boolean {
        return true
    }

    override fun getTextRange(): TextRange {
        return range
    }

    override fun getStartOffsetInParent(): Int {
        return range.startOffset
    }

    override fun getTextOffset(): Int {
        return range.startOffset
    }

    override fun getTextLength(): Int {
        return range.length
    }

    override fun getName(): String? {
        if (myName != null) {
            return myName
        }
        myName = file.text.substring(range.startOffset, range.endOffset)
        return myName
    }

    override fun setName(name: String): PsiElement {
        this.myName = name
        return this
    }

    override fun getText(): String? {
        return getName()
    }

    override fun getLocationString(): String {
        return file.name
    }

    override fun isValid(): Boolean {
        return true
    }
}