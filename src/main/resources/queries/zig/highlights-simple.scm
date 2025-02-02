(identifier) @variable
(builtin_type) @type.builtin
"anyframe" @type.builtin
"null" @constant.builtin
"unreachable" @constant.builtin
"undefined" @constant.builtin
(builtin_identifier) @function.builtin
"c" @variable.builtin
"..." @variable.builtin
"asm" @keyword
"defer" @keyword
"errdefer" @keyword
"test" @keyword
"error" @keyword
"const" @keyword
"var" @keyword
"struct" @keyword.type
"union" @keyword.type
"enum" @keyword.type
"opaque" @keyword.type
"async" @keyword.coroutine
"await" @keyword.coroutine
"suspend" @keyword.coroutine
"nosuspend" @keyword.coroutine
"resume" @keyword.coroutine
"fn" @keyword.function
"and" @keyword.operator
"or" @keyword.operator
"orelse" @keyword.operator
"return" @keyword.return
"if" @keyword.conditional
"else" @keyword.conditional
"switch" @keyword.conditional
"for" @keyword.repeat
"while" @keyword.repeat
"break" @keyword.repeat
"continue" @keyword.repeat
"usingnamespace" @keyword.import
"export" @keyword.import
"try" @keyword.exception
"catch" @keyword.exception
"volatile" @keyword.modifier
"allowzero" @keyword.modifier
"noalias" @keyword.modifier
"addrspace" @keyword.modifier
"align" @keyword.modifier
"callconv" @keyword.modifier
"linksection" @keyword.modifier
"pub" @keyword.modifier
"inline" @keyword.modifier
"noinline" @keyword.modifier
"extern" @keyword.modifier
"comptime" @keyword.modifier
"packed" @keyword.modifier
"threadlocal" @keyword.modifier
"=" @operator
"*=" @operator
"*%=" @operator
"*|=" @operator
"/=" @operator
"%=" @operator
"+=" @operator
"+%=" @operator
"+|=" @operator
"-=" @operator
"-%=" @operator
"-|=" @operator
"<<=" @operator
"<<|=" @operator
">>=" @operator
"&=" @operator
"^=" @operator
"|=" @operator
"!" @operator
"~" @operator
"-" @operator
"-%" @operator
"&" @operator
"==" @operator
"!=" @operator
">" @operator
">=" @operator
"<=" @operator
"<" @operator
"&" @operator
"^" @operator
"|" @operator
"<<" @operator
">>" @operator
"<<|" @operator
"+" @operator
"++" @operator
"+%" @operator
"-%" @operator
"+|" @operator
"-|" @operator
"*" @operator
"/" @operator
"%" @operator
"**" @operator
"*%" @operator
"*|" @operator
"||" @operator
".*" @operator
".?" @operator
"?" @operator
".." @operator
(character) @character
(string) @string
(multiline_string) @string
(integer) @number
(float) @number.float
(boolean) @boolean
(escape_sequence) @string.escape
"[" @punctuation.bracket.left
"]" @punctuation.bracket.right
"(" @punctuation.bracket.parentheses.left
")" @punctuation.bracket.parentheses.right
"{" @punctuation.bracket.braces.left
"}" @punctuation.bracket.braces.right
";" @punctuation.delimiter.semicolon
"." @punctuation.delimiter
"," @punctuation.delimiter.comma
":" @punctuation.delimiter
"=>" @punctuation.delimiter
"->" @punctuation.delimiter
(comment) @comment