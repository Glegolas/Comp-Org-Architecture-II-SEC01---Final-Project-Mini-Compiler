/* PROJECT: MINI-COMPILER 
 * START DATE: 11/16/2026
 *
 *
 * GOAL: Convert .txt high-level language into assembly using Java.
 *
 *
 * TODO: 
 *	1. Get input file from user [-a little progress-] 
 *
 *    	(easy: idea A): Get file-path from the terminal by calling the program directly with a command like "java Main [insert file path here]" 
 *                - PROS: Immediately passes a String array to the 'public static void main' method in ordered fragments, 
 *                - CONS: Relies on the terminal for input, retrieving exact filepaths can get tedious depending on there location
 *
 *      (medium: idea B): Get file-path from a ui-interface using a java ui library like swing.
 *                - PROS: Extremely easy to navigate for file selection.
 *                - CONS: Using an external library could introduce unknown errors/interactions,
 *                        the library could be unsupported or broken on certain operating systems.
 *
 *      (comment) Liam: I'd say implementing idea A first then if enough time is avaliable, we could make idea B an optional feature through a command argument.                        
 *
 *  2. Read file and translate it into machine-code that will be representated as a giant string. [-unfinished-] 
 *     
 *      (comment) Liam: Making a giant string could also NOT be the move here, but I'm just putting it here for now.
 *      (comment) Jared: Identify each type of text by splitting where the spaces are, giving them all a different meaning based on if it is a keyword, identifier, or delimiter. Save their location in the file by line/column
 *      (comment) Liam: We could make each token type a seperate class (KeywordToken, IdentifierToken, and DelimiterToken) and store them in a parent class (TokenizedFileContent) to organize.
 *                      Just trying to think of how each token is going to interact with one another and how location is going to be tracked between them.
 *      
 *
 *  3. Turn the string into a .s output file. [-unfinished-]
 *
 *  4. Run the file. [-unfinished-]
 *
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Main {

	/** MEMBERS *********************************************************/
	
	// FILE-RELATED CONSTANTS
	private static final String ACCEPTED_FILE_EXTENSION = ".txt";
	
	// ERROR MESSAGES
	private static final String ERRORMSG_NO_ARGUMENTS = "No input file path was given.";
	private static final String ERRORMSG_INVALID_PATH = "The input file's path is invalid.";
	private static final String ERRORMSG_INVALID_FILETYPE = "The input file is not a valid file type.";
	private static final String ERRORMSG_INPUT_FILE_IS_UNREADABLE = "The input file is unreadable.";
	private static final String ERRORMSG_FAILED_TO_TOKENIZE_FILE = "The input file cannot be tokenized.";

	// PATTERNS
	private static final String DELIMITING_REGEX = "\\s+|(?<=;)|(?=;)";
	private static final String VARIABLE_REGEX   = "([a-zA-Z_])[a-zA-z0-9_]*";
	private static final String INTEGER_REGEX    = "0|([-]?[1-9][0-9]*)";

	/** ENUMS ***********************************************************/

	private enum DataType {
		INT,
		//FLOAT,
		BOOL
	}

	private enum KeywordType {
		INIT_INT,
		//INIT_FLOAT,
		INIT_BOOL,
		RETURN,
		//BREAK,
		IF,
		ELSE
	}

	private enum OperatorType {
		EQ,      // ==
		NEQ,     // !=
		GREATER, // >
		LESSER,  // <
		GRTEQ,   // >=
		LESEQ,   // <=
		NOT,     // ! (implementation will require this to be a seperate token so it needs to be in the delimiter pattern)
		INIT,    // =
		ADD,     // +
		SUB,     // -
		MUL,     // *
		DIV      // /
	}

	private enum PunctuationType {
		END,         // ;
		END_SCOPE,   // }
		START_SCOPE, // {
	}

	private enum ExpressionType {
		BOOL,
		INT,
		//FLOAT
	}

	/** CLASSES ********************************************************/

	// Still unsure if i even want to use classes like this but ill add them here for now.
	// 

	// INTERFACES
	private static interface SyntaxConsumer {
		public boolean canStartLine();
		public void consume(ArrayList<Token> line);
	}

	// ABSTRACTS
	private static abstract class Token {
		public int lineNumber;
		public int linePos;
		public String fragment; 

		public Token(String fragment, int lineNumber, int linePos) { this.fragment = fragment; this.lineNumber = lineNumber; this.linePos = linePos; }
		public boolean canEndLine() { return false; }
	}

	private static abstract class Literal extends Token {
		public Literal(String fragment, int lineNumber, int linePos) { super(fragment, lineNumber, linePos); }
	}

	// TOKENS
	private static class Keyword extends Token implements SyntaxConsumer {
		public KeywordType keywordType;
		public Keyword(String fragment, int lineNumber, int linePos, KeywordType keywordType) { super(fragment, lineNumber, linePos); this.keywordType = keywordType; }
		public boolean canStartLine() { return keywordType == KeywordType.RETURN || keywordType == KeywordType.INIT_BOOL || keywordType == KeywordType.INIT_INT; }
		public void consume(ArrayList<Token> line) { 
			int ogLineSize  = line.size();
			Token nextToken = line.get(linePos+1);
			Token lastToken = line.get(ogLineSize-1);

			if (!(lastToken instanceof Punctuation) || ((Punctuation)lastToken).puncType != PunctuationType.END) { sendUniqueTokenErrorMessage(lastToken); }

			switch(keywordType) {
				case KeywordType.INIT_BOOL, KeywordType.INIT_INT:
					if (nextToken instanceof Identifier id && id.initializer != null) {
						id.initializer = this;

						if (nextToken == lastToken) { break; } // POSSIBLE LATE INITIALIZER

						int expPos = linePos+3;
						Expression exp = null;
						switch(keywordType) {
							case KeywordType.INIT_BOOL -> exp = new Expression(lineNumber, expPos, ExpressionType.BOOL);
							case KeywordType.INIT_INT  -> exp = new Expression(lineNumber, expPos, ExpressionType.INT);
						}

						if (exp == null) { sendUniqueTokenErrorMessage(this); }

						// TODO: Expression should take up one token by the end of compression phase, meaning that it will reduce the amount of 
						//       tokens in the line passed to it.
						//
						//       Implementation of this should be simple, but it might constitute creating a function for expression creation
						//       for better reuse
						//
						for (int i = expPos; i<ogLineSize-2; ++i) {
							exp.push(line.get(i));
						}

					} else { sendUniqueTokenErrorMessage(nextToken); } // Next token needs to be an uninitialized identifier if the keyword is a identifier initializer.
					break;
			}
		}
	}

	private static class Punctuation extends Token {
		public PunctuationType puncType;
        public Punctuation(String fragment, int lineNumber, int linePos, PunctuationType puncType) { super(fragment, lineNumber, linePos); this.puncType = puncType; }
		public boolean canEndLine() { return puncType == PunctuationType.END || puncType == PunctuationType.START_SCOPE || puncType == PunctuationType.END_SCOPE; }
	}

	private static class Operator extends Token {
		public OperatorType opType;
        public Operator(String fragment, int lineNumber, int linePos, OperatorType opType) { super(fragment, lineNumber, linePos); this.opType = opType; }
	}

	private static class Identifier extends Token implements SyntaxConsumer {
		public Keyword initializer;
		public Literal value;
		public int refCount = 0;
		public Identifier(String fragment, int lineNumber, int linePos) { super(fragment, lineNumber, linePos); }
		public boolean canStartLine() { return initializer != null; }
		public void consume(ArrayList<Token> line) { 

		}
	}

	private static class Expression extends Token {
		public ArrayList<Token> content;
		public ExpressionType expType;
		public boolean containsIdentifiers = false;
        public Expression(int lineNumber, int linePos, ExpressionType expType) {
			String fragment; 
			switch(expType) {
				case ExpressionType.BOOL -> fragment = "<bool-expression>";
				case ExpressionType.INT -> fragment = "<int-expression>";
				default -> fragment = "<ambigious-expression>";
			}
			super(fragment, lineNumber, linePos);
			this.expType = expType;
        }

		public void push(Token nextToken) {
			Token last = content.getLast();

			switch(expType) {
				case ExpressionType.INT:
					if (last == null || last instanceof Operator) {
						if (nextToken instanceof IntLiteral intlit) {
							content.add(intlit);
						} else if (nextToken instanceof Identifier id && id.initializer != null && id.initializer.keywordType == KeywordType.INIT_INT) {
							containsIdentifiers = true;
							content.add(id);
						} 
						else { sendUniqueTokenErrorMessage(nextToken); }

					} else if (last instanceof IntLiteral || last instanceof Identifier) {
						if (nextToken instanceof Operator op && (op.opType == OperatorType.ADD || op.opType == OperatorType.DIV || op.opType == OperatorType.SUB || op.opType == OperatorType.MUL)) {
							content.add(op);
						} else { sendUniqueTokenErrorMessage(nextToken); }
					} else { sendUniqueTokenErrorMessage(last); } // Not a valid operator, literal, or identifier

					break;
			}
		}

		public Token end() {
			Token last = content.getLast();
			if (last == null || !(last instanceof IntLiteral || (last instanceof Identifier id && id.value instanceof IntLiteral))) { sendUniqueTokenErrorMessage(this); }
			if (content.size() == 1) { last.linePos = linePos; return last; }
			if (!containsIdentifiers) { // We can evaluate it out to a literal.
				// TODO: DO EVALUATION HERE
			}

			return this;	
		}
	}

	// LITERALS
	private static class BoolLiteral extends Literal {
		public boolean value;
		public BoolLiteral(String fragment, int lineNumber, int linePos, boolean value) { super(fragment, lineNumber, linePos); this.value = value; }
	}

	private static class IntLiteral extends Literal {
		public int value;
		public IntLiteral(String fragment, int lineNumber, int linePos, int value) { super(fragment, lineNumber, linePos); this.value = value; }
	}



	/*******************************************************************/

	/** DEBUGGING ******************************************************/

	private static void printTokens(ArrayList<ArrayList<Token>> tokens) {
		for (int lineNumber = 0; lineNumber<tokens.size(); ++lineNumber) {
			ArrayList<Token> line = tokens.get(lineNumber);
			System.out.print("LINE " + lineNumber + ":   ");
			for (int linePos = 0; linePos<line.size(); ++linePos) {
				System.out.print(line.get(linePos).getClass().getName() + " ");
			}
			System.out.println();
		}
	}

	/*******************************************************************/
	
	/** METHODS ********************************************************/

	// TODO: FINISH
	// SYNTAX ANALYSIS: Takes the tokenized file-input and checks for grammar and optimizations.
	//
	// UPDATE 1: Implementation is still incomplete. The draft-system will work like this:
	//
	//
	// 1. Iterate through each line -> pick a line number
	//
	//
	// 2. Get end token in line -> see if it is allowed to end line through the 'canEndLine' function.
	//
	//
	// 3. Get start token in line -> check if its a 'SyntaxConsumer' (a token that controls structure of grammar) and if it can be called at the start
	//
	//    3a. SyntaxConsumers would be identifiers (variables) and keywords (if-statements, variable initializers, return)
	//
	//    3b. certain SyntaxConsumers can become undeclarable at the start due to certain conditions, like a variable name being called that hasn't been initialized yet.
	//
	//
	// 4. Feed the line into the SyntaxConsumer's consume function. This function will do the following:
	//   
	//    1. Check if each token in the line fits with the consumer's grammar rules.
	//
	//    2. Compress groups of tokens into expression tokens that can either stay as is or be optimially converted to literals (depending if the statement contains variables).
	//
	private static ArrayList<ArrayList<Token>> syntaxAnalysis(ArrayList<ArrayList<Token>> tokens) {

		for (int lineNumber = 0; lineNumber<tokens.size(); ++lineNumber) {
			ArrayList<Token> line = tokens.get(lineNumber);

			Token end   = line.get(line.size()-1);
			if (!(end.canEndLine()) ) { sendUniqueTokenErrorMessage(end); }

			Token start = line.get(0);
			if (!(start instanceof SyntaxConsumer) || !(((SyntaxConsumer)start).canStartLine())) { sendUniqueTokenErrorMessage(start); }
			((SyntaxConsumer)start).consume(line);
		}

		return tokens;
	}

	// LEXICAL ANALYSIS: Reads through a file, splits each line into fragments, and generates tokens based off them. Returns all tokens in an 2d arraylist with each row representing
	// the tokens in each line.
	private static ArrayList<ArrayList<Token>> lexicalAnalysis(File file) {

		HashMap<String, Identifier> idCache = new HashMap<String, Identifier>();
		ArrayList<ArrayList<Token>> lines = new ArrayList<ArrayList<Token>>();

		try (Scanner reader = new Scanner(file)) {
			for (int lineNumber = 0; reader.hasNextLine(); ++lineNumber) {

				// reader.nextLine() -> returns String representing line in file -> call '.split' with a regex pattern for whitespace/semicolons onto string -> returns String[]
				String[] fragments = reader.nextLine().split(DELIMITING_REGEX);
				if (fragments.length == 0) { continue; }

				ArrayList<Token> line = new ArrayList<Token>();
				lines.add(line);

				// Iterate through each fragment of the line.
				for (int linePos = 0; linePos<fragments.length; ++linePos) {

					// TODO: Finish body
					String fragment = fragments[linePos];
					// System.out.println("FRAGMENT AT " + linePos + ": " + fragment);

					Token token = null;
					switch(fragment) {
						case ";"     : token = new Punctuation(fragment, lineNumber, linePos, PunctuationType.END); break;
						case "="     : token = new Operator(fragment, lineNumber, linePos, OperatorType.INIT); break;
						case "=="    : token = new Operator(fragment, lineNumber, linePos, OperatorType.EQ); break;
						case "int"   : token = new Keyword(fragment, lineNumber, linePos, KeywordType.INIT_INT); break;
						case "false" : token = new BoolLiteral(fragment, lineNumber, linePos, false); break;
						case "true"  : token = new BoolLiteral(fragment, lineNumber, linePos, true); break;
						default      :
							if (fragment.matches(VARIABLE_REGEX)) {
								Identifier id = idCache.get(fragment);
								if (id != null) {
									token = id;
								} else {
									id = new Identifier(fragment, lineNumber, linePos);
									token = id;
									idCache.put(fragment, id);
								}
							} else if (fragment.matches(INTEGER_REGEX)) {
								token = new IntLiteral(fragment, lineNumber, linePos, Integer.parseInt(fragment));
							} else {
								throw new FileNotFoundException();
							}
							break;
					}

					line.add(token);
				}

			}

		} catch (FileNotFoundException e) { 
			sendErrorMessage(ERRORMSG_FAILED_TO_TOKENIZE_FILE);
		}

		return lines;

	}

	// Sends a passed error message and immediately exits the JVM.
	private static void sendErrorMessage(String msg) { System.err.println(msg); System.exit(-1); }

	// Sends an error message using a token as the context.
	private static void sendUniqueTokenErrorMessage(Token token) { sendErrorMessage("FAILED TO TOKENIZE FILE: Line " + token.lineNumber + " at token " + token.linePos + " : " + token.fragment); }
	
	// Accepts a list of strings representing command-line arguments.
	// The primary argument (first argument) would be the file-path to the 
	// input file.
	private static File getValidFileFromInputArguments(String[] args) {
		
		// ERROR CHECK: No arguments passed.
		if (args.length == 0) { sendErrorMessage(ERRORMSG_NO_ARGUMENTS); }
		
		// Make a file object from the assumed path.
		// File objects do not check if they are valid, so manual validity
		// checks are needed.
		File file = new File(args[0]);
		
		// ERROR CHECK: Does the file not exist?
		if (!file.isFile()) { sendErrorMessage(ERRORMSG_INVALID_PATH); }
		
		// ERROR CHECK: Is the file not readable?
		if (!file.canRead()) { sendErrorMessage(ERRORMSG_INPUT_FILE_IS_UNREADABLE); }
		
		// ERROR CHECK: Does the file have a readable filetype?
		String filename = file.getName();
		if (filename.length() < 3 || !filename.endsWith(ACCEPTED_FILE_EXTENSION)) { sendErrorMessage(ERRORMSG_INVALID_FILETYPE); }
		
		return file;
	}
	
	public static void main(String[] args) {
		printTokens( // DEBUG PURPOSES ONLY. WILL BE REPLACED WITH SYNTAXANALYSIS WHEN FINISHED.
			lexicalAnalysis(
				getValidFileFromInputArguments(args)
			)
		);


		// START TRANSLATION CODE HERE //

		//Reading file

		//Splitting text by spaces and saving it into an array
		
	}
	
	/*******************************************************************/
}
