package triangle.syntacticAnalyser;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import triangle.ErrorReporter;
import triangle.abstractSyntaxTrees.Program;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

/**
 * Tests compile-time folding of boolean binary expressions
 * for Task 7 of the coursework.
 */
public class TestBooleanFolding {

    @Test
    public void testBooleanFolding() {
        runFoldingTest("/booleans-to-fold.tri",
                4   // expected folded boolean expressions
        );
    }

    /**
     * Parses the given Triangle program, applies constant folding,
     * and checks the number of folded boolean results.
     *
     * @param filename resource file under src/test/resources/programs
     * @param expectedBooleans expected number of folded boolean results
     */
    private void runFoldingTest(String filename, int expectedBooleans) {
        // Load the Triangle program file
        SourceFile source = SourceFile.fromResource(filename);

        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        // Parse into AST
        Program ast = parser.parseProgram();
        assertEquals("Compilation errors in " + filename,
                0, reporter.getNumErrors());

        // Apply constant folding pass
        ConstantFolder folder = new ConstantFolder();
        ast.visit(folder, null);

        // Use the internal counter from ConstantFolder
        int actualFoldedBooleans = folder.getBooleanFoldCount();


        // Check results
        assertEquals("Incorrect number of folded boolean expressions",
                expectedBooleans,
                actualFoldedBooleans);
    }
}