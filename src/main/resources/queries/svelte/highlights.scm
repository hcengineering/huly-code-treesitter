; Copyright (c) 2025 Huly Labs
; Forked and adapted from nvim-treesitter (https://github.com/nvim-treesitter/nvim-treesitter)
(tag_name) @tag

; (erroneous_end_tag_name) @error ; we do not lint syntax errors
(comment) @comment

(attribute_name) @tag.attribute

((attribute
  (quoted_attribute_value) @string)
  (#set! priority 99))

(text) @none

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading)
  (#eq? @_tag "title"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.1)
  (#eq? @_tag "h1"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.2)
  (#eq? @_tag "h2"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.3)
  (#eq? @_tag "h3"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.4)
  (#eq? @_tag "h4"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.5)
  (#eq? @_tag "h5"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.heading.6)
  (#eq? @_tag "h6"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.strong)
  (#any-of? @_tag "strong" "b"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.italic)
  (#any-of? @_tag "em" "i"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.strikethrough)
  (#any-of? @_tag "s" "del"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.underline)
  (#eq? @_tag "u"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.raw)
  (#any-of? @_tag "code" "kbd"))

((element
  (start_tag
    (tag_name) @_tag)
  (text) @markup.link.label)
  (#eq? @_tag "a"))

((attribute
  (attribute_name) @_attr
  (quoted_attribute_value
    (attribute_value) @string.special.url))
  (#any-of? @_attr "href" "src"))

[
  "<"
  ">"
  "</"
  "/>"
] @tag.delimiter

(start_tag
  "<" @tag.constructor.delimiter
  (
      (tag_name) @_tag
      (#match? @_tag "^[A-Z]")
  )
  ">" @tag.constructor.delimiter
)

(self_closing_tag
  "<" @tag.constructor.delimiter
  (
      (tag_name) @_tag
      (#match? @_tag "^[A-Z]")
  )
  "/>" @tag.constructor.delimiter
)

(end_tag
  "</" @tag.constructor.delimiter
  (
      (tag_name) @_tag
      (#match? @_tag "^[A-Z]")
  )
  ">" @tag.constructor.delimiter
)

"=" @operator

(doctype) @constant

"<!" @tag.delimiter

(entity) @character.special

(raw_text) @none

(
  (tag_name) @tag.constructor
  (#match? @tag.constructor "^[A-Z]")
)

(start_tag
  (
    (tag_name) @_tag
    (#match? @_tag "^[A-Z]")
  )
  (attribute
    (attribute_name) @tag.property
  )
)

(self_closing_tag
  (
    (tag_name) @_tag
    (#match? @_tag "^[A-Z]")
  )
  (attribute
    (attribute_name) @tag.property
  )
)

[
  "as"
  "key"
  "html"
  "snippet"
  "render"
] @keyword

"const" @keyword.modifier

[
  "if"
  "else"
  "then"
] @keyword.conditional

"each" @keyword.repeat

[
  "await"
  "then"
] @keyword.coroutine

"catch" @keyword.exception

"debug" @keyword.debug

"{" @punctuation.bracket.braces.left
"}" @punctuation.bracket.braces.right

[
  "#"
  ":"
  "/"
  "@"
] @tag.delimiter

(snippet_name) @function

(
  (attribute
    (attribute_name) @tag.directive
  )
  (#match? @tag.directive "^[a-z]+\\:")
)