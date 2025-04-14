(if_statement ":" @start alternative: (_) @end) @indent
(elif_clause ":" @start) @indent
(else_clause ":" @start) @indent
(match_statement ":" @start) @indent
(case_clause ":" @start) @indent
(for_statement ":" @start alternative: (_) @end) @indent
(while_statement ":" @start alternative: (_) @end) @indent
(try_statement ":" @start body: (_) @end) @indent
(except_clause ":" @start) @indent
(except_group_clause ":" @start) @indent
(finally_clause ":" @start) @indent
(with_statement ":" @start) @indent
(function_definition ":" @start) @indent
(class_definition ":" @start) @indent
(lambda ":" @start) @indent
(_ "[" @start "]" @end) @indent
(_ "(" @start ")" @end) @indent
(_ "{" @start "}" @end) @indent