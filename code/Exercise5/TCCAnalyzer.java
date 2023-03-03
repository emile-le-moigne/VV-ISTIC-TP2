package fr.istic.vv.tcc_calculator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
