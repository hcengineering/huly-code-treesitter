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
    private val extensions = HashMap<String, String>()
    private val languages = HashMap<String, Language>()

    init {
        coroutineScope.launch {
            val text = withContext(Dispatchers.IO) {
                Language::class.java.getResource("/queries/known-languages.json")!!.readText()
            }
            val json = Json { ignoreUnknownKeys = true }
            val knownLanguagesJson = json.parseToJsonElement(text).jsonObject
            val languageNames = knownLanguagesJson.keys
            knownLanguagesJson.forEach({ languageName, languageJson ->
                languageJson.jsonObject["extensions"]?.jsonArray?.forEach { extension ->
                    extensions[extension.jsonPrimitive.content] = languageName
                }
            })
            for (languageName in languageNames) {
                val highlightsText = withContext(Dispatchers.IO) {
                    Language::class.java.getResource("/queries/$languageName/highlights-simple.scm")?.readText()
                }
                if (highlightsText == null) {
                    LOG.warn("No highlights-simple.scm found for $languageName")
                    continue
                }
                val languageHighlights = HashMap<LanguageSymbol, String>()
                highlightsText.lineSequence().map { line -> line.trim().split(" ") }.forEach { split ->
                    val nodeName = split[0].substring(1, split[0].length - 1)
                    val isNamed = split[0].startsWith('(') && split[0].endsWith(')')
                    val captureName = split[1].substring(1)
                    languageHighlights[LanguageSymbol(nodeName, isNamed)] = captureName
                }
                val language = when (languageName) {
                    "rust" -> Language(TreeSitterRust(), languageName, languageHighlights)
                    "typescript" -> Language(TreeSitterTypescript(), languageName, languageHighlights)
                    "zig" -> Language(TreeSitterZig(), languageName, languageHighlights)
                    "svelte" -> Language(TreeSitterSvelte(), languageName, languageHighlights)
                    "javascript" -> Language(TreeSitterJavascript(), languageName, languageHighlights)
                    "astro" -> Language(TreeSitterAstro(), languageName, languageHighlights)
                    else -> {
                        LOG.warn("Unknown language: $languageName")
                        null
                    }
                }
                if (language == null) continue
                languages[languageName] = language
            }
        }
    }

    fun getLanguage(extension: String): Language? {
        return extensions[extension]?.let { languageName -> languages[languageName] }
    }
}
