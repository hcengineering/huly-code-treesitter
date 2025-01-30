package com.hulylabs.intellij.plugins.treesitter

import com.hulylabs.treesitter.language.SyntaxSnapshot
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder

object TreeSitterStorageUtil {
    private val SYNTAX_SNAPSHOT_KEY_RAW = Key.create<SyntaxSnapshot>("com.hulylabs.syntaxSnapshotRaw")
    private val SYNTAX_SNAPSHOT_KEY = Key.create<SyntaxSnapshot>("com.hulylabs.syntaxSnapshot")

    fun getSnapshotForTimestamp(dataHolder: UserDataHolder, oldTimestamp: Long): SyntaxSnapshot? {
        if (dataHolder is Document) {
            val syntaxSnapshot = dataHolder.getUserData(SYNTAX_SNAPSHOT_KEY)
            if (syntaxSnapshot != null && syntaxSnapshot.timestamp == oldTimestamp) {
                return syntaxSnapshot
            }
            return null
        } else {
            return dataHolder.getUserData(SYNTAX_SNAPSHOT_KEY_RAW)
        }

    }

    fun setCurrentSnapshot(dataHolder: UserDataHolder, syntaxSnapshot: SyntaxSnapshot) {
        if (dataHolder is Document) {
            if (dataHolder.modificationStamp != syntaxSnapshot.timestamp) {
                throw IllegalStateException("Document modification stamp does not match syntax snapshot timestamp")
            }
            dataHolder.putUserData(SYNTAX_SNAPSHOT_KEY, syntaxSnapshot)
        } else {
            dataHolder.putUserData(SYNTAX_SNAPSHOT_KEY_RAW, syntaxSnapshot)
        }
    }

    fun moveSnapshotToDocument(dataHolder: UserDataHolder, document: Document) {
        val syntaxSnapshot: SyntaxSnapshot?
        if (dataHolder is Document) {
            syntaxSnapshot = dataHolder.getUserData(SYNTAX_SNAPSHOT_KEY)
            dataHolder.putUserData(SYNTAX_SNAPSHOT_KEY, null)
        } else {
            syntaxSnapshot = dataHolder.getUserData(SYNTAX_SNAPSHOT_KEY_RAW)
            dataHolder.putUserData(SYNTAX_SNAPSHOT_KEY_RAW, null)
        }
        if (syntaxSnapshot != null) {
            setCurrentSnapshot(document, syntaxSnapshot.withTimestamp(document.modificationStamp))
        }
    }
}
