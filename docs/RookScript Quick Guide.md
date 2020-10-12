# Rookscript Language Quick Guide


### Comments

```
// This is a single-line comment.
/*
    This is a multi
    line comment.
*/
/* You can even write them like this. */
```

All comments are ignored, and are not preserved after compilation.


### Value Types

Null

```
null
```

Booleans

```
true
false
```

Integers (64-bit)

```
0
5
34
0xff        // hexadecimal notation
0X0ff       // hexadecimal notation
0x6767      // hexadecimal notation
0x00fd34dd  // hexadecimal notation
```

Floating-point (64-bit, must start with a number)

```
0.0
0.456
1.467
1e2         // exponent notation
0.5e6       // exponent notation
1.545E7     // exponent notation
4.605e-7    // exponent notation
0.44444e+3  // exponent notation
infinity    // floating-point, IEEE positive infinity
NaN         // floating-point, IEEE not-a-number ("nan" also works - it is case-insensitive)
```

Strings (wrapped in double quotes)

```
""
"apple"
"104"
"Matt Tropiano"
"abracadabra"
```

Lists

```
[]            // empty list
[5]           // one element
[1, 2, 3]
["apple", "orange", "pear", "kumquat"]
[1, 1.0, "apple", false, null]            // can hold multiple types
[[1,2,3],[4,5,6]]                        // can hold nested lists
```

Maps

```
{}              // empty map
{x: 5, y: 6}    // map with two fields
{               // map with several fields and types (values can be any type)
    name: "Bob", 
    age: 23,
    gender: "Male",
    likes: ["Golf", "Baseball"],
    dislikes: ["Writing documentation", "Cold weather", "mean people"],
    rating: 9.5
}
```

Map keys do not need to be identifier names - they can also be numbers or string literals. 

```
{
    "first name": "Bob", 
    "last name": "Smith",
    0: "Zero", 
    1: "One", 
    2: "Two"
}
```

**Buffers**, **Errors**, **Objects**, three additional types, have no literal representation, but are storable as variables.


### Logical Equivalence

Some parts of Rookscript evaluate values logically. For those cases, the following values are considered `false`:

```
null
false
0
0.0
NaN
""      // empty string
```

Everything else is considered `true`.


### Variables

All variables/identifiers must not start with a number (since numbers do), and must be comprised
of alphanumeric characters plus the underscore `_`.

```
x
arg0
VARNAME
this_is_a_variable_too
```

List contents are read via 0-based index:

```
x = [4,5,6,7]
x[0]            //  4
x[1]            //  5
x[2]            //  6
x[3]            //  7
x[3.5]          //  7 (3.5 is chopped to 3)
x[-1]           //  null (values outside of a list are null)
x[4]            //  null (values outside of a list are null)
```

Map contents are read via the `.` operator:

```
m = {x: 5, y: -3}
m.x     // 5
m.y     // -3
m.z     // null (invalid key)
```


### Statements

Each script is a series of **statements**. Statements are usually function calls or expressions that assign values to variables. All statements are terminated with a semicolon (`;`).

Some statements look like this:

```
y = 9;                      // assigns 9 to y
age = 24;                   // assigns 24 to age
x = 4.0 + 5.0;              // assigns 9.0 to x
b = double(4);              // calls the function "double" with 4 as its only parameter, and assigns the result to b.
tagPerson("Bob", "old");    // calls the function "tagPerson" with "Bob" and "old" as parameters.
doWork();                   // calls the function "doWork" with no parameters.
```

A *block* of statements, executed in the order written, are placed between *braces*:

```
{
    x = 24;         // assign 24 to x
    y = 9;          // assign 9 to y
    z = x + y;      // assign the result of x + y (33) to z
}
```


### Expressions

Expressions are infix-notated (like how humans write it) and are comprised of values and operators.

```
5 + 4
x - 3 / 6
9 + -4 - (x * 3.0)
!false
"apple" + "sauce"
```

There are **unary** operators:

```
!       // logical not     (!false !4.0)
~       // bitwise not     (~5)
-       // negation        (-3)
+       // absolute        (+(-16) +9.02)
```

and there are **binary** operators:

```
+       // addition
-       // subtraction
*       // multiplication
/       // division
%       // modulo division
&       // bitwise and
|       // bitwise or
^       // bitwise xor
==      // logical equals
===     // logical strict equals (type and value)
!=      // logical not equal
!==     // logical strict not equals (type and value)
<       // logical less than
>       // logical greater than
<=      // logical less than or equal
>=      // logical greater than or equal
<<      // left bit shift
>>      // right bit shift
>>>     // right bit shift (zero-padded)
```

All operators have a defined precedence:

```
4 + 5 * 3       // equals 19, not 27
```

