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
variable           : VAR ID ASSIGN expression;
introduction       : INTRODUCTION CURLY_LEFT (statement | unnamedEvent)* choicesBlock? CURLY_RIGHT;
location           : LOCATION ID CURLY_LEFT (statement | unnamedEvent)* choicesBlock? CURLY_RIGHT;
namedEvent         : EVENT ID CURLY_LEFT statement* choicesBlock? CURLY_RIGHT;

// Second-level constructs
unnamedEvent      : STORY? EVENT CURLY_LEFT conditionsBlock? statement* choicesBlock? CURLY_RIGHT;

statement         : print | assignment | triggerEvent | branch | jumpLocation |
                    finishEvent | endStory | untriggerEvent;

branch            : BRANCH CURLY_LEFT conditionsBlock statement* choicesBlock? CURLY_RIGHT;
conditionsBlock   : CONDITIONS CURLY_LEFT expression* CURLY_RIGHT;
choicesBlock      : CHOICES CURLY_LEFT choice* CURLY_RIGHT;
choice            : STRING (statementBlock | statement);
statementBlock    : CURLY_LEFT statement* choicesBlock? CURLY_RIGHT;

// Atomic statements
jumpLocation      : GOTO ID;
triggerEvent      : TRIGGER ID;
finishEvent       : FINISH_EVENT;
endStory          : END_STORY;
print             : STRING (CONTINUE_SIGN | REPLACE_SIGN)?;
untriggerEvent    : UNTRIGGER;
assignment        : ID ASSIGN expression;

// Value expressions
expression        : boolExpression | intExpression | otherExpression;

intExpression     : intExpressionU | intExpressionB;
intExpressionU    : INT | ((PLUS | MINUS) (intExpression | implicitTypedExpr)) | (PAREN_LEFT intExpression PAREN_RIGHT);
intExpressionB    : ((intExpressionU | implicitTypedExpr) (PLUS | MINUS | MULT | DIV | MOD) (intExpressionU | implicitTypedExpr));

boolExpression    : boolExpressionU | boolExpressionB;
boolExpressionU   : BOOL | implicitTypedExpr | (NOT boolExpression);

boolExpressionB   : boolAggregetion | intComparison | otherComparison ;
boolAggregetion   : boolExpressionU OR boolExpressionU;
intComparison     : intExpression (LT | LE | EQ | GE | GT | NE) (intExpression | implicitTypedExpr) |
                    implicitTypedExpr (LT | LE | EQ | GE | GT | NE) intExpression;
otherComparison   : otherExpression (EQ | NE) otherExpression;

otherExpression   : otherExpressionU;
otherExpressionU  : STRING | implicitTypedExpr | INTRODUCTION | HERE;

implicitTypedExpr : ID;

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
EQ                : '==';
NE                : '!=';
NOT               : '!';
PLUS              : '+';
MINUS             : '-';
MULT              : '*';
DIV               : '/';
MOD               : '%';
OR                : '|';
ASSIGN            : '=';