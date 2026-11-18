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
import java.util.Scanner;
// import java.util.List;

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

	/** CLASSES ********************************************************/

	// Still unsure if i even want to use classes like this but ill add them here for now.
	// 
	private class Token {
		int location;
		Token parent; // Possible parent-child implementation for certain tokens like variable declarations, function delimiters, if-else-statements, etc?

		public Token() { }
		public Token(int l) { location = l; }
		public Token(int l, Token p) { location = l; parent = p; }

		// TODO: Finish this class.
	}

	private class KeywordToken extends Token {
		
	}

	private class DelimiterToken extends Token {

	}

	private class IdentifierToken extends Token {

	}

	// Contains the tokenized content from a file (unsure if I want to do it this way.)
	private class TokenizedFileContent {
		// public List<List<KeywordToken>> keywords;
		// public List<List<DelimiterToken>> delimiters;
		// public List<List<IdentifierToken>> identifiers;
	}

	/*******************************************************************/
	
	/** METHODS ********************************************************/

	// Reads through a file, splits each line into fragments, and generates tokens based off them.  
	private TokenizedFileContent tokenizeFileContent(File file) {

		TokenizedFileContent tokenized = new TokenizedFileContent(); // Resulting content

		try (Scanner reader = new Scanner(file)) {
			for (int lineNumber = 0; reader.hasNextLine(); ++lineNumber) {

				// reader.nextLine() -> returns String representing line in file -> call '.split' with a regex pattern for whitespace onto string -> returns String[]
				String[] fragments = reader.nextLine().split("\\s");

				// Iterate through each fragment of the line.
				for (int frag = 0; frag<fragments.length; ++frag) {
					// TODO: Finish body
				}


			}

		} catch (FileNotFoundException e) { 
			sendErrorMessage(ERRORMSG_FAILED_TO_TOKENIZE_FILE);
		}

		return tokenized;

	}

	// Sends a passed error message and immediately exits the JVM.
	private static void sendErrorMessage(String msg) { System.err.println(msg); System.exit(-1); }
	
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
		File target = getValidFileFromInputArguments(args);


		// START TRANSLATION CODE HERE //

		//Reading file

		//Splitting text by spaces and saving it into an array
		
	}
	
	/*******************************************************************/
}
