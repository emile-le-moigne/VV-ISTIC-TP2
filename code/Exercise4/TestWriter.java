package fr.istic.vv;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/*
 * Try to read a directory, and copy the code of all java files in a txt file.
 */
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
