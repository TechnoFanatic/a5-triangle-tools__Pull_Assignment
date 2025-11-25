package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class TestCurlyCommand {

    @Test
    public void testCurlyCommand() {
        compileExpectSuccess("/while-curly.tri");
    }

    private void compileExpectSuccess(String filename) {
        // The programs directory is configured as a test resource in build.gradle
        SourceFile source = SourceFile.fromResource(filename);

        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        // Compile the program
        parser.parseProgram();

        // Check that there were no errors
        assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
    }
}