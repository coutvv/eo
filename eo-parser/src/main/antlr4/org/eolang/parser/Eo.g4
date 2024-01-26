grammar Eo;

tokens { TAB, UNTAB }

// Entry point
program
    : license? metas? objects EOF
    ;

// Double EOL
eop : EOL EOL
    ;

// Licence
license
    : (COMMENT EOL)* COMMENT eop
    ;

// Metas
metas
    : (META EOL)* META eop
    ;

// Objects
// Ends on the next line
objects
    : (commented EOL?)* commented
    ;

// Object with optional comment
// Ends on the next line
commented
    : (COMMENT EOL)* object
    ;

// Object
// Ends on the next line
object
    : master
    | slave
    ;

// Objects that may be used inside abstract object
// Ends on the next line
slave
    : application
    | methodNamed EOL
    | justNamed EOL
    ;

// Indeprendent objects that may have slaves (except atom)
// Ends on the next line
master
    : atom EOL
    | formation
    | hanonym oname EOL
    ;

// Just an object reference without name
just: beginner
    | finisherCopied
    | versioned
    ;

// Just object reference with optional name
justNamed
    : just oname?
    ;

// Atom - abstract object with mandatory name and type
// Comment can be placed before atom
// Can't contain inner objects
atom: ahead suffix type
    ;

// Formation - abstract object with mandatory name
// Comment can be placed before atom
// Can contain inner objects
// Ends on the next line
formation
    : ahead oname (inners | EOL)
    ;

// Inner objects inside abstraction
// Every inner object must be indented
// Ends on the next line
// No empty lines before "slave"
// May be one empty line before "master"
inners
    : EOL TAB object (slave | EOL? master)* UNTAB
    ;

// Attributes of an abstract object, atom or horizontal anonym object
attributes
    : LSQ (attribute (SPACE attribute)*)? RSQ
    ;

// Attribute
attribute
    : NAME
    ;

// Type of atom
type: SPACE SLASH (NAME | QUESTION)
    ;

// Application
// - horizontal
// - vertical
// Ends on the next line
application
    : happlicationExtended oname? EOL
    | vapplication
    ;

// Horizontal application
// The whole application is written in one line
// The head does not contain elements in vertical notation
// The division of elements into regular and extended ones is
// due to the presence of horizontal anonymous objects where inner objects
// must be horizontal only
happlication
    : happlicationHead happlicationTail
    | happlicationReversed
    ;

// Extended horizontal application
// The head can contain elements in horizontal or vertical notations
happlicationExtended
    : happlicationHeadExtended happlicationTail
    | happlicationReversed
    ;

// Reversed horizontal application
happlicationReversed
    : reversed happlicationTailReversed
    ;

// Head of horizontal application
// Does not contain elements in vertical notation
happlicationHead
    : hmethod
    | applicable
    ;

// Extended head of horizontal application
// Can contain elements in vertical notation
happlicationHeadExtended
    : vmethod
    | hmethodExtended
    | applicable
    ;

// Simple statements that can be used as head of application
applicable
    : STAR
    | (NAME | AT) COPY?
    ;

// Horizontal application tail
happlicationTail
    : (SPACE happlicationArg as)+
    | (SPACE happlicationArg)+
    ;

happlicationTailReversed
    : SPACE happlicationTailReversedFirst happlicationTail?
    ;

// The rule is separated because we should enter to the last object
// here, but don't do it on happlicationTail rule
happlicationTailReversedFirst
    : happlicationArg
    ;

// Argument of horizontal application
// Does not contain elements in vertical notation
happlicationArg
    : beginner
    | finisherCopied
    | hmethod
    | scope
    ;

// Vertical application
// Ends on the next line
vapplication
    : vapplicationHeadNamed vapplicationArgs
    | reversed oname? vapplicationArgsReversed
    ;

// Vertical application head
vapplicationHead
    : applicable
    | hmethodOptional
    | vmethodOptional
    | versioned
    ;

// Vertical application head with optional name
vapplicationHeadNamed
    : vapplicationHead oname?
    ;

// Vertical application head with binding
vapplicationHeadAs
    : vapplicationHead as
    ;

// Vertical application arguments
// Ends on the next line
vapplicationArgs
    : EOL TAB vapplicationArg UNTAB
    ;

// Arguments for reversed vertical application
vapplicationArgsReversed
    : EOL TAB vapplicationArgUnbound vapplicationArg? UNTAB
    ;

// Arguments of vertical application
// Must either all bound or all unbound
// Ends on the next line
vapplicationArg
    : vapplicationArgBound+
    | vapplicationArgUnbound+
    ;

// Vertical application arguments with bindings
vapplicationArgBound
    : vapplicationArgBoundCurrent EOL
    | vapplicationArgBoundNext
    ;

