### Larger ideas

### Medium term ideas

- consider using KSP instead of kapt for the editor codegen
- navigate using up and down arrows -> focus the according sub-editor of the next/previous item in the ListEditor
- command to update editor result before using it
- store control arguments in clipboard
  - how to apply to appropriate control
  - or store control config in `Editor.viewConfig`...
- extractor commands (replace parent with one of its children)
- take into account the expander/content relationship when querying applicable commands
  - does a simple delegation expander->content, content->expander suffice?
- flexible mode for `ListEditorControl`, where user can add and remove "line breaks"
- register control factories in plugin initializer instead of annotations
- look into the JavaFX `TextField`-API - maybe the `HextantTextField` could be smarter

### Fixes

- add cut command (Ctrl+X)
- the ListEditor result is sometimes faulty
- record edits when updating control arguments
- shortcuts to move list objects up and down (right and left)
- make completion popups sleeker