In order of precedence (same row is same precedence):

```
Unary
    ! ~ - +        // logical not, bitwise not, negate, absolute

Binary
    * / %          // multiply, divide, modulo
    + -            // add, subtract
    >> >>> <<      // right shift, padded right shift, left shift
    > >= < <=      // greater, greater-equal, less, less-equal
    == === != !==  // equal, strict equal, not equal, strict not equal
    &              // bitwise and
    ^              // exclusive or
    |              // bitwise or
```

The following binary operators **short-circuit** their logic:

```
&&      // logical and - short circuit if left side is false
||      // logical or - short circuit if left side is true
?:      // false coalesce - short circuit if left side is not false, return first non-false-equivalent.
??      // null coalesce - short circuit if left side is not null, return first non-null.
```

Assignment operators:

```
=       // equals
+=      // plus equals
-=      // minus equals
*=      // multiply equals
/=      // divide equals
%=      // modulo equals
&=      // bitwise-and equals
|=      // bitwise-or equals
>>=     // right-shift equals
>>>=    // padded right-shift equals
<<=     // left-shift equals
```


The aforementioned short-circuiters have **lowest** precedence.

```
4 + 7 && 3 - 3 && 12.0     // returns false (11 && 0). 0 is false-equivalent. 12.0 is not even evaluated.
```

Precedence can be altered using parenthesis - these essentially create sub-expressions within expressions:

```
4 + 5 * 3       // equals 19, not 27
4 + (5 * 3)     // equals 19, not 27
(4 + 5) * 3     // equals 27, not 19
```

When using operators, some either result in implicit type promotion, or reduction. This is critical to think about for comparisions later!

```
4 + 5.0         // result is 9.0 - floating-point
!3              // result is false - boolean
```

There is also a **ternary** operator:

```
? :         // if before the '?' is true-equivalent, evaluate before the colon, else after.

x ? 5 : 7   // returns 5 if x is true-equivalent, else returns 7
```

### Control Statements

There are a few special types of statements that control execution.


#### The "If" Statement

"If" statements represent a branch in logic - if the expression in the parenthesis evaluates to `true`, the subsequent block is executed.

```
if (x == 4)
{
    println("x is 4!");
}
```

If an "if" statement is followed up by an "else" block, that block will be executed instead.

```
if (x == 4)
{
    println("x is 4!");
}
else
{
    println("x is not 4!");
}
```

A block of statements can have the braces omitted if it is just one statement (this is also true for all control statements expecting a block of statements).

```
if (x == 4)
    println("x is 4!");
else
    println("x is not 4!");
```

For this reason, you can chain together if-elses...

```
if (x == 4)
{
    println("x is 4!");
}
else if (x == 5)
{
    println("x is 5!");
}
else
{
    println("x is neither 4 nor 5!");
}
```

...since this is equivalent:

```
if (x == 4)
{
    println("x is 4!");
}
else 
{
    if (x == 5)
    {
        println("x is 5!");
    }
    else
    {
        println("x is neither 4 nor 5!");
    }
}
```

#### The "While" Loop

"While" loops execute the following block of statements as long as the conditional expression in the parenthesis still evaluates `true` after the block completes.

```
x = 5;
// This code will loop 5 times.
while (x > 0)
{
    println("x is " + x);
    x = x - 1;
}
```

#### The "For" Loop

"For" loops execute the following block of statements as long as the conditional expression is still evaluates `true` after the block completes.

*For* loops have an *initializer*, *conditional*, and *step* part enclosed in parenthesis that drives the loop. Each of these parts are separated by a semicolon (`;`), except for the last part.

```
// This code will loop 10 times.
for (x = 0; x < 10; x += 1)
{
    println("x is " + x);
}
```

Each part can be left out of a *for* loop, except for the conditional. The previous loop can be rewritten as:

```
x = 10;
for (; x < 10;)
{
    println("x is " + x);
    x += 1;
}
```

#### The "Each" Loop

"Each" loops iterate linearly through values that are collections of values. All value types can be iterated through (with varying usefulness).

```
numbers = [1, 2, 3, 4, 5];
// will print each number in "numbers" on its own line.
each (x : numbers)
{
    println(x);
}
```

Maps and other map-like objects can have their entries iterated through, as well.

```
triple = { x: 3, y: 4, z: -2 };
// will print each key and value in "triple" on its own line.
each (k, v : triple)
{
    println(k + " is " + v);
}
```

Performing a key-value iteration on lists will fill the key with the index:

```
numbers = [1, 2, 3, 4, 5];
// will print each index and value in "numbers" on its own line.
each (k, v : numbers)
{
    println("numbers[" + k + "] is " + v);
}
```

Attempting to iterate on a single value will only run the loop once, though:

