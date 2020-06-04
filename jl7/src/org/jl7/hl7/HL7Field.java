package org.jl7.hl7;

import java.util.ArrayList;

/**
 * Represents an HL7 field
 * 
 * @since 0.1
 * 
 * @author henribenoit
 * 
 */
public class HL7Field {
	/**
	 * Character used to mark the end of a component and the beginning of the
	 * next component (i.e. component delimiter). Default: ^
	 * 
	 * @since 0.1
	 */
	public char componentSeparator;
	/**
	 * Character used to mark the end of a subcomponent and the beginning of the
	 * next subcomponent (i.e. subcomponent delimiter). Default: &
	 * 
	 * @since 0.1
	 */
	public char subcomponentSeparator;
	/**
	 * Character used to mark the end of a field repetition and the beginning of
	 * the next field repetition (i.e. field repetition delimiter). Default: ~
	 * 
	 * @since 0.1
	 */
	public char repetitionSeparator;
	/**
	 * Character used to escape characters which would otherwise be interpreted.
	 * Default: \
	 * 
	 * @since 0.1
	 */
	public char escapeCharacter;
	/**
	 * List of all repetitions in this field.
	 */
	protected ArrayList<HL7FieldRepetition> repetitions = new ArrayList<HL7FieldRepetition>();

	/**
	 * Sets all delimiters defined for this field.
	 * 
	 * @param value
	 *            a string containing all delimiters defined for this field:
	 *            fieldSeparator (not used in this class) + componentSeparator +
	 *            repetitionSeparator + escapeCharacter + subcomponentSeparator
	 * 
	 * @since 0.1
	 */
	public void setDelimiters(String value) {
		componentSeparator = value.charAt(1);
		repetitionSeparator = value.charAt(2);
		escapeCharacter = value.charAt(3);
		subcomponentSeparator = value.charAt(4);
	}

	/**
	 * Removes all repetitions from this field and adds the given repetitions to
	 * it.
	 * 
	 * @param fields
	 *            array of string representations of repetitions to be added.
	 * 
	 * @param delimiters
	 *            a string containing all delimiters defined for this field:
	 *            fieldSeparator + componentSeparator + repetitionSeparator +
	 *            escapeCharacter + subcomponentSeparator
	 * 
	 * @param escapesInSubcomponents
	 *            whether escape characters are used or not
	 * 
	 * @since 0.1
	 */
	public void setRepetitions(String[] repetitions, String delimiters,
			boolean escapesInSubcomponents) {
		for (String repetition : repetitions) {
			this.repetitions.add(HL7Parser.parseFieldRepetition(repetition,
					delimiters, escapesInSubcomponents));
		}
	}

	/**
	 * Parses the string representation of an HL7 field and sets the repetitions
	 * in this field.
	 * 
	 * @param hl7String
	 *            string representation of an HL7 field
	 */
	public void setRepetitionsWithoutDelimiters(String hl7String) {
		this.repetitions.add(HL7Parser
				.parseFieldRepetitionWithoutDelimiters(hl7String));
	}

	/**
	 * Returns the repetition at the given position.
	 * 
	 * @param index
	 *            position in the field of the repetition to be returned
	 * 
	 * @return the repetition at the given position
	 * 
	 * @since 0.1
	 */
	public HL7FieldRepetition get(int index) {
		if (index < repetitions.size()) {
			return (HL7FieldRepetition) repetitions.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns the string representation of this field.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @since 0.1
	 */
	@Override
	public String toString() {
		String s = null;
		for (HL7FieldRepetition rep : repetitions) {
			if (s == null) {
				s = rep.toString();
			} else {
				s = s + repetitionSeparator + rep.toString();
			}
		}
		return s;
	}

	/**
	 * Returns the string representation of this field where all escape
	 * sequences have been replaced by their values.
	 * 
	 * @return the string representation of this field where all escape
	 *         sequences have been replaced by their values
	 * 
	 * @since 0.1
	 */
	public String getValue() {
		String s = null;
		for (HL7FieldRepetition rep : repetitions) {
			if (s == null) {
				s = rep.getValue();
			} else {
				s = s + repetitionSeparator + rep.getValue();
			}
		}
		return s;
	}
}
