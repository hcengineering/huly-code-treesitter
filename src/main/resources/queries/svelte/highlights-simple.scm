(tag_name) @tag
(comment) @comment
(attribute_name) @tag.attribute
(quoted_attribute_value) @string
(text) @none
"<" @tag.delimiter
">" @tag.delimiter
"</" @tag.delimiter
"/>" @tag.delimiter
"=" @operator
(doctype) @constant
"<!" @tag.delimiter
(entity) @character.special
(raw_text) @none
"as" @keyword
"key" @keyword
"html" @keyword
"snippet" @keyword
"render" @keyword
"const" @keyword.modifier
"if" @keyword.conditional
"else" @keyword.conditional
"then" @keyword.conditional
"each" @keyword.repeat
"await" @keyword.coroutine
"then" @keyword.coroutine
"catch" @keyword.exception
"debug" @keyword.debug
"{" @punctuation.bracket.braces.left
"}" @punctuation.bracket.braces.right
"#" @tag.delimiter
":" @tag.delimiter
"/" @tag.delimiter
"@" @tag.delimiter