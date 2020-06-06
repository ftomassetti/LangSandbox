[![Apache 2.0 License][license badge]][Apache 2.0 License]&nbsp;
![build status][travis badge]

# LangSandbox

This project is used to illustrate how to build a programming language.
The code present here is discussed in a series of articles.

From these series of tutorials I derived a [book on creating languages].

You may be interested in the companion project ([Kanvas]) where we show how to build an editor for your language.

1. [Define the lexer]
2. [Define the parser]
3. [Build an editor with syntax highlighting]  (the code for this part is in [this project][Kanvas])
4. [Build an editor with autocompletion]  (the code for this part is in [this project][Kanvas])
5. [Map the parse tree to the abstract syntax tree]
6. [Transform the abstract syntax tree]
7. [Validate the abstract syntax tree]
8. [Generate bytecode]

I hope you enjoy and please let me know if you have issues, ideas, comments or any sort of feedback!

<!-----------------------------------------------------------------------------
                               REFERENCE LINKS
------------------------------------------------------------------------------>

[Kanvas]: https://github.com/ftomassetti/kanvas "Visit the Kanvas repository on GitHub"
[book on creating languages]: https://tomassetti.me/create-languages "'How to Create Pragmatic, Lightweight Languages' by Federico Tomassetti"

[Apache 2.0 License]: ./LICENSE

<!-- badges -->

[license badge]: https://img.shields.io/badge/license-Apache%202.0-00b5da.svg
[travis badge]: https://travis-ci.org/ftomassetti/LangSandbox.svg?branch=master "Travis CI Build status for EditorConfig code-styles consistency validation"

<!-- tutorials -->

[Define the lexer]: https://tomassetti.me/getting-started-with-antlr-building-a-simple-expression-language/ "Read the tutorial on tomassetti.me"
[Define the parser]: https://tomassetti.me/building-and-testing-a-parser-with-antlr-and-kotlin/ "Read the tutorial on tomassetti.me"
[Build an editor with syntax highlighting]: https://tomassetti.me/how-to-create-an-editor-with-syntax-highlighting-dsl/ "Read the tutorial on tomassetti.me"
[Build an editor with autocompletion]: https://tomassetti.me/autocompletion-editor-antlr/ "Read the tutorial on tomassetti.me"
[Map the parse tree to the abstract syntax tree]: https://tomassetti.me/parse-tree-abstract-syntax-tree/ "Read the tutorial on tomassetti.me"
[Transform the abstract syntax tree]: https://tomassetti.me/model-to-model-transformations/ "Read the tutorial on tomassetti.me"
[Validate the abstract syntax tree]: https://tomassetti.me/building-compiler-language-validation/ "Read the tutorial on tomassetti.me"
[Generate bytecode]: https://tomassetti.me/generating-bytecode/ "Read the tutorial on tomassetti.me"

<!-- EOF -->
