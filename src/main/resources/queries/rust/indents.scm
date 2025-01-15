[
    ((where_clause) _ @end)
    (field_expression)
    (call_expression)
    (assignment_expression)
    (let_declaration)
    (let_chain)
    (await_expression)
] @indent

(_ "[" @start "]" @end) @indent
(_ "<" @start ">" @end) @indent
(_ "{" @start "}" @end) @indent
(_ "(" @start ")" @end) @indent