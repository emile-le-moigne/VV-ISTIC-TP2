package fr.istic.vv.tcc_calculator;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.w3c.dom.Node;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

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