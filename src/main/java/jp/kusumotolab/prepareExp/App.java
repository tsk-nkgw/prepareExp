/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jp.kusumotolab.prepareExp;

import jp.kusumotolab.prepareExp.visitor.InitializeVisitor;
import jp.kusumotolab.prepareExp.visitor.ModifyModifierVisitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(final String[] args) throws IOException {
        final ASTVisitor visitor = args[1].equals("0") ? new InitializeVisitor() : new ModifyModifierVisitor();
        getAllJavaPath(Paths.get(args[0])).forEach(e -> {
            final ASTParser astParser = ASTParser.newParser(AST.JLS11);
            byte[] bytes = null;

            try {
                bytes = Files.readAllBytes(e);
            } catch (IOException e1) {
                System.exit(1);
            }

            final String oldeContent = new String(bytes, StandardCharsets.UTF_8);
            astParser.setSource(oldeContent.toCharArray());
            final CompilationUnit unit = (CompilationUnit) astParser.createAST(new NullProgressMonitor());
            unit.accept(visitor);

            final Document document = new Document(oldeContent);
            final TextEdit textEdit = unit.rewrite(document, null);

            try {
                textEdit.apply(document);
            } catch (final BadLocationException e1) {
                System.exit(2);
            }

            final String newContent = document.get();

            try {
                Files.write(e, newContent.getBytes(StandardCharsets.UTF_8));
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    public static List<Path> getAllJavaPath(final Path directory) throws IOException {
        return Files.walk(directory).filter(e -> e.toString().endsWith(".java"))
                .collect(Collectors.toList());
    }
}
