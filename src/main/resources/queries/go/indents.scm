[
  (import_declaration)
  (const_declaration)
  (var_declaration)
  (type_declaration)

  (func_literal)

  (expression_case)
  (communication_case)
  (type_case)
  (default_case)
] @indent

(_ "[" @start "]" @end) @indent
(_ "<" @start ">" @end) @indent
(_ "{" @start "}" @end) @indent
(_ "(" @start ")" @end) @indent
