# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false negative). Explain why you would not solve this issue.

## Answer

I used the quickstart java rulest on Apach Commons Lang :
```bash=
./run.sh pmd -d ../../commons-lang -R rulesets/java/quickstart.xml
```


- True positive result :
```
../../commons-lang/src/main/java/org/apache/commons/lang3/math/Fraction.java:267:	OneDeclarationPerLine:	Use one line for each declaration, it enhances code readability.
```
Indeed, the following line :
```java=
double delta1, delta2 = Double.MAX_VALUE;
```
could be clearer if wrote like this : 
```java=
double delta1 = Double.MAX_VALUE;
double delta2 = Double.MAX_VALUE;
```
especially considering that it was done for the other declarations just above :
```java=
int numer0 = 0; // the pre-previous
int denom0 = 1; // the pre-previous
int numer1 = 1; // the previous
int denom1 = 0; // the previous
```


- False negative result :
```
../../commons-lang/src/test/java/org/apache/commons/lang3/util/FluentBitSetTest.java:1441:	EmptyCatchBlock:	Avoid empty catch blocks
```
This happens in the function *test_setII()* wich verify, in this exemple, that a function rightfully throw an IndexOutOfBoundsException :
```java=
try {
    bs.set(-1, 3);
    fail("Test1: Attempt to flip with  negative index failed to generate exception");
} catch (final IndexOutOfBoundsException e) {
    // Correct behavior
}
```
Doing nothing with the catch here is perfectly right (as indicated by the comment), it is just a way to continue the test when the behavior is correct and the stop indicating an Error would happen in the try.