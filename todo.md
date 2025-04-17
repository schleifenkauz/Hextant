### Larger ideas

### Medium term ideas

- consider using KSP instead of kapt for the editor codegen
- navigate using up and down arrows -> focus the according sub-editor of the next/previous item in the ListEditor
- command to update editor result before using it
- store control arguments in clipboard
  - how to apply to appropriate control
  - or store control config in `Editor.viewConfig`...
- extractor commands (replace parent with one of its children)
- shortcuts to move list objects up and down (right and left)
- select multiple consecutive editors with ctrl+shift + arrow key
- take into account the expander/content relationship when querying applicable commands
  - does a simple delegation expander->content, content->expander suffice?
- flexible mode for `ListEditorControl`, where user can add and remove "line breaks"
- make completion popups sleeker

### Fixes

- add cut command (Ctrl+X)
- the ListEditor result is faulty