package com.hulylabs.treesitter.language

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.treesitter.TreeSitterRust
import org.treesitter.TreeSitterTypescript
import org.treesitter.TreeSitterZig
import org.treesitter.TreeSitterSvelte
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterAstro

@Service
class LanguageRegistry(
    private val coroutineScope: CoroutineScope,
) {
    private val LOG = Logger.getInstance(LanguageRegistry::class.java)
    private val languageNames = HashMap<String, String>()
    private val languageHighlights = HashMap<String, HashMap<LanguageSymbol, String>>()

    init {
        coroutineScope.launch {
            val text = withContext(Dispatchers.IO) {
                Language::class.java.getResource("/queries/known-languages.json")!!.readText()
            }
            val json = Json { ignoreUnknownKeys = true }
            json.parseToJsonElement(text).jsonObject.forEach({ languageName, languageJson ->
                languageJson.jsonObject["extensions"]?.jsonArray?.forEach { extension ->
                    languageNames[extension.jsonPrimitive.content] = languageName
                }
            })
            for (languageName in languageNames.values) {
                val highlightsText = withContext(Dispatchers.IO) {
                    Language::class.java.getResource("/queries/$languageName/highlights-simple.scm")?.readText()
                }
                if (highlightsText == null) {
                    LOG.warn("No highlights-simple.scm found for $languageName")
                    continue
                }
                languageHighlights[languageName] = HashMap()
                highlightsText.lineSequence().map { line -> line.trim().split(" ") }.forEach { split ->
                    val nodeName = split[0].substring(1, split[0].length - 1)
                    val isNamed = split[0].startsWith('(') && split[0].endsWith(')')
                    val captureName = split[1].substring(1)
                    languageHighlights[languageName]!![LanguageSymbol(nodeName, isNamed)] = captureName
                }
            }
        }
    }

    fun getLanguage(extension: String): Language? {
        languageNames[extension]?.let { languageName ->
            when (languageName) {
                "rust" -> return Language(TreeSitterRust(), languageName)
                "typescript" -> return Language(TreeSitterTypescript(), languageName)
                "zig" -> return Language(TreeSitterZig(), languageName)
                "svelte" -> return Language(TreeSitterSvelte(), languageName)
                "javascript" -> return Language(TreeSitterJavascript(), languageName)
                "astro" -> return Language(TreeSitterAstro(), languageName)
                else -> {
                    LOG.warn("Unknown language: $languageName")
                }
            }
        }
        return null
    }

    fun getLanguageHighlights(language: Language): Map<LanguageSymbol, String>? {
        return languageHighlights[language.name]
    }
}
