# Hextant - A structural editor

**Hextant is in a very early stage of development. It is neither recommended nor possible to use Hextant for production.
If you want to try an example to grasp the key concepts of Hextant,  
please refer to [this](https://github.com/NKb03/Hextant/wiki/The-calculator-example) wiki page.   
If you want to learn what I mean with structural editor you can read [this](https://github.com/NKb03/Hextant/wiki/What-is-a-structural-editor).  
Generally the [wiki](https://github.com/NKb03/Hextant/wiki) is the best source for information.    

## Getting Started

### Prerequisites
- Java 1.8 (not higher not lower)
- Kotlin 1.3.11
- [KRef](https://github.com/NKB03/KRef) (to install just follow the build instructions in the [README](https://github.com/NKb03/KRef/blob/master/README.md))
- [ReaKtive](https://github.com/NKB03/ReaKtive) (to install just follow the build instructions in the [README](https://github.com/NKb03/ReaKtive/blob/master/README.md))

### Installing
To install Hextant you need to follow these steps:
- Clone the repository `git clone https://github.com/NKB03/Hextant <target_dir>`
- Build with gradle: `gradle build`
- Open the project in Intellij or any other IDE
If any errors occur while installing please feel free to create an issue or write me an e-mail.

### Running tests
To run the tests you IntelliJ and the Kotlin Spek Plugin.  
In Intellij:
- Edit Run configurations
- Add new configuration
- Select "Spek - JVM"
- For type select "Package"
- For package select "hextant-core"
- For module select "hextant-core.test"  
Testing via gradle is not supported.  

### Running the Lisp-editor
To try the Lisp-editor you just have to run the JavaFx Application in the file `\hextant-lisp\src\test\kotlin\hextant\lisp\LispEditorTest.kt` relative from the project root.

### Running the calculator
To try the calculator run the JavaFX Application in the file  `hextant-expr\src\test\kotlin\hextant.expr\ExprEditorViewTest.kt`.

## Contributing
If you want to contribute to Hextant there are three ways of doing it.
1. You can help improving the core of Hextant. Therefore you must fork the repository and create a pull request after making your changes.
2. You can create a plugin for a new language currently not supported by Hextant. I'm currently working on the documentation on how to develop new extensions, but until I have completed that (and also after I completed it), feel free to contact me via e-mail to get help.
3. If you are fluent in CSS you can create new stylesheets, to try this just modify the stylesheet in the file `hextant-core\src\main\resources\hextant\core\style.css`. This is very much appreciated as I'm not an expert in CSS at all.
To try your new stylesheet just run either the Lisp-editor or the calculator as described above. 

## Authors
- Nikolaus Knop (niko.knop003@gmail.com)
