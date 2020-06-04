package org.jl7.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Represents an HL7 segment
 * 
 * @since 0.1
 * 
 * @author henribenoit
 * 
 */
public class HL7Segment {
	/**
	 * Character used to mark the end of a field and the beginning of the next
	 * field (i.e. field delimiter). Default: |
	 * 
	 * @since 0.1
	 */
	public char fieldSeparator;
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
	 * List of all fields in this segment.
	 */
	protected ArrayList<HL7Field> fields = new ArrayList<HL7Field>();

	/**
	 * Returns a string containing all delimiters defined for this message
	 * 
	 * @return a string containing all delimiters defined for this message:
	 *         fieldSeparator + componentSeparator + repetitionSeparator +
	 *         escapeCharacter + subcomponentSeparator
	 * 
	 * @since 0.1
	 */
	public String getDelimiters() {
		return "" + fieldSeparator + componentSeparator + repetitionSeparator
				+ escapeCharacter + subcomponentSeparator;
	}

	/**
	 * Sets all delimiters defined for this segment.
	 * 
	 * @param value
	 *            a string containing all delimiters defined for this segment:
	 *            fieldSeparator + componentSeparator + repetitionSeparator +
	 *            escapeCharacter + subcomponentSeparator
	 * 
	 * @since 0.1
	 */
	public void setDelimiters(String value) {
		fieldSeparator = value.charAt(0);
		componentSeparator = value.charAt(1);
		repetitionSeparator = value.charAt(2);
		escapeCharacter = value.charAt(3);
		subcomponentSeparator = value.charAt(4);
	}

	/**
	 * Removes all fields from this segment and adds the given fields to it.
	 * 
	 * @param fields
	 *            array of string representations of fields to be added.
	 * 
	 * @param delimiters
	 *            a string containing all delimiters defined for this segment:
	 *            fieldSeparator + componentSeparator + repetitionSeparator +
	 *            escapeCharacter + subcomponentSeparator
	 * 
	 * @param escapesInSubcomponents
	 *            whether escape characters are used or not
	 * 
	 * @since 0.1
	 */
	public void setFields(String[] fields, String delimiters,
			boolean escapesInSubcomponents) {
		this.fields.clear();
		for (String field : fields) {
			this.fields.add(HL7Parser.parseField(field, delimiters,
					escapesInSubcomponents));
			// TODO: Do not use escapesInSubcomponents for delimiters : MSH
			// segment.
		}
	}

	/**
	 * Removes all fields in this segment and adds the given fields.
	 * 
	 * @param fields
	 *            HL7 fields to add to this segment
	 * 
	 * @since 0.1
	 */
	public void setFields(HL7Field[] fields) {
		this.fields.clear();
		for (HL7Field field : fields) {
			this.fields.add(field);
		}
	}

	/**
	 * Returns a list containing all fields in this segment.
	 * 
	 * @return a list containing all fields in this segment
	 * 
	 * @since 0.1
	 */
	public ArrayList<?> getFields() {
		return fields;
	}

	/**
	 * Replaces the list of fields in this segment by the list provided as
	 * parameter.
	 * 
	 * @param fields
	 *            list of fields to be added in this segment.
	 * 
	 * @since 0.1
	 */
	public void setFields(ArrayList<HL7Field> fields) {
		this.fields = fields;
	}

	/**
	 * Returns the field at the given position.
	 * 
	 * @param index
	 *            position in the segment of the field to be returned
	 * 
	 * @return the field at the given position
	 * 
	 * @since 0.1
	 */
	public HL7Field get(int index) {
		if (index < fields.size()) {
			return fields.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns the number of fields in this segment.
	 * 
	 * @return the number of fields in this segment
	 * 
	 * @since 0.1
	 */
	public int getCount() {
		return fields.size();
	}

	/**
	 * Returns the type of this segment.
	 * 
	 * @return the type of this segment
	 * 
	 * @since 0.1
	 */
	public String getSegmentType() {
		return fields.get(0).toString();
	}

	/**
	 * Returns an enumerator through the fields in this segment.
	 * 
	 * @return an enumerator through the fields in this segment
	 * 
	 * @since 0.1
	 */
	public Enumeration<HL7Field> getEnumerator() {
		return Collections.enumeration(fields);
	}

	/**
	 * Returns the string representation of this segment.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @since 0.1
	 */
	@Override
	public String toString() {
		String s = null;
		for (HL7Field field : fields) {
			if (s == null) {
				s = field.toString();
			} else {
				s = s + fieldSeparator + field.toString();
			}
		}
		return s;
	}

	/**
	 * Returns the string representation of this segment where all escape
	 * sequences have been replaced by their values.
	 * 
	 * @return the string representation of this segment where all escape
	 *         sequences have been replaced by their values
	 * 
	 * @since 0.1
	 */
	public String getValue() {
		String s = null;
		for (HL7Field field : fields) {
			if (s == null) {
				s = field.getValue();
			} else {
				s = s + fieldSeparator + field.getValue();
			}
		}
		return s;
	}
}
