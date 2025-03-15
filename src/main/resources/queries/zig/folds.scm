((block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((struct_declaration "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((opaque_declaration "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((enum_declaration "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((union_declaration "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((error_set_declaration "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((switch_expression "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((initializer_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))

((comment) @fold
  (#match? @fold "^(?://(?:[^/!]|//).*|//)$")
  (#set! fold.combined-lines)
  (#set! fold.text "// ..."))
((comment) @fold
  (#match? @fold "^(?:///[^/].*|///)$")
  (#set! fold.combined-lines)
  (#set! fold.text "/// ..."))
((comment) @fold
  (#match? @fold "^//!")
  (#set! fold.combined-lines)
  (#set! fold.text "//! ..."))

((parameters "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((call_expression "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((arguments "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((multiline_string) @fold
  (#set! fold.text "\\\\ ..." @fold))