```
a = 3;
// will just print "3"
each (x : a)
{
    println(x);
}
```

A key-value iteration on a single value will return `null` as the key, and the value as the value.

Iterators are also smart enough to iterate through Java-native collections that are not RookScript values - any OBJECTREF type that implements Map or Iterable will work (which includes Collections)!


### Entry Points

All starting points in a script's execution are called *entry points* and are declared as such:

```
entry start()
{
    // ... statements go here
}
```

...this declares an entry point called `start` that has no parameters.

Entry points can declare any number of parameters, declared as identifiers that hold the values that are passed in when they are called from the host. The `return` keyword returns execution to the host plus a value result, if any.


### Functions

#### Local Functions

*Local Functions* are simple subroutines. They are declared in the script like so:

```
function name(parameter0, parameter1)
{
    // ... code
}
```

Like *entry points*, they can declare any number of parameters, declared as identifiers that hold the values that are passed in when they are called. The `return` keyword returns execution to the caller plus a value result. *All local functions return a value.* If it never hits a `return` statement, they return `null`. **It is good practice to ensure that a function hits a `return` at every branch in execution if it is supposed to return a value!**

Functions, however, **cannot** be the start of a script's execution, like *entry points*.

This is a function that doubles a value and returns it:

```
function double(x)
{
    return x * 2;
}
```

This is a function that calls other functions (and returns `null`):

```
// println is a function that prints a newline-terminated message to STDOUT.

function printUppercase(message)
{
    u = strupper(message);
    println(u);
}
```

Functions in Rookscript support *recursion*, or functions that call themselves.

```
function factorial(x)
{
    if (x <= 0)
        return 1;
    else
        return x * factorial(x - 1);
}
```

If a function is called with less parameters than required, the remaining parameters are filled with `null`.

```
// println is a function that prints a newline-terminated message to STDOUT.

function printStuff(p1, p2, p3)
{
    println("p1 is " + p1);
    println("p2 is " + p2);
    println("p3 is " + p3);
}

entry main()
{
    printStuff(1, 2);
    
    /*
        Will print:
        p1 is 1
        p2 is 2
        p3 is null
    */
}
```

#### Host Functions

*Host Functions* are functions that are provided by the host implementation. They are not declared in the script. They are called like local functions, and are null-filled like local functions. Also, like local functions, *all host functions return a value.* 

```
// println is a function that prints a newline-terminated message to STDOUT.

entry main()
{
    println("Hello, world!");
}
```

Sometimes, a host function is available within a *namespace*. These functions need to have a prefix before them:

```
// println is a function that prints a newline-terminated message to STDOUT, in the std namespace:

entry main()
{
    std::println("Hello, world!");    // works.
    println("Hello, world!");         // does not compile - not found!
}
```

How these functions are set up is up to the hosting program that provides them. Host functions (and namespaces) are resolved at compile-time, and must be provided to the script compiler via function resolvers before script compile.

#### Function Chains

*Function Chains* are function calls that use the return value of one function to be used as the *first parameter* of the next function called. The return of that function can also be used as the first parameter of the next function, etc.

This makes functions that return the object that it operates on more useful, as well as functions that use that object as the first parameter.

The function chain operator (a.k.a. the "partial application" operator) is `->`. You must also omit the first parameter of the function being called.

```
function double(i)
{
    return 2 * i;
}

// println is a function that prints a newline-terminated message to STDOUT.

entry main()
{
    x = 2;
    println(double(x));               // prints "4"
    println(x->double());             // also prints "4"
    println(x->double()->double());     // prints "8"
}
```

You may use literals as the start of the chain, provided that they are enclosed in parenthesis:

```
function double(i)
{
    return 2 * i;
}

// println is a function that prints a newline-terminated message to STDOUT.

entry main()
{
    println((double(2));              // prints "4"
    println((2)->double());           // also prints "4"
    println((2)->double()->double()); // prints "8"
    ("Math is fun!")->println();      // prints "Math is fun!"
}
```

It can be really useful!

```
function vec2(x, y)
{
    return {x: x, y: y};
}

// Adds components to a vector.
function vec2Add(v2, x, y)
{
    v2.x += x;
    v2.y += y;
    return v2;
}

// Negates a vector.
function vec2Negate(v2)
{
    v2.x = -v2.x;
    v2.y = -v2.y;
    return v2;
}

// println is a function that prints a newline-terminated message to STDOUT.

entry main()
{
    v = vec2(0, 0)->vec2Add(3, 4)->vec2Negate();
    println(v);  // prints "{x: -3, y: -4}"
}
```

### Scopes

