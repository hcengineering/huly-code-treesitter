; Copyright (c) 2025 Huly Labs
; Forked and adapted from nvim-treesitter (https://github.com/nvim-treesitter/nvim-treesitter)
((comment) @injection.content
  (#set! injection.language "comment"))

; <style>...</style>
; <style blocking> ...</style>
; Add "lang" to predicate check so that vue/svelte can inherit this
; without having this element being captured twice
((style_element
  (start_tag) @_no_type_lang
  (raw_text) @injection.content)
  (#not-match? @_no_type_lang "\\slang\\s*=.*")
  (#not-match? @_no_type_lang "\\stype\\s*=.*")
  (#set! injection.language "css"))

((style_element
  (start_tag
    (attribute
      (attribute_name) @_type
      (quoted_attribute_value
        (attribute_value) @_css)))
  (raw_text) @injection.content)
  (#eq? @_type "type")
  (#eq? @_css "text/css")
  (#set! injection.language "css"))

; <script>...</script>
; <script defer>...</script>
((script_element
  (start_tag) @_no_type_lang
  (raw_text) @injection.content)
  (#not-match? @_no_type_lang "\\slang\\s*=.*")
  (#not-match? @_no_type_lang "\\slang\\s*=.*")
  (#set! injection.language "javascript"))

; <script type="mimetype-or-well-known-script-type">
(script_element
  (start_tag
    (attribute
      (attribute_name) @_attr
      (#eq? @_attr "type")
      (quoted_attribute_value
        (attribute_value) @injection.mimetype)))
  (raw_text) @injection.content)

; <a style="/* css */">
((attribute
  (attribute_name) @_attr
  (quoted_attribute_value
    (attribute_value) @injection.content))
  (#eq? @_attr "style")
  (#set! injection.language "css"))

; lit-html style template interpolation
; <a @click=${e => console.log(e)}>
; <a @click="${e => console.log(e)}">
((attribute
  (quoted_attribute_value
    (attribute_value) @injection.content))
  (#match? @injection.content "^\\$\\{.*\\}$")
  (#offset! @injection.content 2 -1)
  (#set! injection.language "javascript"))

((attribute
  (attribute_value) @injection.content)
  (#match? @injection.content "^\\$\\{.*\\}$")
  (#offset! @injection.content 2 -1)
  (#set! injection.language "javascript"))

; <input type="checkbox" onchange="this.closest('form').elements.output.value = this.checked">
(attribute
  (attribute_name) @_name
  (#match? @_name "^on[a-z]+$")
  (quoted_attribute_value
    (attribute_value) @injection.content)
  (#set! injection.language "javascript"))


(frontmatter
  (frontmatter_js_block) @injection.content
  (#set! injection.language "typescript"))

(attribute_interpolation
  (attribute_js_expr) @injection.content
  (#set! injection.language "typescript"))

(attribute
  (attribute_backtick_string) @injection.content
  (#set! injection.language "typescript"))

(html_interpolation
  (permissible_text) @injection.content
  (#set! injection.language "typescript"))

(script_element
  (raw_text) @injection.content
  (#set! injection.language "typescript"))
