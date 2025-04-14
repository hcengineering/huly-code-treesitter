((if_statement ":" @start alternative: (_) @end) @fold
  (#set! fold.text ": ..."))
((elif_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((else_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((match_statement ":" @start) @fold
  (#set! fold.text ": ..."))
((case_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((for_statement ":" @start alternative: (_) @end) @fold
  (#set! fold.text ": ..."))
((while_statement ":" @start alternative: (_) @end) @fold
  (#set! fold.text ": ..."))
((try_statement ":" @start body: (_) @end) @fold
  (#set! fold.text ": ..."))
((except_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((except_group_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((finally_clause ":" @start) @fold
  (#set! fold.text ": ..."))
((with_statement ":" @start) @fold
  (#set! fold.text ": ..."))
((function_definition ":" @start) @fold
  (#set! fold.text ": ..."))
((class_definition ":" @start) @fold
  (#set! fold.text ": ..."))
((lambda ":" @start) @fold
  (#set! fold.text ": ..."))

((_ "[" @start "]" @end) @fold
  (#set! fold.text "[...]"))
((_ "(" @start ")" @end) @fold
  (#set! fold.text "(...)"))
((_ "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))

[
  (import_from_statement)
  (parameters)
  (argument_list)
  (parenthesized_expression)
  (generator_expression)
  (list_comprehension)
  (set_comprehension)
  (dictionary_comprehension)
  (tuple)
  (list)
  (set)
  (dictionary)
  (string)
] @fold

[
  (import_statement)
  (import_from_statement)
]+ @fold