The host implementation may provide one or more special namespaces that contain variables that scripts can manipulate called *Scopes*. Scopes are useful when you want a script to manipulate a state, send several pieces of data to the host implementation, or make a lot of values available to the calling script. Some variables in a scope can be read-only, and will not be changed if a script attempts to set or redefine them. Some scopes allow scripts to add variables to them.

```
// "data" is the name of a scope provided to the script.

entry setData()
{
    data::x = 4;
    data::y = 8 + data::x;
}
```

What is available and what is changeable is entirely up to the host. Scopes are resolved at compile-time, and must be provided to the script compiler before script compile.

**Note** that the same operator `::` is used for resolving scope variables and host function namespaces. The compiler differentates this by checking whether a function call is being made or not:

```
io::file    // "io" is a scope
io::file()  // "io" is a namespace for a function, "file"
```

### Error Handling

Errors happen. Sometimes they happen when you don't expect them to. But you can `check` for them:

```
entry main()
{
    check (err)
    {
        fis = fisopen("stuff.txt");
        fos = fosopen("stuff2.txt");
        relay(fis, fos);
    }
    close(fis);
    close(fos);
    if (err != null)
        println("ERROR: " + err);
    else
        println("Copied file successfully.");
}
```

If an `Error` type is returned for any of the function calls in the `check` block, the block is broken out of, and the provided variable, `err`, is set to the error returned. Otherwise, if no error occurs, `err` will be set to null once execution leaves the block.

Since `check` can work on a single line/statement block, checking one line looks like:

```
check (err) relay(fis, fos);
```

Same effects apply.

You can mark a whole function/entry point as checked - it will return the error if one occurs:

```
check function fileContents(fis, len)
{
    out = bufnew(len);
    bos = bosopen(out);
    relay(fis, bos, 8192, len);
    return out;
}

entry main()
{
    filename = "stuff.txt";
    check (err) 
    { 
        fis = fisopen(filename);
        databuf = fileContents(fis, filelen(filename));
    }
    close(fis);

    if (err != null)
        println("ERROR: " + data);
    else
        println("Read " + length(databuf) + " bytes");
}
```

In this situation, either the `fileContents` function call will return a buffer on success, or an `Error` type on error.

#### Some Technical Caveats

The `check` functionality does not have a way to automatically free resources (like how some languages have `finally`) - you will have to ensure that happens given your program's flow.

Checking for errors also adds a little overhead to a script (roughly +7% throughput). A check is made each function call and entering/leaving a `check` block pushes/pops some more objects in/out of the stack. Use discretion with your automatic error detection, especially in real-time applications that call a script constantly!


### The Preprocessor

RookScript has a C-like preprocessor for affecting the token parser stream. All of the directives are below:

Preprocessor directives must occupy a line - the directive plus parameters are terminated by a newline.


#### #include

The #include directive includes the contents of the specified file. The filename is provided as a string parameter, and can either be a relative file path from the file that contains the directive or an absolute path. Files can be included more than once - use this with caution!

NOTE: The Java implementation of the compiler can also resolve resources off of the classpath by prefixing the path string with `classpath:`. The include behavior can also be replaced entirely in the Java implementation.


	#include "util.rscript"
	#include "stuff/junk.rscript"


#### #define

The #define directive defines a single-token macro that expands to a series of other tokens. This is also useful for creating defines and testing if they were defined later. They may also expand to zero tokens. All macro tokens are CASE SENSITIVE!

NOTE: Unlike the C-language preprocessor, this does not create macro functions.


	#define GREETING_TEXT "Hello."
	#define TWO_PI ( PI() * 2 )
	#define FILE_WAS_INCLUDED


#### #undefine

The #undefine directive removes a previously defined macro. All subsequent uses of that macro are treated as though they were never defined.


	#define GREETING_TEXT "Hello."
	#undefine GREETING_TEXT
	println(GREETING_TEXT);   // compiler thinks GREETING_TEXT is a variable


#### #ifdef

The #ifdef directive includes the next series of lines if the following macro was defined, until it reaches an #endif directive.


	#define LINES
	#ifdef LINES
	println("LINES was defined.");
	#endif


#### #ifndef

The #ifdef directive includes the next series of lines if the following macro was NOT defined, until it reaches an #endif directive.


	#define STUFF
	#ifndef LINES
	println("LINES was not defined.");
	#endif
	

#### #else

The #else directive ends the most recently started "if" directive block and provides an alternate section if the first "if" is not processed.


	#define LINES
	#ifdef LINES
	println("LINES was defined.");
	#else
	println("LINES was NOT defined.");
	#endif


#### #endif

The #endif directive ends the most recently started "if" or "else" directive block.


	#define LINES
	#ifdef LINES
	println("LINES was defined.");
	#endif

	#define LINES
	#ifdef LINES
	println("LINES was defined.");
	#else
	println("LINES was NOT defined.");
	#endif


