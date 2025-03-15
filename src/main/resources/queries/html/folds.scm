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