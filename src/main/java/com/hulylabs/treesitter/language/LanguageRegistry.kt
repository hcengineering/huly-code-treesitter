package com.hulylabs.treesitter.language

import com.hulylabs.treesitter.query.Query
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.treesitter.TSLanguage

@Service
class LanguageRegistry(
    private val coroutineScope: CoroutineScope,
) {
    private val LOG = Logger.getInstance(LanguageRegistry::class.java)
    private val extensions = HashMap<String, String>()
    private val languages = HashMap<String, Language>()

    private fun parseSimpleQuery(queryText: String): Map<LanguageSymbol, String> {
        return queryText.lineSequence().map { line ->
            val split = line.trim().split(" ")
            val nodeName = split[0].substring(1, split[0].length - 1)
            val isNamed = split[0].startsWith('(') && split[0].endsWith(')')
            val captureName = split[1].substring(1)
            Pair(LanguageSymbol(nodeName, isNamed), captureName)
        }.toMap()
    }

    init {
        coroutineScope.launch {
            val text = withContext(Dispatchers.IO) {
                Language::class.java.getResource("/queries/known-languages.json")!!.readText()
            }
            val json = Json { ignoreUnknownKeys = true }
            val knownLanguagesJson = json.parseToJsonElement(text).jsonObject
            knownLanguagesJson.forEach({ languageName, languageJson ->
                languageJson.jsonObject["extensions"]?.jsonArray?.forEach { extension ->
                    extensions[extension.jsonPrimitive.content] = languageName
                }
            })
            val pluginClassLoader = LanguageRegistry::class.java.classLoader
            for (languageEntry in knownLanguagesJson) {
                val languageName = languageEntry.key
                val className = languageEntry.value.jsonObject["className"]?.jsonPrimitive?.content
                if (className == null) {
                    LOG.warn("No className found for $languageName")
                    continue
                }
                val highlightsText = withContext(Dispatchers.IO) {
                    Language::class.java.getResource("/queries/$languageName/highlights-simple.scm")?.readText()
                }
                if (highlightsText == null) {
                    LOG.warn("No highlights-simple.scm found for $languageName")
                    continue
                }
                val languageHighlights = parseSimpleQuery(highlightsText)
                try {
                    val clazz = pluginClassLoader.loadClass(className)
                    val constructor = clazz.getConstructor()
                    val tsLanguage = constructor.newInstance() as TSLanguage
                    val language = Language(tsLanguage, languageName, languageHighlights)
                    launch {
                        val queryData = withContext(Dispatchers.IO) {
                            Language::class.java.getResource("/queries/$languageName/indents.scm")?.readBytes()
                        }
                        if (queryData != null) {
                            val query = Query(tsLanguage, queryData, mapOf())
                            language.setIndentQuery(query)
                        }
                    }
                    languages[languageName] = language
                } catch (e: Exception) {
                    LOG.warn("Class not found for $languageName: $e")
                    continue
                }
            }
        }
    }

    fun getLanguage(extension: String): Language? {
        return extensions[extension]?.let { languageName -> languages[languageName] }
    }
}
