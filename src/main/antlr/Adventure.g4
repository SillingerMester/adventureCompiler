grammar Adventure;

@header {
    //package adventure; // Replace with your package name
}

@members {
    // Import any necessary classes or packages here
}

// Root of syntax tree
adventure : (variable | introduction | location | named_event)*;

// Top-level contructs
variable           : VAR ID EQ expression;
introduction       : INTRODUCTION CURLY_LEFT statement* CURLY_RIGHT;
location           : LOCATION ID CURLY_LEFT (statement | unnamed_event)* CURLY_RIGHT;
named_event       : EVENT ID CURLY_LEFT statement* CURLY_RIGHT;

// Second-level constructs
unnamed_event     : EVENT CURLY_LEFT conditions_block? statement* choices_block? CURLY_RIGHT;

statement         : print | assignment | trigger_event | branch |
                    jump_location | choices_block | finish_event | end_story;

assignment        : ID EQ expression;

branch            : BRANCH CURLY_LEFT conditions_block statement* CURLY_RIGHT;
conditions_block  : CONDITIONS CURLY_LEFT expression* CURLY_RIGHT;
choices_block     : CHOICES CURLY_LEFT choice* CURLY_RIGHT;
choice            : STRING (statement_block | statement);
statement_block   : CURLY_LEFT statement* CURLY_RIGHT;

// Atomic statements
jump_location     : GOTO ID;
trigger_event     : TRIGGER ID;
finish_event      : FINISH_EVENT;
end_story         : END_STORY;
print             : STRING (CONTINUE_SIGN | REPLACE_SIGN)?;

// Value expressions
expression        : unary_expression | binary_expression;
unary_expression  : literal | (unary_operator expression) | (PAREN_LEFT expression PAREN_RIGHT);
binary_expression : unary_expression binary_operator unary_expression;
literal           : STRING | INT | BOOL | ID | INTRODUCTION | HERE;

// Lexeme collections
unary_operator    : PLUS | MINUS | NOT;
binary_operator   : LT | LE | GT | GE | EQ | NE | PLUS | MINUS | DIV | MOD;

// Keywords
VAR               : 'var';
INTRODUCTION      : 'introduction';
LOCATION          : 'location';
EVENT             : 'event';
END_STORY         : 'end';
FINISH_EVENT      : 'finish';
GOTO              : 'goto';
TRIGGER           : 'trigger';
CHOICES           : 'choices';
BRANCH            : 'branch';
CONDITIONS        : 'conditions';
HERE              : 'here';

// Whitespace
NEWLINE           : ('\r\n' | '\r' | '\n') -> skip;
WHITESPACE        : (' ' | '\t')(' ' | '\t')* -> skip;
COMMENT           : ('//' .*? ('\r\n' | '\r' | '\n')) -> skip;

// Identifiers
ID                : [_]*[a-z][A-Za-z0-9_]* ;

// Literals
STRING            : '"' .*? '"';
BOOL              : 'true' | 'false';
INT               : [0-9][0-9]*;

// Operators
CONTINUE_SIGN     : '->';
REPLACE_SIGN      : '<-';
PAREN_LEFT        : '(';
PAREN_RIGHT       : ')';
CURLY_LEFT        : '{';
CURLY_RIGHT       : '}';
LT                : '<';
LE                : '<=';
GT                : '>';
GE                : '>=';
EQ                : '=';
NE                : '!=';
NOT               : '!';
PLUS              : '+';
MINUS             : '-';
MULT              : '*';
DIV               : '/';
MOD               : '%';
