/*
 * @(#)Compiler.java                       
 * 
 * Revisions and updates (c) 2022-2025 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 * 
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle;

import java.util.List;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import triangle.contextualAnalyzer.SummaryVisitor;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

/**
 * The main driver class for the Triangle compiler.
 */
public class Compiler {

    /**
     * Command-line options parsed via cli-parser.
     */
	public static class CLIOptions {
	/** The filename for the object program, normally obj.tam. */
    @Argument(description = "The filename for the object program", alias = "o")
    public String objectName = "obj.tam";

    @Argument(description = "Show the AST after contextual analysis")
    public boolean showTree = false;

    @Argument(description = "Enable constant folding optimisation")
    public boolean folding = false;

    @Argument(description = "Show the AST after folding is complete")
    public boolean showTreeAfter = false;

    @Argument(description = "Show summary statistics of the program")
    public boolean showStats = false;
	}

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName   the name of the file containing the source program.
	 * @param objectName   the name of the file containing the object program.
	 * @param showingAST   true iff the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName,
                                  String objectName,
                                  boolean showingAST,
                                  boolean showingTable,
                                  boolean folding,
                                  boolean showTreeAfter,
                                  boolean showStats) {

		System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceName);

		if (source == null) {
			System.out.println("Can't access source file " + sourceName);
			System.exit(1);
		}

		scanner = new Scanner(source);
		reporter = new ErrorReporter(false);
		parser = new Parser(scanner, reporter);
		checker = new Checker(reporter);
		emitter = new Emitter(reporter);
		encoder = new Encoder(emitter, reporter);
		drawer = new Drawer();

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {

            // Run summary stats if requested (can be done after parsing)
            if (showStats) {
                SummaryVisitor visitor = new SummaryVisitor();
                theAST.visit(visitor, null);
                visitor.printStats();
            }
			
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass

			// Show AST after contextual analysis, if requested
			if (showingAST) {
				drawer.draw(theAST);
			}

			// Constant folding and optional tree display afterwards
			if (folding) {
				theAST.visit(new ConstantFolder());

				if (showTreeAfter) {
					drawer.draw(theAST);
				}
			}
			
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
			}
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else {
			System.out.println("Compilation was unsuccessful.");
		}
		return successful;
	}

	/**
	 * Triangle compiler main program.
	 *
	 * @param args the command-line arguments. First non-option argument specifies the
	 *             source filename. Options:
	 *             --objectName=<filename>
	 *             --showTree
	 *             --folding
	 *             --showTreeAfter
	 */
	public static void main(String[] args) {

		CLIOptions options = new CLIOptions();

		// Parse flags and get remaining positional args
        List<String> extraArgs;
        try {
            extraArgs = Args.parse(options, args);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error parsing arguments: " + ex.getMessage());
            Args.usage(options);
            System.exit(1);
            return; // unreachable, but keeps compiler happy
        }

        if (extraArgs.isEmpty()) {
            System.out.println("Usage: tc <sourcefile> [--objectName=<output>] [--showTree] [--folding] [--showTreeAfter]");
            Args.usage(options);
            System.exit(1);
        }
		
		

		String sourceName = extraArgs.get(0);
		
		boolean compiledOK =
            compileProgram(sourceName,
                           options.objectName,
                           options.showTree,
                           false, // showingTable unused
                           options.folding,
                           options.showTreeAfter,
                            options.showStats);

		if (!options.showTree && !options.showTreeAfter) {
			System.exit(compiledOK ? 0 : 1);
		}
	}
}
