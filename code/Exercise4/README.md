# Code of your exercise

```java
public class TestWriter {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Should provide the path to the source code");
            System.exit(1);
        }

        File directory = new File(args[0]);
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        String fileName = "thirdParser.txt";
        FileWriter writer = new FileWriter(args[0] + "/" + fileName);
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                System.out.println(file.getName());
                // SourceRoot sourceRoot = new SourceRoot(file.toPath());
                CompilationUnit cu = StaticJavaParser.parse(file);
                cu.accept(new GetterVisitor(), null);
                GetterVisitor visitor = new GetterVisitor();
                for (ClassOrInterfaceDeclaration classDeclaration : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                    List<FieldDeclaration> fields = classDeclaration.getFields();
                    for (FieldDeclaration field : fields) {
                        visitor.publicGetters(field, file.getName(), writer, null);
                    }
                }

            }
        }
        writer.close();
    }
}
```

```java
public class GetterVisitor extends VoidVisitorWithDefaults<Void> {

    public void publicGetters(FieldDeclaration field, String className, FileWriter writer, Void arg)
            throws IOException {
        VariableDeclarator variable = field.getVariable(0);
        String fieldName = variable.getNameAsString();
        Type type = variable.getType();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        boolean hasGetter = field.getParentNode().get().findFirst(MethodDeclaration.class,
                arg0 -> arg0.getType().equals(type) && arg0.getNameAsString().equals(getterName) && arg0.isPublic())
                .isPresent();
        if (!hasGetter) {
            writer.write("Found no public getter in " + className + " for field: " + type.toString() + " " + fieldName
                    + "\n");
        }
        super.visit(field, arg);
    }

    public Optional<String> noPublicGetter(FieldDeclaration field, Void arg) {
        System.out.println("hey");
        String fieldName = field.getVariable(0).getNameAsString();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        boolean hasGetter = field.getParentNode().get().findFirst(MethodDeclaration.class,
                m -> m.getNameAsString().equals(getterName) && m.isPublic()).isPresent();
        if (!hasGetter) {
            return Optional.of("Found no public getter for field: " + fieldName);
        }
        super.visit(field, arg);
        return Optional.empty();
    }
}
```
