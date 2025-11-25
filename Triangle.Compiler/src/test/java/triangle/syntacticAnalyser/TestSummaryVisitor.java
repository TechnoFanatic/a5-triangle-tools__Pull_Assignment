package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import triangle.contextualAnalyzer.SummaryVisitor;
import triangle.abstractSyntaxTrees.Program;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.ErrorReporter;

public class TestSummaryVisitor {

    @Test
    public void testDoubleProgram() {
        runSummaryAndExpectCounts("/double.tri", 1, 0); // expected counts according to AST
    }

    @Test
    public void testHiProgram() {
        runSummaryAndExpectCounts("/hi.tri", 0, 2); // expected counts according to AST
    }

    private void runSummaryAndExpectCounts(String filename, int expectedInts, int expectedChars) {
        SourceFile source = SourceFile.fromResource(filename);
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        Program ast = parser.parseProgram();
        assertEquals("Compilation errors in " + filename, 0, reporter.getNumErrors());

        SummaryVisitor visitor = new SummaryVisitor();
        ast.visit(visitor, null);

        assertEquals("IntegerExpression count mismatch", expectedInts, visitor.getIntegerExpressionCount());
        assertEquals("CharacterExpression count mismatch", expectedChars, visitor.getCharacterExpressionCount());
    }
}