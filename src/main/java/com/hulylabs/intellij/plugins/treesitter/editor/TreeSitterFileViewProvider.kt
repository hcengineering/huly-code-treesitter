package com.hulylabs.intellij.plugins.treesitter.editor

import com.hulylabs.intellij.plugins.treesitter.TreeSitterStorageUtil
import com.hulylabs.intellij.plugins.treesitter.language.TreeSitterLanguage
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.PsiElementBase
import javax.swing.Icon

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
        val currentDocument = document as? DocumentEx ?: return super.findElementAt(offset)
        val snapshot = TreeSitterStorageUtil.getSnapshotForTimestamp(currentDocument, currentDocument.modificationSequence)
            ?: return super.findElementAt(offset)

        val psiFile = manager.findFile(virtualFile) ?: return super.findElementAt(offset)
        val (nodeRange, named) = snapshot.findNodeRangeAt(offset) ?: return super.findElementAt(offset)
        if (nodeRange.startOffset >= currentDocument.textLength || nodeRange.endOffset > currentDocument.textLength) {
            return super.findElementAt(offset)
        }
        val nodeElementType = if (named) {
            TreeSitterFakePsiElementNamed(psiFile, TextRange(nodeRange.startOffset, nodeRange.endOffset))
        } else {
            TreeSitterFakePsiElementUnnamed(psiFile, TextRange(nodeRange.startOffset, nodeRange.endOffset))
        }

        return nodeElementType
    }
}

abstract class TreeSitterFakePsiElementBase(protected val file: PsiFile, protected val range: TextRange) :
    PsiElementBase(), ItemPresentation {
    var myName: String? = null

    override fun getPresentableText(): String? {
        return name
    }

    override fun getIcon(unused: Boolean): Icon? {
        return null
    }

    override fun getLanguage(): Language {
        return TreeSitterLanguage.INSTANCE
    }

    override fun getChildren(): Array<PsiElement> {
        return PsiElement.EMPTY_ARRAY
    }

    override fun getParent(): PsiElement {
        return file
    }

    override fun getFirstChild(): PsiElement? {
        return null
    }

    override fun getLastChild(): PsiElement? {
        return null
    }

    override fun getPrevSibling(): PsiElement? {
        return null
    }

    override fun getNextSibling(): PsiElement? {
        return null
    }

    override fun getTextRange(): TextRange {
        return range
    }

    override fun getStartOffsetInParent(): Int {
        return range.startOffset
    }

    override fun getTextLength(): Int {
        return range.length
    }

    override fun findElementAt(offset: Int): PsiElement? {
        return null
    }

    override fun getTextOffset(): Int {
        return range.startOffset
    }

    override fun getName(): String? {
        if (myName != null) {
            return myName
        }
        if (range.startOffset >= file.textLength || range.endOffset > file.textLength) {
            return null
        }
        myName = file.text.substring(range.startOffset, range.endOffset)
        return myName
    }

    override fun getText(): String {
        return name ?: ""
    }

    override fun textToCharArray(): CharArray {
        return text.toCharArray()
    }

    override fun getNode(): ASTNode? {
        return null
    }

    override fun isPhysical(): Boolean {
        return true
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun getLocationString(): String {
        return file.name
    }

    override fun getProject(): Project {
        return file.project
    }

    override fun getContainingFile(): PsiFile {
        return file
    }

    override fun getManager(): PsiManager {
        return file.manager
    }
}

class TreeSitterFakePsiElementNamed(file: PsiFile, range: TextRange) : TreeSitterFakePsiElementBase(file, range),
    PsiNamedElement {
    override fun setName(name: String): PsiElement {
        this.myName = name
        return this
    }
}

class TreeSitterFakePsiElementUnnamed(file: PsiFile, range: TextRange) : TreeSitterFakePsiElementBase(file, range),
    PsiElement {}