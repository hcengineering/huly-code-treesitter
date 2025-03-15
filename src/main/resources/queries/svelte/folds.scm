((element (start_tag) @start (_) @end .) @fold
  (#set! range.inner))
((start_tag (tag_name) @start ">" @end) @fold
  (#set! range.inner))
((self_closing_tag (tag_name) @start "/>" @end) @fold
  (#set! range.inner))
(script_element (raw_text) @fold)
(style_element (raw_text) @fold)
((comment) @fold
  (#set! fold.text "<!--...-->"))

((if_statement (if_start) @start (else_if_block) @end) @fold
  (#set! range.inner))
((if_statement (if_start) @start (else_block) @end) @fold
  (#set! range.inner))
((if_statement (if_start) @start (if_end) @end) @fold
  (#set! range.inner))
((else_if_block (else_if_start) @start) @fold
  (#set! range.inner))
((else_block (else_start) @start) @fold
  (#set! range.inner))
((each_statement (each_start) @start [(else_block) (each_end)] @end) @fold
  (#set! range.inner))
((await_statement (await_start) @start (then_block) @end) @fold
  (#set! range.inner))
((await_statement (await_start) @start (catch_block) @end) @fold
  (#set! range.inner))
((await_statement (await_start) @start (await_end) @end) @fold
  (#set! range.inner))
((then_block (then_start) @start) @fold
  (#set! range.inner))
((catch_block (catch_start) @start) @fold
  (#set! range.inner))

((expression "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))