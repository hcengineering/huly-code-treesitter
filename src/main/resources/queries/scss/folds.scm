((block "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((keyframe_block_list "{" @start "}" @end) @fold
  (#set! fold.text "{...}"))
((import_statement) @fold
  (#set! fold.combined-lines)
  (#set! fold.text "@import ..."))
((comment) @fold
  (#set! fold.text "/* ... */"))