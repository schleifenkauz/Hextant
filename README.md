# Hextant - A structural editor

## What is Hextant?

Hextant is a structured editor. 
This means that when you use Hextant, 
what you're editing is not the textual representation, but the *Abstract Syntax Tree* of the thing you edit.
More precisely, Hextant itself is not a structured editor, 
but a framework for easily creating structured editors (or *AST-editors*) for different data formats and languages.

## What can be edited with Hextant?

In principle, everything, that has some well-defined syntax or format, can be edited with Hextant.
For example, one could use Hextant to edit:
- code written in a programming language
- some data written in markup language like XML
- an HTML-website
- an arithmetic expression

But nothing comes for free. 
To use Hextant for a specific programming language or data format, 
you need a plugin (written by you or someone else), 
that tells Hextant about the shape of the code or data you want to edit 
and what transformations the user should be able to apply to it.
As an example, watch this demo of a Hextant plugin for a language similar to Haskell:
<div></div>
<a href="http://www.youtube.com/watch?feature=player_embedded&v=09ni_mwsipc
" target="_blank"><img src="http://img.youtube.com/vi/09ni_mwsipc/0.jpg" 
alt="Video could not be loaded" width="560" height="315" border="10" /></a>

## How does it work?

As said above, Hextant is a structured editor (or AST-editor).
Each node in the Abstract Syntax Tree, you are editing, is represented by one editor in the *editor tree*.
Editors may either be leaves, or they may be composed of other editors.
Most of the time, the leave editors (also called **token editors**) are displayed as text fields. 
For example, an editor for arithmetic expressions may have editors for integer literals as the leaves. 
As the user types in some characters, the editor checks, 
that the string typed by the user is indeed a valid integer literal.   
![Demo couldn't be loaded](gif/literals.gif)  
One may argue, that this is at the end yet again a text-based approach,
but the distinction to an ordinary text-editor is,
that textual representation is only used in the leaves (or "tokens" in BNF-jargon) of the editor tree.
Note, for example, that this editor for integer literals would never recognize a string such as "1+2".  
To form such an expression, a **compound editor** is needed. 
Compound editors are editors that are composed of other editors.
For example in the following GIF there is an editor that is composed of two editors for integer literals
and one editor for an arithmetic operator.  
![Demo couldn't be loaded](gif/compound.gif)  
To quickly switch between the individual components of a compound editor without using the mouse,
you can use the shortcuts ``Ctrl+🡄`` and ``Ctrl+🡆``. 
By pressing ``Ctrl+M`` (for "select more") you select the parent of the currently focused editor
and the shortcut ``Ctrl+L`` (for "select less") lets you select the child of a compound editor that was focused last.  
You're probably not very impressed with an editor that can only edit expressions with one operator and two operands.
What we want is a structured editor that can be used to write down every possible arithmetic expression using the four basic operators.
This leads us to the next concept: **Expanders**.
An expander is an editor that acts as a placeholder that can be filled in by the user. 
Expanders can be in two different states. They can be either *unexpanded* or *expanded*.
In the unexpanded state the expander is displayed by a text field. 
If not focused, unexpanded are highlighted, so that the user knows that the has to fill in a gap in his program.
The user can type into that text field and then hit ``Enter`` to expand the text to a concrete editor.
For example, in the following GIF, the text "+" is expanded to a compound editor for binary expressions with "+" as the operator
and integer literals are expanded to editors for integer literals.  
![Demo couldn't be loaded](gif/expander.gif)  
To replace the editor that was expanded by the placeholder again), the user has to use the keyboard shortcut ``Ctrl+R``.  
Now suppose, our language of arithmetic expressions is extended with a form ``sum <expressions...>``, 
that sums up an arbitrary-size list of expressions.
To represent such an expression with a structured editor, 
an editor that is composed of arbitrarily many sub-editors of equal shape is needed.
In Hextant, such editors are called **list editors**.  
![Demo couldn't be loaded](gif/lists_cooler.gif)  
To add a new editor to a list editor, the shortcut ``Ctrl+Insert`` is used.
The shortcut ``Ctrl+Remove`` removes the selected item.
Like with compound editors, the arrow keys can be used to traverse the sub-editors. 
Note that using the summation sign in the above GIF is only possible,
because in Hextant, the display of editors is detached from there internal representation.  
A problem arises, when you have made an expression and now want to multiply it by three.
With what the editor is able to do so far, 
you would have to select the whole expression by repeated use of the ``Ctrl+M``-shortcut
and then press ``Ctrl+R`` to reset the whole thing.
Then you would type "*" into the expander, expand it, 
retype your whole expression on the left-hand side and put the integer literal "3" into the right-hand side.
This kind of redundancy is of course intolerable.
Therefore, Hextant supports **commands**, that implement common transformations of editors.
For example, the plugin for editing arithmetic expressions supports a ``wrap`` command,
that replaces some expression by binary expression with the selected expression as the left-hand side.
To execute a command, you have to select the editor on which the command is to be executed 
and then type the command into the command-line. 
By hitting ``Enter`` the command is then expanded and editors for the arguments that the command receives are created.
Typing ``Ctrl+R`` resets the command line.
To execute the command you have to type ``Ctrl+Enter``.
Commands that have no parameters, can be executed by just pressing ``Ctrl+Enter`` 
after typing them into the command line, without needing to expand them first. 
Some commands also have shortcuts assigned to them.  
![Demo couldn't be loaded](gif/commands.gif) 

## Why use Hextant?

Structural editors, and Hextant in particular Hextant, have a number of important advantages over traditional text-based IDEs.

- Structural editors **don't need to maintain the synchronization 
between textual and structural representation** of the edited program. 
This eliminates a big factor in terms of **IDE-performance**.
- The editor works with the same internal representation as the compiler/interpreter of the language does
So there is **no need for lexing and parsing** the program before compiling/evaluation it.
This reduces the time the programmer waits for the interpreter/compiler by a big margin and facilitates **integration of live-coding features** into plugins.
- The **decoupling of internal representation of the program and display on the screen** is an inherent feature of Hextant's architecture.
This enables the coexistence and mixing of more "text-like" and more **graphical views** on the edited program.
Another advantage following from the decoupling is the possibility of **programming language personalisation**
- Perhaps the most important point: 
In absence of a parser, the syntax of the languages supported by plugins are **extensible**.
Developers can create plugins that extend the features and syntax of languages defined by other plugins.
These syntax-extension plugins are **modular** in the sense, that different syntax-extensions plugins can be mixed seamlessly.
For each project, he works on, a programmer can put together a language that fits his specific needs by installing the adequate plugins.

For a more detailed discussion of these advantages and of solutions to inconveniences that arise with the use of structural editors, 
see [this](https://github.com/NKb03/Hextant/wiki/Why-structural-editors) article.

## How to get it working on my computer?

To build and run Hextant on your computer you need Git, version 1.8 of the Java Development Kit and the Intellij IDE.
Follow these steps:
- Clone the project: ```git clone https://github.com/NKB03/Hextant```.
- Open the project in IntelliJ and wait for the Gradle import to finish.
- Build the project.
- Use the "Launch Hextant" run-configuration to start Hextant.
- If you don't want to use IntelliJ simply run ``gradlew build`` 
and then use ``launch.bat`` or ``launch.sh`` depending on your OS.
- Now you should see a window, that looks like this:  
![Image couldn't be loaded](gif/launcher.png)
- To create a new project, use the command ``create <project-type> <project-name>``. 
Some available project types are ``Lisp Project"`` and ``Expression``. 
- In the window that pops up, you can select additional plugins, you want to use.
- Click ``Ok`` to open the newly created project.
- To open the project command line press ``Ctrl+G``.
- To save the project use ``Ctrl+S`` or type ``save`` into the project command line.
- To close the project and return to the launcher window use ``Ctrl+Q`` or type ``quit`` into the project command line.
- To open a project the command ``open <project>`` can be used from the launcher.
- Commands for renaming and deleting projects are also supported.

## How to contribute?

There are essentially three ways in which you can contribute to Hextant.

1. By trying out Hextant and suggesting possible improvements. 
If you notice some inconvenience while using Hextant or have an idea for a new feature/plugin just open a new issue.
2. By developing a plugin for your language of choice. 
You can refer to [this](https://github.com/NKb03/Hextant/wiki/Writing-plugins) tutorial to learn how to write plugins.
3. By working on the core-framework.

All three ways of contributing are greatly appreciated. 
If you have a question feel free to open an issue or write me an email (niko.knop003@gmail.com). 