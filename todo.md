# Short term improvements and fixes

1. Vim mode
    * When TextField is created it is editable
    * When a TextField loses focus or user hits ESCAPE it becomes uneditable
    * When user types I, TextField becomes editable
    * TextFields must stay focusable
    * When user exits Vim mode, all TextFields must become editable
    * Vim mode also affects command line
2. Improve command completion and focus of argument editors
    * Highlight matched parts
    * When command with parameters is expanded the argument editors need focus
3. Fix bug in inspection display, where warnings hide more concrete errors
    
# Long term goals

1. More default configuration in HextantApplication
    * Default shortcuts for undo, redo etc.
    * Default command line (probably also with shortcuts)
    * More default properties in HextantPlatform
2. Some kind of project managements (probably involves reworking plugins)
3. Configurability for inspections and commands 
    * Ability to disable/enable commands and inspections
    * Ability to configure highlighting of problems
4. Fix Memory leaks
    * Search for suspicious uses of HashMap and replace with WeakHashMap if needed
    * Write more tests to prevent regressions, when new features are added
5. Model and JavaFX implementation of project explorers (connected to 2.)
    * probably based on JavaFx TreeView
    * style should configurable through CSS
6. Get some reliable data on memory consumption
7. Depends on 6., if memory consumption is an issue rethink memory model
    * Some kind of virtual files (seen as roots of editor trees)
    * Virtual editors that map a virtual file to a specific editor
    * Must inspections highlight currently not opened files
    * Also connected to 4.
8. Improve intuitiveness and correctness of navigation
    * Command selection
    * Navigation inside problems and fixes
    * Traversal with arrow keys
    * Selection of multiple editors
    * etc.
9. Do we really need concurrency?