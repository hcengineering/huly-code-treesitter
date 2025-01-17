package com.hulylabs.intellij.plugins.treesitter

import com.hulylabs.treesitter.language.LanguageGeneratedTree
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder

object TreeSitterStorageUtil {
    private val TREE_KEY_RAW = Key.create<LanguageGeneratedTree>("com.hulylabs.treeSitterTreeRaw")
    private val TREE_KEY = Key.create<Pair<LanguageGeneratedTree, Long>>("com.hulylabs.treeSitterTree")

    fun getTreeForTimestamp(dataHolder: UserDataHolder, oldTimestamp: Long): LanguageGeneratedTree? {
        if (dataHolder is Document) {
            val tree = dataHolder.getUserData(TREE_KEY)
            if (tree != null && tree.second == oldTimestamp) {
                return tree.first
            }
            return null
        } else {
            return dataHolder.getUserData(TREE_KEY_RAW)
        }

    }

    fun setCurrentTree(dataHolder: UserDataHolder, tree: LanguageGeneratedTree) {
        if (dataHolder is Document) {
            dataHolder.putUserData(TREE_KEY, Pair(tree, dataHolder.modificationStamp))
        } else {
            dataHolder.putUserData(TREE_KEY_RAW, tree)
        }
    }

    fun moveTreeToDocument(dataHolder: UserDataHolder, document: Document) {
        var tree: LanguageGeneratedTree? = null
        if (dataHolder is Document) {
            dataHolder.getUserData(TREE_KEY)?.let { tree = it.first }
            dataHolder.putUserData(TREE_KEY, null)
        } else {
            dataHolder.getUserData(TREE_KEY_RAW)?.let { tree = it }
            dataHolder.putUserData(TREE_KEY_RAW, null)
        }
        if (tree != null) {
            setCurrentTree(document, tree as LanguageGeneratedTree)
        }
    }
}
