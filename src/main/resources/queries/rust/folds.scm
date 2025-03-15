((macro_definition "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((token_tree_pattern "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((token_tree "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((declaration_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((enum_variant_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((field_declaration_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((use_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((field_initializer_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((match_block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((struct_pattern "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))

((macro_definition "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((macro_definition "[" @start "]" @end) @fold
    (#set! fold.text "[...]"))
((token_tree_pattern "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((token_tree_pattern "[" @start "]" @end) @fold
    (#set! fold.text "[...]"))
((token_tree "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((token_tree "[" @start "]" @end) @fold
  (#set! fold.text "[...]"))

((use_declaration . "use") @fold
  (#set! fold.text "use ...")
  (#set! fold.combined-lines)
  (#set! fold.collapsed))
((block_comment) @fold
  (#set! fold.text "/* ... */"))
((block_comment outer: _) @fold
  (#set! fold.text "/** ... */"))
((block_comment inner: _) @fold
  (#set! fold.text "/*! ... */"))
((line_comment !inner !outer) @fold
  (#set! fold.combined-lines)
  (#set! fold.text "// ..."))
((line_comment outer: _) @fold
  (#set! fold.combined-lines)
  (#set! fold.text "/// ..."))
((line_comment inner: _) @fold
  (#set! fold.combined-lines)
  (#set! fold.text "//! ..."))
((arguments) @fold
  (#set! fold.text "(...)"))
((parameters) @fold
  (#set! fold.text "(...)"))
(_ return_type: (_type) @fold)
((array_expression) @fold
  (#set! fold.text "[...]"))
((where_clause) @fold
  (#set! fold.text "where ..."))