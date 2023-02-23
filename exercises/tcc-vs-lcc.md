# TCC _vs_ LCC

Explain under which circumstances _Tight Class Cohesion_ (TCC) and _Loose Class Cohesion_ (LCC) metrics produce the same value for a given Java class. Build an example of such as class and include the code below or find one example in an open-source project from Github and include the link to the class below. Could LCC be lower than TCC for any given class? Explain.

## Answer

### Same value

TCC and LCC metrics produce the same value for a given Java class when there is no undirect connections between methods.

### Example

```java
public class MyClass {
    private int x;
    private int y;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
```

Here there is 4 methods, so we have 6 method pairs.

The number of direct connections is 2 : ( getX(), setX() ), ( getY(), setY() ).

The number of undirect connections is 0 because there is no edge greater than 1.

So the value for TCC is 2 / 6.

And the value for LCC is (2 + 0) / 6 = 2 / 6.

### LCC lower than TCC

LCC cannot be lower than TCC for any given class. TCC that only deals with the direct connection between methods is a subset of LCC wich deals with direct and undirect connections. LCC can be equal or greater than the TCC.
