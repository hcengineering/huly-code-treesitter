package com.hulylabs.treesitter.language

import com.hulylabs.treesitter.rusty.TreeSitterNativeLanguageRegistry
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val highlightsData = withContext(Dispatchers.IO) {
                    Language::class.java.getResource("/queries/$languageName/highlights.scm")?.readBytes()
                }
                if (highlightsData == null) {
                    LOG.warn("No highlights.scm found for $languageName")
                    continue
                }
                try {
                    val clazz = pluginClassLoader.loadClass(className)
                    val constructor = clazz.getConstructor()
                    val tsLanguage = constructor.newInstance() as TSLanguage
                    val registry =
                        ApplicationManager.getApplication().getService(TreeSitterNativeLanguageRegistry::class.java)
                    val language = Language(languageName, registry.registerLanguage(languageName, tsLanguage))

                    language.nativeHighlights = registry.addHighlightQuery(language, highlightsData)
                    languages[languageName] = language
                    launch {
                        val queryData = withContext(Dispatchers.IO) {
                            Language::class.java.getResource("/queries/$languageName/indents.scm")?.readBytes()
                        }
                        if (queryData != null) {
                            ApplicationManager.getApplication().getService(TreeSitterNativeLanguageRegistry::class.java)
                                .addIndentQuery(language, queryData)
                        }
                    }
                    launch {
                        val queryData = withContext(Dispatchers.IO) {
                            Language::class.java.getResource("/queries/$languageName/folds.scm")?.readBytes()
                        }
                        if (queryData != null) {
                            ApplicationManager.getApplication().getService(TreeSitterNativeLanguageRegistry::class.java)
                                .addFoldQuery(language, queryData)
                        }
                    }
                    launch {
                        val queryData = withContext(Dispatchers.IO) {
                            Language::class.java.getResource("/queries/$languageName/injections.scm")?.readBytes()
                        }
                        if (queryData != null) {
                            ApplicationManager.getApplication().getService(TreeSitterNativeLanguageRegistry::class.java)
                                .addInjectionQuery(language, queryData)
                        }
                    }
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
