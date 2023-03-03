package fr.istic.vv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

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
