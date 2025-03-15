((field_declaration_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((interface_type "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((expression_switch_statement "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((type_switch_statement "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((select_statement "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((literal_value "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))

((expression_case ":" @start _ @end .) @fold
  (#set! fold.text ": ..."))
((default_case ":" @start _ @end .) @fold
  (#set! fold.text ": ..."))
((type_case ":" @start _ @end .) @fold
  (#set! fold.text ": ..."))
((communication_case ":" @start _ @end .) @fold
  (#set! fold.text ": ..."))

((argument_list "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((const_declaration "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((type_declaration "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((import_spec_list "(" @start ")" @end) @fold
  (#set! fold.text "(...)")
  (#set! fold.collapsed))
((var_spec_list "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((parameter_list "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))

((raw_string_literal "`" @start "`" @end) @fold
  (#set! fold.text "`...`"))
((comment) @fold
  (#match? @fold "^/\\*")
  (#set! fold.text "/* ... */"))
((comment) @fold
  (#match? @fold "^//")
  (#set! fold.combined-lines)
  (#set! fold.text "// ..."))
