parser grammar SandyParser;

options { tokenVocab=SandyLexer; }

sandyFile : lines=line+ ;

line      : statement (NEWLINE | EOF) ;

statement : varDeclaration # varDeclarationStatement
          | assignment     # assignmentStatement ;

varDeclaration : VAR assignment ;

assignment : ID ASSIGN expression ;

expression : left=expression operator=(DIVISION|ASTERISK) right=expression # binaryOperation
           | left=expression operator=(PLUS|MINUS) right=expression        # binaryOperation
           | LPAREN expression RPAREN # parenExpression
           | ID                #varReference
           | MINUS expression  #minusExpression
           | INTLIT # intLiteral
           | DECLIT # decimalLiteral ;
