# Code of your exercise

```java
public class TCCAnalyzer {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println("Should provide the path to the source code");
            System.exit(1);
        }

        File folder = new File(args[0]);
        if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        // Parse the java files

        List<File> javaFiles = new ArrayList<>();

        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }
        File tableFile = new File(args[0] + "/" + "tccValues.md");
        FileWriter tableWriter = new FileWriter(tableFile);
        tableWriter.write("| Class Name | N | NP | NDC | TCC |\n");
        tableWriter.write("| ---------- | --------- | --------- | --------- | --------- |\n");
        // Compute the TCC for each class
        javaFiles.forEach(
                javaFile -> {
                    try {

                        File graphFile = new File(args[0] + "/" + javaFile.getName() + "Graph.dot");
                        FileWriter graphWriter = new FileWriter(graphFile);

                        TCCCalculator.calculateTCC(javaFile, graphWriter, tableWriter);
                        graphWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        tableWriter.close();
    }
}
```

```java
public class TCCCalculator {

    /*
     * NP = maximum number of possible connections
     * = N * (N − 1) / 2 where N is the number of methods
     *
     * NDC = number of direct connections (number of edges in the connection graph)
     *
     * Tight class cohesion TCC = NDC / NP
     */

    public static double calculateTCC(File file, Writer graph, Writer table) throws IOException {

        CompilationUnit cu = StaticJavaParser.parse(file);
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        if (methods.isEmpty() || methods.size() == 1) {
            return 0;
        }
        double n = methods.size(); // number of methods
        double np = n * (n - 1) / 2; // maximum number of possible connections

        Map<MethodDeclaration, Set<VariableDeclarator>> directConnection = new HashMap<MethodDeclaration, Set<VariableDeclarator>>();

        for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
            VariableDeclarator variable = field.getVariables().get(0);
            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                boolean usesVariable = method.getBody().map(body -> {
                    AtomicBoolean result = new AtomicBoolean(false);
                    body.walk(node -> {
                        if (node instanceof NameExpr) {
                            NameExpr nameExpr = (NameExpr) node;
                            if (nameExpr.getName().asString().equals(variable.getName().asString())) {
                                result.set(true);
                            }
                        }
                    });
                    return result.get();
                }).orElse(false);

                // Si la méthode utilise l'attribut, ajoute la paire à la liste connections
                if (usesVariable) {
                    Set<VariableDeclarator> variableSet = directConnection.computeIfAbsent(method,
                            k -> new HashSet<>());
                    variableSet.add(variable);
                }
            }
        }

        double ndc = 0; // number of direct connections

        Set<MethodDeclaration> passedMethods = new HashSet<MethodDeclaration>();

        graph.write("graph {\n");

        for (Map.Entry<MethodDeclaration, Set<VariableDeclarator>> entry : directConnection.entrySet()) {
            MethodDeclaration currentMethod = entry.getKey();
            Set<VariableDeclarator> currentVariables = entry.getValue();
            int currentDirectConnections = 0;
            for (Map.Entry<MethodDeclaration, Set<VariableDeclarator>> otherEntry : directConnection.entrySet()) {

                Set<VariableDeclarator> otherVariables = otherEntry.getValue();
                if (!entry.equals(otherEntry) && !passedMethods.contains(otherEntry.getKey())
                        && (otherVariables.containsAll(currentVariables)
                                || currentVariables.containsAll(otherVariables))) {
                    currentDirectConnections++;
                    graph.write(toGraphConnection(entry.getKey(), otherEntry.getKey(), currentVariables));

                }
            }
            passedMethods.add(currentMethod);
            ndc += currentDirectConnections;
        }
        graph.write("}");
        double tcc = ndc / np;

        table.write(String.format("| %s | %f | %f | %f | %f |\n", cu.getType(0).getName(), n, np, ndc, tcc));

        return tcc;
    }

    private static String toGraphConnection(MethodDeclaration method1, MethodDeclaration method2,
            Set<VariableDeclarator> variables) {

        String method1Name = method1.getNameAsString();
        String method2Name = method2.getNameAsString();

        StringBuilder sb = new StringBuilder();

        for (VariableDeclarator variable : variables) {
            sb.append(variable.getNameAsString());
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String variablesLabel = sb.toString();
        return method1Name + " -- " + method2Name + "[label=\"" + variablesLabel + "\"];\n";
    }
}
```
