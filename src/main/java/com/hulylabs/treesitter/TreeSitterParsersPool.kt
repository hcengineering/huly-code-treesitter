package com.hulylabs.treesitter

import org.treesitter.TSParser

internal object TreeSitterParsersPool {
    private val parsers: MutableList<TSParser> = mutableListOf()

    fun <T> withParser(func: (TSParser) -> T): T {
        val parser = synchronized(parsers) {
            if (parsers.isNotEmpty()) {
                parsers.removeAt(parsers.size - 1)
            } else {
                TSParser()
            }
        }
        parser.setIncludedRanges(arrayOf())
        parser.reset()
        val result = func(parser)
        synchronized(parsers) {
            parsers.add(parser)
        }
        return result
    }
}