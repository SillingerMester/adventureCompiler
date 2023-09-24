grammar Adventure;

@header {
    //package adventure; // Replace with your package name
}

@members {
    // Import any necessary classes or packages here
}

// Root of syntax tree
adventure : (variable | introduction | location | namedEvent)*;

// Top-level contructs
variable           : VAR ID EQ expression;
introduction       : INTRODUCTION CURLY_LEFT statement* CURLY_RIGHT;
location           : LOCATION ID CURLY_LEFT (statement | unnamedEvent)* CURLY_RIGHT;
namedEvent         : EVENT ID CURLY_LEFT statement* CURLY_RIGHT;

// Second-level constructs
unnamedEvent      : STORY? EVENT CURLY_LEFT conditionsBlock? statement* choicesBlock? CURLY_RIGHT;

statement         : print | assignment | triggerEvent | branch | jumpLocation |
                    choicesBlock | finishEvent | endStory | untriggerEvent;

assignment        : ID EQ expression;

branch            : BRANCH CURLY_LEFT conditionsBlock statement* CURLY_RIGHT;
conditionsBlock   : CONDITIONS CURLY_LEFT expression* CURLY_RIGHT;
choicesBlock     : CHOICES CURLY_LEFT choice* CURLY_RIGHT;
choice            : STRING (statementBlock | statement);
statementBlock    : CURLY_LEFT statement* CURLY_RIGHT;

// Atomic statements
jumpLocation      : GOTO ID;
triggerEvent      : TRIGGER ID;
finishEvent       : FINISH_EVENT;
endStory          : END_STORY;
print             : STRING (CONTINUE_SIGN | REPLACE_SIGN)?;
untriggerEvent    : UNTRIGGER;

// Value expressions
expression        : binaryExpression | unaryExpression;
unaryExpression   : literal | (unaryOperator expression) | (PAREN_LEFT expression PAREN_RIGHT);
binaryExpression  : unaryExpression binaryOperator unaryExpression;
literal           : STRING | INT | BOOL | ID | INTRODUCTION | HERE;

// Lexeme collections
unaryOperator     : PLUS | MINUS | NOT;
binaryOperator    : LT | LE | GT | GE | EQ | NE | PLUS | MINUS | DIV | MOD;

// Whitespace
NEWLINE           : ('\r\n' | '\r' | '\n') -> skip;
WHITESPACE        : (' ' | '\t')(' ' | '\t')* -> skip;
COMMENT           : ('//' .*? ('\r\n' | '\r' | '\n')) -> skip;

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
STORY             : 'story';
UNTRIGGER         : 'untrigger';

// Literals
STRING            : '"' .*? '"';
BOOL              : 'true' | 'false';
INT               : [0-9][0-9]*;

// Identifiers
ID                : [_]*[a-z][A-Za-z0-9_]* ;

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
