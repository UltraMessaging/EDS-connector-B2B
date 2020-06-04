package org.jl7.hl7;

/**
 * Represents an HL7 subcomponent
 * 
 * @since 0.1
 * 
 * @author henribenoit
 * 
 */
public class HL7Subcomponent {
	/**
	 * Character used to mark the end of a segment and the beginning of the next
	 * segment (i.e. segment delimiter). Default: ASCII 10
	 * 
	 * @since 0.1
	 */
	char segmentTerminator = HL7Message.segmentTerminator;
	/**
	 * Character used to mark the end of a field and the beginning of the next
	 * // * field (i.e. field delimiter). Default: |
	 * 
	 * @since 0.1
	 */
	public char fieldSeparator = '|';
	/**
	 * Character used to mark the end of a component and the beginning of the
	 * next component (i.e. component delimiter). Default: ^
	 * 
	 * @since 0.1
	 */
	public char componentSeparator = '^';
	/**
	 * Character used to mark the end of a subcomponent and the beginning of the
	 * next subcomponent (i.e. subcomponent delimiter). Default: &
	 * 
	 * @since 0.1
	 */
	public char subcomponentSeparator = '&';
	/**
	 * Character used to mark the end of a field repetition and the beginning of
	 * the next field repetition (i.e. field repetition delimiter). Default: ~
	 * 
	 * @since 0.1
	 */
	public char repetitionSeparator = '~';
	/**
	 * Character used to escape characters which would otherwise be interpreted.
	 * Default: \
	 * 
	 * @since 0.1
	 */
	public char escapeCharacter = '\\';
	/**
	 * Value of this subcomponent.
	 */
	private String value;

	/**
	 * Returns the string representation of this subcomponent where all escape
	 * sequences have been replaced by their values.
	 * 
	 * @return the string representation of this subcomponent where all escape
	 *         sequences have been replaced by their values
	 * 
	 * @since 0.1
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the content of this subcomponent without interpreting escape
	 * sequences (i.e. this method should be used only if escape sequences have
	 * already been interpreted).
	 * 
	 * @param newValue
	 *            value to be set
	 */
	public void setValue(String newValue) {
		value = newValue;
	}

	/**
	 * Sets the content of this subcomponent after interpreting all escape
	 * sequences.
	 * 
	 * @param hl7String
	 *            string representation of an HL7 subcomponent
	 * @param delimiters
	 *            a string containing all delimiters defined for this segment:
	 *            fieldSeparator + componentSeparator + repetitionSeparator +
	 *            escapeCharacter + subcomponentSeparator
	 */
	public void setEscapedValue(String hl7String, String delimiters) {
		segmentTerminator = HL7Message.segmentTerminator;
		fieldSeparator = delimiters.charAt(0);
		componentSeparator = delimiters.charAt(1);
		repetitionSeparator = delimiters.charAt(2);
		escapeCharacter = delimiters.charAt(3);
		subcomponentSeparator = delimiters.charAt(4);
		String value = hl7String;
		value = value.replace("" + escapeCharacter + segmentTerminator, ""
				+ segmentTerminator);
		value = value.replace("" + escapeCharacter + fieldSeparator, ""
				+ fieldSeparator);
		value = value.replace("" + escapeCharacter + repetitionSeparator, ""
				+ repetitionSeparator);
		value = value.replace("" + escapeCharacter + componentSeparator, ""
				+ componentSeparator);
		value = value.replace("" + escapeCharacter + subcomponentSeparator, ""
				+ subcomponentSeparator);
		value = value.replace("\\E\\", "" + escapeCharacter);
		value = value.replace("\\F\\", "" + fieldSeparator);
		value = value.replace("\\R\\", "" + repetitionSeparator);
		value = value.replace("\\S\\", "" + componentSeparator);
		value = value.replace("\\T\\", "" + subcomponentSeparator);
		// TODO: support \Cxxyy\, \Mxxyyzz\, \Xdd…\ and \Zdd…\
		setValue(value);
	}

	/**
	 * Returns the string representation of this component (with escape characters).
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @since 0.1
	 */
	@Override
	public String toString() {
		String value = getValue();
		value = value.replace("" + escapeCharacter, "\\E\\");
		value = value.replace("" + subcomponentSeparator, "\\T\\");
		value = value.replace("" + componentSeparator, "\\S\\");
		value = value.replace("" + repetitionSeparator, "\\R\\");
		value = value.replace("" + fieldSeparator, "\\F\\");
		value = value.replace("" + segmentTerminator, "" + escapeCharacter
				+ segmentTerminator);
		return value;
	}
	// TODO: Add a method to get the text only (removing \H\ and \N\
}
