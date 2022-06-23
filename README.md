# What it can do

The `@ArgumentNameConstant` annotation generates a type which contains 1 constant for each argument in annotated method.
Much similar to lombok `@FieldNameConstants` but for method arguments.

It string constants (fields marked `public static final` of type `java.lang.String`). The constant field always has the
exact same name as the method argument name.

The generated type is called with following pattern `type name + method name + "Arguments"` and is public.

This might be useful for working
with [blocks__business-control-core](https://github.com/devkorol/blocks__business-control-core)

# Quick start

Mark some method with `@ArgumentNameConstant` annotation. The method should contain at least one argument for type
generation.

```
import com.github.devkorol.blocks.argument.name.constant.ArgumentNameConstant;

public class ArgumentNameConstantExample {

    @ArgumentNameConstant
    public void doSomeStuff(String name, Integer age) {
        ...
    }
}
```

The code will be followed with:

```
public class ArgumentNameConstantExampleDoSomeStuffArguments {
    public static final String name = "name";
    public static final String age = "age";
}
```