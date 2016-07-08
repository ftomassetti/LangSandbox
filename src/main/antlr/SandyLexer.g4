lexer grammar SandyLexer;

// Whitespace
NEWLINE            : '\r\n' | 'r' | '\n' ;

// Keywords
VAR                : 'var' ;
VAL                : 'val' ;

// Literals
TRUE               : 'true' ;
FALSE              : 'false' ;
INTLIT             : '0'|[1-9][0-9]* ;
DECLIT             : '0'|[1-9][0-9]* '.' [0-9]+ ;

// Operators
PLUS               : '+' ;
MINUS              : '-' ;
ASTERISK           : '*' ;
DIVISION           : '/' ;
EQUAL              : '==' ;
ASSIGN             : '=' ;
LPAREN             : '(' ;
RPAREN             : ')' ;

// Identifiers
VID                : [_]*[a-z][A-Za-z0-9_]* ;
