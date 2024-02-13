grammar Adventure;

@header {
    //package adventure; // Replace with your package name
}

@members {
    // Import any necessary classes or packages here
}

// Root of syntax tree
adventure : (variable | introduction | location | namedEvent | codeInjection | statsBlock | inventoryBlock | item)*;

// Top-level contructs
variable           : VAR ID ASSIGN expression;
introduction       : INTRODUCTION CURLY_LEFT (statement | unnamedEvent)* choicesBlock? CURLY_RIGHT;
location           : LOCATION ID CURLY_LEFT (statement | unnamedEvent)* choicesBlock? CURLY_RIGHT;
namedEvent         : STORY? EVENT ID CURLY_LEFT statement* choicesBlock? CURLY_RIGHT;
statsBlock         : STATS CURLY_LEFT ID* CURLY_RIGHT;
inventoryBlock     : INVENTORY CURLY_LEFT ID* CURLY_RIGHT;
item               : ITEM ID CURLY_LEFT DESCRIPTION STRING itemFunction* CURLY_RIGHT;

// Second-level constructs
unnamedEvent      : STORY? EVENT CURLY_LEFT conditionsBlock? statement* choicesBlock? CURLY_RIGHT;

statement         : print | assignment | triggerEvent | branch | jumpLocation | variable  | finishEvent |
                    endStory | untriggerEvent | codeInjection | loadGame | saveGame | consumeItem |
                    equipItem | unequipItem | getItem | replaceItem;

branch            : BRANCH CURLY_LEFT conditionsBlock statement* choicesBlock? CURLY_RIGHT;
conditionsBlock   : CONDITIONS CURLY_LEFT expression* CURLY_RIGHT;
choicesBlock      : CHOICES CURLY_LEFT choice* afterChoice? CURLY_RIGHT;
choice            : STRING (statementBlock | statement | itemsSubmenu);
statementBlock    : CURLY_LEFT statement* choicesBlock? CURLY_RIGHT;
itemFunction      : (USE | EQUIP | UNEQUIP | ID) (statementBlock | statement);
afterChoice       : AFTER_CHOICE CURLY_LEFT statement* CURLY_RIGHT;

// Atomic statements
jumpLocation      : GOTO ID;
triggerEvent      : TRIGGER ID;
finishEvent       : FINISH_EVENT;
endStory          : END_STORY;
print             : STRING (CONTINUE_SIGN | REPLACE_SIGN)?;
untriggerEvent    : UNTRIGGER;
assignment        : ID ASSIGN expression;
loadGame          : LOAD;
saveGame          : SAVE;
consumeItem       : CONSUME;
equipItem         : EQUIP;
unequipItem       : UNEQUIP;
getItem           : GET_ITEM ID;
replaceItem       : REPLACE_ITEM ID;
itemsSubmenu      : ITEMS_SUBMENU;

// Value expressions
expression        : implicitTypedExpr | boolExpression | intExpression | otherExpression | codeInjectionExpr;

intExpression     : intExpressionB | intExpressionU;
intExpressionU    : INT | ((PLUS | MINUS) (intExpression | implicitTypedExpr)) | (PAREN_LEFT intExpression PAREN_RIGHT) |
                    builtinMax;
intExpressionB    : ((intExpressionU | implicitTypedExpr) (PLUS | MINUS | MULT | DIV | MOD) (intExpressionU | implicitTypedExpr));

boolExpression    : boolExpressionU | boolExpressionB;
boolExpressionU   : BOOL | implicitTypedExpr | (NOT boolExpression) | afterEvent | hasItem;

boolExpressionB   : boolAggregetion | intComparison | otherComparison ;
boolAggregetion   : boolExpressionU OR boolExpressionU;
intComparison     : intExpression (LT | LE | EQ | GE | GT | NE) (intExpression | implicitTypedExpr) |
                    implicitTypedExpr (LT | LE | EQ | GE | GT | NE) intExpression;
otherComparison   : otherExpression (EQ | NE) otherExpression;

otherExpression   : otherExpressionU;
otherExpressionU  : STRING | inputText | implicitTypedExpr | INTRODUCTION | HERE;

implicitTypedExpr : ID;

//builtin functions
builtinMax        : MAX PAREN_LEFT expression COMMA expression PAREN_RIGHT;
afterEvent        : AFTER PAREN_LEFT ID PAREN_RIGHT;
hasItem           : HAS_ITEM PAREN_LEFT ID PAREN_RIGHT;
inputText         : INPUT_TEXT PAREN_LEFT STRING PAREN_RIGHT;

// Code injection
codeInjection     : CODE_INJECTION;
codeInjectionExpr : CODE_INJECTION;
CODE_INJECTION    : '@[' .*? ']@';


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
STATS             : 'stats';
LOAD              : 'load';
SAVE              : 'save';
INVENTORY         : 'inventory';
ITEM              : 'item';
DESCRIPTION       : 'description';
CONSUME           : 'consume';
EQUIP             : 'equip';
UNEQUIP           : 'unequip';
AFTER             : 'after';
HAS_ITEM          : 'has_item';
GET_ITEM          : 'get_item';
MAX               : 'max';
AFTER_CHOICE      : 'afterEach';
REPLACE_ITEM      : 'replace';
USE               : 'use';
INPUT_TEXT        : 'input_text';
ITEMS_SUBMENU     : 'items_submenu';

// Literals
STRING            : '"' .*? '"';
BOOL              : 'true' | 'false';
INT               : [0-9][0-9]*;

// Identifiers
ID                : [_]*[A-Za-z][A-Za-z0-9_]* ;

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
COMMA             : ',';