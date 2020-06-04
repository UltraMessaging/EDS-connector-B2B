package org.jl7.hl7;

import java.util.ArrayList;

/**
 * Represents an HL7 field repetition
 * 
 * @since 0.1
 * 
 * @author henribenoit
 * 
 */
public class HL7FieldRepetition {
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
	 * Character used to escape characters which would otherwise be interpreted.
	 * Default: \
	 * 
	 * @since 0.1
	 */
	public char escapeCharacter;
	/**
	 * List of all components in this field repetition.
	 */
	protected ArrayList<HL7Component> components = new ArrayList<HL7Component>();

	/**
	 * Sets all delimiters defined for this field repetition.
	 * 
	 * @param value
	 *            a string containing all delimiters defined for this field
	 *            repetition: fieldSeparator (not used in this class) +
	 *            componentSeparator (not used in this class) +
	 *            repetitionSeparator + escapeCharacter + subcomponentSeparator
	 * 
	 * @since 0.1
	 */
	public void setDelimiters(String value) {
		componentSeparator = value.charAt(1);
		escapeCharacter = value.charAt(3);
		subcomponentSeparator = value.charAt(4);
	}

	/**
	 * Removes all components from this field repetition and adds the given components to
	 * it.
	 * 
	 * @param fields
	 *            array of string representations of components to be added.
	 * 
	 * @param delimiters
	 *            a string containing all delimiters defined for this field
	 *            repetition: fieldSeparator + componentSeparator +
	 *            repetitionSeparator + escapeCharacter + subcomponentSeparator
	 * 
	 * @param escapesInSubcomponents
	 *            whether escape characters are used or not
	 * 
	 * @since 0.1
	 */
	public void setComponents(String[] components, String delimiters,
			boolean escapesInSubcomponents) {
		for (String component : components) {
			this.components.add(HL7Parser.parseComponent(component, delimiters,
					escapesInSubcomponents));
		}
	}

	/**
	 * Parses the string representation of an HL7 field repetition and sets the
	 * components in this field.
	 * 
	 * @param hl7String
	 *            string representation of an HL7 field representation
	 */
	public void setComponentsWithoutDelimiters(String hl7String) {
		this.components.add(HL7Parser
				.parseComponentWithoutDelimiters(hl7String));
	}

	/**
	 * Returns the component at the given position.
	 * 
	 * @param index
	 *            position in the field repetition of the component to be returned
	 * 
	 * @return the component at the given position
	 * 
	 * @since 0.1
	 */
	public HL7Component get(int index) {
		if (index < components.size()) {
			return (HL7Component) components.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns the string representation of this field repetition.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @since 0.1
	 */
	@Override
	public String toString() {
		String s = null;
		for (HL7Component comp : components) {
			if (s == null) {
				s = comp.toString();
			} else {
				s = s + componentSeparator + comp.toString();
			}
		}
		return s;
	}

	/**
	 * Returns the string representation of this field repetition where all
	 * escape sequences have been replaced by their values.
	 * 
	 * @return the string representation of this field repetition where all
	 *         escape sequences have been replaced by their values
	 * 
	 * @since 0.1
	 */
	public String getValue() {
		String s = null;
		for (HL7Component comp : components) {
			if (s == null) {
				s = comp.getValue();
			} else {
				s = s + componentSeparator + comp.getValue();
			}
		}
		return s;
	}
}
