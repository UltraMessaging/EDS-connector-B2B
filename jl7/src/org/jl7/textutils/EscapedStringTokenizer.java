package org.jl7.textutils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * The EscapedStringTokenizer class allows an application to break a string into
 * tokens. The tokenizer creates a list of tokens as soon as the constructor is
 * called (unlike java.util.StringTokenizer).<br>
 * <br>
 * The set of separators (i.e. the delimiters, the characters that separate
 * tokens) may be specified.<br>
 * <br>
 * An instance of EscapedStringTokenizer behaves in one of two ways, depending
 * on whether it was created with the returnSeparators flag having the value
 * true or false:
 * <ul>
 * <li>If the flag is false, separator characters only serve to separate tokens.
 * A token is a maximal sequence of consecutive characters that are not
 * separators (or that are separators following the escape character).</li>
 * <li>If the flag is true, separator characters are themselves considered to be
 * tokens. A token is thus either one separator character, or a maximal sequence
 * of consecutive characters that are not separators (or that are separators
 * following the escape character).</li>
 * </ul>
 * 
 * @author henribenoit
 * 
 */
public class EscapedStringTokenizer implements Enumeration<String> {
	/**
	 * String to tokenize
	 */
	private String input;
	/**
	 * String containing the list of separators (each character in the string
	 * defines a separator)
	 */
	private String separator;
	/**
	 * Escape character (if one of the separator characters follows this
	 * character in the input string, it will not be considered as a separator
	 */
	private char escape;
	/**
	 * List of all tokens
	 */
	private ArrayList<String> array;
	/**
	 * whether the separators found in the string should be returned as tokens
	 */
	private boolean returnSeparators;
	/**
	 * index of the next element to be returned by the enumeration
	 */
	private int nextIndex = 0;

	/**
	 * Creates an EscapedStringTokenizer witch can handle multiple separators
	 * and doesn't return the separator as tokens.
	 * 
	 * @param input
	 *            String to tokenize
	 * @param separator
	 *            String containing the list of separators (each character in
	 *            the string defines a separator)
	 * @param escape
	 *            Escape character (if one of the separator characters follows
	 *            this character in the input string, it will not be considered
	 *            as a separator
	 * 
	 * @since 0.1
	 */
	public EscapedStringTokenizer(String input, String separator, char escape) {
		this(input, separator, escape, false);
	}

	/**
	 * Creates an EscapedStringTokenizer witch can only handle a single
	 * separator and doesn't return the separator as tokens.
	 * 
	 * @param input
	 *            String to tokenize
	 * @param separator
	 *            Character used as separator to tokenize the input string
	 * @param escape
	 *            Escape character (if the separator character follows this
	 *            character in the input string, it will not be considered as a
	 *            separator
	 * 
	 * @since 0.1
	 */
	public EscapedStringTokenizer(String input, char separator, char escape) {
		this(input, separator + "", escape, false);
	}

	/**
	 * Creates an EscapedStringTokenizer witch can only handle a single
	 * separator and can return the separator as tokens.
	 * 
	 * @param input
	 *            String to tokenize
	 * @param separator
	 *            Character used as separator to tokenize the input string
	 * @param escape
	 *            Escape character (if the separator character follows this
	 *            character in the input string, it will not be considered as a
	 *            separator
	 * @param returnSeparators
	 *            whether the separators found in the string should be returned
	 *            as tokens
	 * 
	 * @since 0.1
	 */
	public EscapedStringTokenizer(String input, char separator, char escape,
			boolean returnSeparators) {
		this(input, separator + "", escape, returnSeparators);
	}

	/**
	 * Creates an EscapedStringTokenizer witch can handle multiple separators
	 * and return separators as tokens.
	 * 
	 * @param input
	 *            String to tokenize
	 * @param separator
	 *            String containing the list of separators (each character in
	 *            the string defines a separator)
	 * @param escape
	 *            Escape character (if one of the separator characters follows
	 *            this character in the input string, it will not be considered
	 *            as a separator
	 * @param returnSeparators
	 *            whether the separators found in the string should be returned
	 *            as tokens
	 * 
	 * @since 0.1
	 */
	public EscapedStringTokenizer(String input, String separator, char escape,
			boolean returnSeparators) {
		this.input = input;
		this.separator = separator;
		this.escape = escape;
		this.returnSeparators = returnSeparators;
		split();
	}

	/**
	 * Splits the input string according to the list of separators, escape
	 * character and whether separators should be returned as tokens
	 */
	private void split() {
		char lastChar = ' ';
		StringBuilder builder = new StringBuilder();
		array = new ArrayList<String>();

		CharacterIterator it = new StringCharacterIterator(input);
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (!isSeparator(c)) {
				builder.append(c);
			}
			else if (lastChar == escape) {
				builder.deleteCharAt(builder.toString().length() - 1);
				builder.append(c);
			} else {
				array.add(builder.toString());
				builder = new StringBuilder();
				if (returnSeparators) {
					builder.append(c);
					array.add(builder.toString());
					builder = new StringBuilder();
				}
			}
			lastChar = c;
		}
		if (builder.length() > 0) {
			array.add(builder.toString());
		}
	}

	/**
	 * Returns whether the character is contained in the list of separators.
	 * 
	 * @param c
	 *            character to check
	 * @return whether the character is contained in the list of separators.
	 */
	private boolean isSeparator(char c) {
		CharacterIterator it = new StringCharacterIterator(separator);
		for (char c2 = it.first(); c2 != CharacterIterator.DONE; c2 = it.next()) {
			if (c == c2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the list of tokens as an array of strings
	 * 
	 * @return the list of tokens as an array of strings
	 * 
	 * @since 0.1
	 */
	public String[] getTokens() {
		return array.toArray(new String[array.size()]);
	}

	/**
	 * 
	 * @see java.util.Enumeration#hasMoreElements()
	 * 
	 * @since 0.1
	 */
	public boolean hasMoreElements() {
		return (nextIndex < array.size());
	}

	/**
	 * 
	 * @see java.util.Enumeration#nextElement()
	 * 
	 * @since 0.1
	 */
	public String nextElement() {
		String nextElement = array.get(nextIndex);
		nextIndex++;
		return nextElement;
	}
}