// Vertical application arguments with bindings
// Ends on the current line
vapplicationArgBoundCurrent
    : vapplicationArgHapplicationBound // horizontal application
    | vapplicationArgHanonymBound // horizontal anonym object
    | (just | method) as oname? // just an object reference | method
    ;

// Vertical application arguments with bindings
// Ends on the next line
vapplicationArgBoundNext
    : vapplicationArgVanonymBound // vertical anonym object
    | vapplicationHeadAs oname? vapplicationArg // vertical application
    | reversed as oname? vapplicationArgsReversed // reversed vertical application
    ;

// Vertical application arguments without bindings
// Ends on the next line
vapplicationArgUnbound
    : vapplicationArgUnboundCurrent EOL
    | vapplicationArgUnboundNext
    ;

// Vertical application arguments without bindings
// Ends on the current line
vapplicationArgUnboundCurrent
    : vapplicationArgHapplicationUnbound // horizontal application
    | vapplicationArgHanonymUnbound // horizontal anonym object
    | justNamed // just an object reference
    | methodNamed // method
    ;

// Vertical application arguments without bindings
// Ends on the next line
vapplicationArgUnboundNext
    : vapplicationArgVanonymUnbound // vertical anonym object
    | vapplicationHeadNamed vapplicationArgs // vertical application
    | reversed oname? vapplicationArgsReversed // reversed verical application
    ;

// Horizontal application as argument of vertical application
vapplicationArgHapplicationBound
    : LB happlicationExtended RB as oname?
    ;

vapplicationArgHapplicationUnbound
    : happlicationExtended oname?
    ;

// Vertical anonym object as argument of vertical application
vapplicationArgVanonymUnbound
    : attributes vanonymTail
    ;

// Bound vertical anonym abstract object as argument of vertical application argument
// Ends on the next line
vapplicationArgVanonymBound
    : attributes as vanonymTail
    ;

// Horizontal anonym abstract object as argument of vertical application
vapplicationArgHanonymBound
    : LB hanonym RB as oname?
    ;

vapplicationArgHanonymUnbound
    : hanonym oname?
    ;

// Horizontal anonym object
hanonym
    : attributes hanonymInner+
    ;

// Inner object of horizontal anonym object
// Does not contan elements in vertical notation
hanonymInner
    : SPACE LB (hmethod | hmethodVersioned | happlication | hanonym | just) oname RB
    ;

// Abstract objects <- arguments of vertical anonym object <- argument of vertical application
// Ends on the next line
formatees
    : EOL
      TAB
      (innerformatee | slave)
      (slave | EOL? innerformatee)*
      UNTAB
    ;

// Inner abstract object of formatees
// Ends on the enxt line
innerformatee
    : ahead vanonymTail
    ;

// Optional comment + attributes
ahead
    : (COMMENT EOL)* attributes
    ;

// Tail of vertical anonym objects
// Ends on the next line
vanonymTail
    : oname? (formatees | EOL)
    ;

// Method
method
    : hmethodOptional
    | vmethodOptional
    ;

// Method with optional name
methodNamed
    : method oname?
    ;

// Horizontal method
// The whole method is written in one line
// The head does not contain elements in vertical notation
hmethod
    : hmethodHead methodTail+
    ;

// Optional horizontal method
hmethodOptional
    : hmethodExtended
    | hmethodExtendedVersioned
    ;

// Extended horizontal method
// The head can contain elements in vertical notation
hmethodExtended
    : hmethodHeadExtended methodTail+
    ;

// Versioned horizontal method
// The whole method is written in one line
// The head does not contain elements in vertical notation
// The division of elements into regular and versioned ones is due to
// the presence of horizontal application where head or agruments can't
// contain version
hmethodVersioned
    : hmethodHead methodTail* methodTailVersioned
    ;

// Versioned extended horizontal method
// The head can contain elements in vertical notation
hmethodExtendedVersioned
    : hmethodHeadExtended methodTail* methodTailVersioned
    ;

// Head of horizontal method
hmethodHead
    : beginner
    | finisherCopied
    | scope
    ;

// Extended head of horizontal method
hmethodHeadExtended
    : beginner
    | finisherCopied
    | scope
    ;

// Vertical method
vmethod
    : vmethodHead vmethodTail
    ;

// Vertical method with version
vmethodVersioned
    : vmethodHead vmethodTailVersioned
    ;

// Optional vertical method
vmethodOptional
    : vmethod
    | vmethodVersioned
    ;

// Head of vertical method
// The simple variation of this block leads to left recursion error
// So in order to avoid it this block was described in more detail
// Head of vertical method can be:
// 1. vertical method
// 2. horizontal method
// 3. vertical application
// 4. horizontal application. The same logic as with a vertical application
// 5. just an object reference
vmethodHead
    : vmethodHead vmethodTailOptional vmethodHeadApplicationTail
    | vmethodHeadHmethodExtended
    | vmethodHeadVapplication
    | vmethodHeadHapplication
    | justNamed
    ;

