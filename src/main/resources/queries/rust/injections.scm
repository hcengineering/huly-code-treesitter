; Copyright (c) 2025 Huly Labs
; Forked and adapted from nvim-treesitter (https://github.com/nvim-treesitter/nvim-treesitter)
(macro_definition
  (macro_rule
    left: (token_tree_pattern) @injection.content
    (#set! injection.language "rust")))

(macro_definition
  (macro_rule
    right: (token_tree) @injection.content
    (#set! injection.language "rust")))

((macro_invocation
  macro: (identifier) @injection.language
  (token_tree) @injection.content)
  (#any-of? @injection.language "html" "json"))
