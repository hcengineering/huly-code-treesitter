[
    (call_expression)
    (assignment_expression)
    (member_expression)
    (lexical_declaration)
    (variable_declaration)
    (assignment_expression)
    (if_statement)
    (for_statement)
] @indent

(_ "[" @start "]" @end) @indent
(_ "<" @start ">" @end) @indent
(_ "{" @start "}" @end) @indent
(_ "(" @start ")" @end) @indent

(jsx_opening_element ">" @end) @indent

(jsx_element
  (jsx_opening_element) @start
  (jsx_closing_element)? @end) @indent