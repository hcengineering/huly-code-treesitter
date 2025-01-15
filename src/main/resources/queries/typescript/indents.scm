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