vmethodTailOptional
    : vmethodTail
    | vmethodTailVersioned
    ;

vmethodHeadApplicationTail
    : oname? vapplicationArgs?
    | happlicationTail oname?
    ;

vmethodHeadHmethodExtended
    : hmethodOptional oname?
    ;

vmethodHeadVapplication
    : (applicable | hmethodOptional | versioned) oname? vapplicationArgs
    | reversed oname? vapplicationArgsReversed
    ;

vmethodHeadHapplication
    : (applicable | hmethodExtended) happlicationTail oname?
    | happlicationReversed oname?
    ;

// Tail of vertical method
vmethodTail
    : EOL methodTail
    ;

// Versioned tail of vertical method
vmethodTailVersioned
    : EOL methodTailVersioned
    ;

// Tail of method
methodTail
    : DOT finisherCopied
    ;

// Versioned tail of method
methodTailVersioned
    : DOT NAME version?
    ;

// Can be at the beginning of the statement
// Can't be after DOT
beginner
    : STAR
    | ROOT
    | HOME
    | XI
    | data
    ;

// Can start or finish the statement
finisher
    : NAME
    | AT
    | RHO
    | SIGMA
    | VERTEX
    ;

// Finisher with optional COPY
finisherCopied
    : finisher COPY?
    ;

// Name with optional version
versioned
    : NAME version?
    ;

// Reversed notation
// Only finisher can be used in reversed notation
reversed
    : finisher DOT
    ;

// Object name
oname
    : suffix CONST?
    ;

// Suffix
suffix
    : SPACE ARROW SPACE (AT | NAME)
    ;

// Simple scope
// Does not contain elements in vertical notation
// Is used in happlicationArg, hmethodHead
scope
    : LB (happlication | hanonym) RB
    ;

// Version
version
    : BAR VER
    ;

// Binding
as  : COLON (NAME | RHO | INT)
    ;

// Data
data: BYTES
    | BOOL
    | TEXT
    | STRING
    | INT
    | FLOAT
    | HEX
    ;

COMMENT
    : HASH
    | (HASH ~[\r\n]* ~[\r\n\t ])
    ;
META: PLUS NAME (SPACE ~[\r\n]+)?
    ;

ROOT: 'Q'
    ;
HOME: 'QQ'
    ;
STAR: '*'
    ;
CONST
    : '!'
    ;
SLASH
    : '/'
    ;
COLON
    : ':'
    ;
COPY: '\''
    ;
ARROW
    : '>'
    ;
VERTEX
    : '<'
    ;
SIGMA
    : '&'
    ;
XI  : '$'
    ;
PLUS: '+'
    ;
MINUS
    : '-'
    ;
QUESTION
    : '?'
    ;
SPACE
    : ' '
    ;
DOT : '.'
    ;
LSQ : '['
    ;
RSQ : ']'
    ;
LB  : '('
    ;
RB  : ')'
    ;
AT  : '@'
    ;
RHO : '^'
    ;
HASH: '#'
    ;
BAR : '|'
    ;

fragment INDENT
    : SPACE SPACE
    ;

fragment LINEBREAK
    : '\n'
    | '\r\n'
    ;

EOL : LINEBREAK INDENT*
    ;

fragment BYTE
    : [0-9A-F][0-9A-F]
    ;

fragment EMPTY_BYTES
    : MINUS MINUS
    ;
fragment LINE_BYTES
    : BYTE (MINUS BYTE)+
    ;

BYTES
    : EMPTY_BYTES
    | BYTE MINUS
    | LINE_BYTES (MINUS EOL LINE_BYTES)*
    ;

BOOL: 'TRUE'
    | 'FALSE'
    ;

fragment ESCAPE_SEQUENCE
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ BYTE BYTE
    ;

STRING
    : '"' (~["\\\r\n] | ESCAPE_SEQUENCE)* '"'
    ;

fragment ZERO
    : '0'
    ;

INT : (PLUS | MINUS)? (ZERO | ZERO?[1-9][0-9]*)
    ;

fragment EXPONENT
    : ('e'|'E') (PLUS | MINUS)? ('0'..'9')+
    ;

FLOAT
    :
    (PLUS | MINUS)? [0-9]+ DOT [0-9]+ EXPONENT?
    ;

HEX : '0x' [0-9a-fA-F]+
    ;

NAME: [a-z] ~[ \r\n\t,.|':;!?\][}{)(]*
    ;

VER : [0-9]+ DOT [0-9]+ DOT [0-9]+
    ;

fragment TEXT_MARK
    : '"""'
    ;

TEXT: TEXT_MARK ('\n' | '\r\n') (~[\\] | ESCAPE_SEQUENCE)*? TEXT_MARK
    ;
