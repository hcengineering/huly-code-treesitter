((export_clause "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((named_imports "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((statement_block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((switch_body "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((object "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((object_pattern "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((class_body "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((template_substitution) @fold
  (#set! fold.text "${...}"))

((switch_case ":" @start) @fold
  (#set! fold.text ": ..."))
((switch_default ":" @start) @fold
  (#set! fold.text ": ..."))

((array "[" @start "]" @end) @fold
  (#set! fold.text "[...]"))
((array_pattern "[" @start "]" @end) @fold
  (#set! fold.text "[...]"))

((parenthesized_expression "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((arguments "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((formal_parameters "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))

((import_statement) @fold
  (#set! fold.combined-lines)
  (#set! fold.collapsed)
  (#set! fold.text "import ..."))
((export_statement) @fold
  (#set! fold.combined-lines)
  (#set! fold.text "export ..."))

((comment) @fold
  (#match? @fold "^//")
  (#set! fold.combined-lines)
  (#set! fold.text "// ..."))

((comment) @fold
  (#match? @fold "^/\\*[^*]")
  (#set! fold.text "/* ... */"))
((comment) @fold
  (#match? @fold "^(?s)^/\\*\\*[^*].*\\*/$")
  (#set! fold.text "/** ... */"))