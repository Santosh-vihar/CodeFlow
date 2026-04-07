JavaAir

A lightweight interpreted scripting language implemented in Java.
JavaAir reads and executes commands from a .air file, providing simple programming features such as variables, arithmetic, conditionals, loops, and objects.

The interpreter processes the file line by line and executes commands dynamically.

Features

JavaAir supports the following features:

1. Variables

You can create and update variables using the set command.

Example:

set x = 10
set name = "John"

Variables are stored internally in a map structure.

2. Printing Output

Use print to display values.

Example:

print "Hello World"
print x
print(x)

Outputs the resolved value of variables or expressions.

3. Arithmetic Expressions

JavaAir supports arithmetic expressions using:

+  addition
-  subtraction
*  multiplication
/  division
%  modulus

Example:

set x = int(5 + 3 * 2)
print x

Arithmetic expressions are parsed by an internal expression parser.

4. Adding to Variables

You can increase variable values using the add command.

Example:

set score = 10
add score 5
print score

Output:

15
5. Conditional Statements

JavaAir supports if statements with optional else.

Example:

if (x > 5) print "greater"

Or block syntax:

if (x > 5) {
    print "greater"
} else {
    print "smaller"
}

Supported comparison operators:

>
<
>=
<=
==
!=

Logical operators:

&&
||
and
or
6. Loops

JavaAir includes a simple repeat loop.

Example:

repeat 5 print "Hello"

Output:

Hello
Hello
Hello
Hello
Hello
7. Objects

You can define simple objects with properties.

Create object:

object player

Set properties:

set player.name = "Alex"
set player.score = 100

Access properties:

print player.name
print player.score
Example Program

Example .air script:

set x = 10
set y = 20

print "Sum:"
print int(x + y)

if (x < y) {
    print "x is smaller"
}

repeat 3 print "Looping"

object user
set user.name = "Alice"
set user.age = 25

print user.name
Project Structure
JavaAir.java
program.air
JavaAir.java

Main interpreter that:

Reads .air files
Parses commands
Executes instructions
program.air

The script file containing JavaAir commands.

How It Works
The interpreter loads program.air.
Each line is sanitized.
The command type is detected.
The corresponding handler executes the command.

Main execution flow:

main()
 └── read program.air
      └── execute(line)
           ├── handlePrint
           ├── handleSet
           ├── handleAdd
           ├── handleIf
           ├── handleRepeat
           └── handleObject
Expression Engine

JavaAir contains a custom integer expression parser that supports:

Operator precedence
Parentheses
Variable resolution
Unary operators

Example:

set result = int((5 + 3) * 2)
print result

Output:

16
Error Handling

JavaAir reports common errors such as:

❌ Invalid set syntax
❌ Invalid add syntax
❌ Missing closing } in if block
❌ Division by zero
❌ Unknown command
Running JavaAir
1. Compile
javac JavaAir.java
2. Run
java JavaAir

The interpreter will automatically execute:

program.air
Limitations

Current limitations include:

No functions
No file imports
No floating-point numbers
Single-line repeat loops
Limited object model
Future Improvements

Possible improvements:

Functions and procedures
Arrays and lists
User-defined classes
Multi-line loops
File imports
Error reporting with line numbers
String interpolation
Floating point arithmetic
License

This project is open-source and can be freely modified and extended.
