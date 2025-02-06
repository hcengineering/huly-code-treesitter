; Copyright (c) 2025 Huly Labs
; Forked and adapted from nvim-treesitter (https://github.com/nvim-treesitter/nvim-treesitter)

; html(`...`), html`...`, sql(`...`), etc.
(call_expression
  function: (identifier) @injection.language
  arguments: [
    (arguments
      (template_string) @injection.content)
    (template_string) @injection.content
  ]
  (#match? @injection.language "^[a-zA-Z][a-zA-Z0-9]*$")
  (#offset! @injection.content 1 -1)
  (#set! injection.include-children)
  ; Languages excluded from auto-injection due to special rules
  ; - svg uses the html parser
  ; - css uses the styled parser
  (#not-any-of? @injection.language "svg" "css"))

; svg`...` or svg(`...`)
(call_expression
  function: (identifier) @_name
  (#eq? @_name "svg")
  arguments: [
    (arguments
      (template_string) @injection.content)
    (template_string) @injection.content
  ]
  (#offset! @injection.content 1 -1)
  (#set! injection.include-children)
  (#set! injection.language "html"))

; css`<css>`, keyframes`<css>`
(call_expression
  function: (identifier) @_name
  (#any-of? @_name "css" "keyframes")
  arguments: (template_string) @injection.content
  (#offset! @injection.content 1 -1)
  (#set! injection.include-children)
  (#set! injection.language "styled"))

; styled.div`<css>`
(call_expression
  function: (member_expression
    object: (identifier) @_name
    (#eq? @_name "styled"))
  arguments: ((template_string) @injection.content
    (#offset! @injection.content 1 -1)
    (#set! injection.include-children)
    (#set! injection.language "styled")))

; styled(Component)`<css>`
(call_expression
  function: (call_expression
    function: (identifier) @_name
    (#eq? @_name "styled"))
  arguments: ((template_string) @injection.content
    (#offset! @injection.content 1 -1)
    (#set! injection.include-children)
    (#set! injection.language "styled")))

; styled.div.attrs({ prop: "foo" })`<css>`
(call_expression
  function: (call_expression
    function: (member_expression
      object: (member_expression
        object: (identifier) @_name
        (#eq? @_name "styled"))))
  arguments: ((template_string) @injection.content
    (#offset! @injection.content 1 -1)
    (#set! injection.include-children)
    (#set! injection.language "styled")))

; styled(Component).attrs({ prop: "foo" })`<css>`
(call_expression
  function: (call_expression
    function: (member_expression
      object: (call_expression
        function: (identifier) @_name
        (#eq? @_name "styled"))))
  arguments: ((template_string) @injection.content
    (#offset! @injection.content 1 -1)
    (#set! injection.include-children)
    (#set! injection.language "styled")))

; el.innerHTML = `<html>`
(assignment_expression
  left: (member_expression
    property: (property_identifier) @_prop
    (#any-of? @_prop "outerHTML" "innerHTML"))
  right: (template_string) @injection.content
  (#offset! @injection.content 1 -1)
  (#set! injection.include-children)
  (#set! injection.language "html"))

; el.innerHTML = '<html>'
(assignment_expression
  left: (member_expression
    property: (property_identifier) @_prop
    (#any-of? @_prop "outerHTML" "innerHTML"))
  right: (string
    (string_fragment) @injection.content)
  (#set! injection.language "html"))

; @Component({
;   styles: [`<css>`]
; })
(decorator
  (call_expression
    function: ((identifier) @_name
      (#eq? @_name "Component"))
    arguments: (arguments
      (object
        (pair
          key: ((property_identifier) @_prop
            (#eq? @_prop "styles"))
          value: (array
            ((template_string) @injection.content
              (#offset! @injection.content 1 -1)
              (#set! injection.include-children)
              (#set! injection.language "css"))))))))

; @Component({
;   styles: `<css>`
; })
(decorator
  (call_expression
    function: ((identifier) @_name
      (#eq? @_name "Component"))
    arguments: (arguments
      (object
        (pair
          key: ((property_identifier) @_prop
            (#eq? @_prop "styles"))
          value: ((template_string) @injection.content
            (#set! injection.include-children)
            (#offset! @injection.content 1 -1)
            (#set! injection.language "css")))))))

; Styled Jsx <style jsx>
(jsx_element
  (jsx_opening_element
    (identifier) @_name
    (#eq? @_name "style")
    (jsx_attribute) @_attr
    (#eq? @_attr "jsx"))
  (jsx_expression
    ((template_string) @injection.content
      (#set! injection.language "css"))
    (#offset! @injection.content 1 -1)))
