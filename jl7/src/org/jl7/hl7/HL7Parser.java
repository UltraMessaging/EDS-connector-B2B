package org.jl7.hl7;

import org.jl7.textutils.EscapedStringTokenizer;

/**
 * @author henribenoit
 * 
 */
public class HL7Parser {
	/**
	 * @param hl7String
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7Message parseMessage(String hl7String,
			boolean escapesInSubcomponents) {
		HL7Message message = new HL7Message();
		String delimiters = hl7String.substring(3, 5);
		message.setDelimiters(delimiters);
		EscapedStringTokenizer tokenizer = new EscapedStringTokenizer(
				hl7String, HL7Message.segmentTerminator,
				message.escapeCharacter);
		String[] segments = tokenizer.getTokens();
		for (String segment : segments) {
			message.addSegment(segment, delimiters, escapesInSubcomponents);
		}
		return message;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7Segment parseSegment(String hl7String, String delimiters,
			boolean escapesInSubcomponents) {
		HL7Segment segment = null;
		if (hl7String.startsWith("MSH")) {
			segment = new HL7MSHSegment();
		} else {
			segment = new HL7Segment();
		}
		segment.setDelimiters(delimiters);
		EscapedStringTokenizer tokenizer = new EscapedStringTokenizer(
				hl7String, segment.fieldSeparator, segment.escapeCharacter);
		String[] fields = tokenizer.getTokens();
		segment.setFields(fields, delimiters, escapesInSubcomponents);
		return segment;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7Field parseField(String hl7String, String delimiters,
			boolean escapesInSubcomponents) {
		HL7Field field = new HL7Field();
		field.setDelimiters(delimiters);
		EscapedStringTokenizer tokenizer = new EscapedStringTokenizer(
				hl7String, field.repetitionSeparator, field.escapeCharacter);
		String[] fields = tokenizer.getTokens();
		field.setRepetitions(fields, delimiters, escapesInSubcomponents);
		return field;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @return
	 */
	public static HL7Field parseMSHFirstField(String hl7String,
			String delimiters) {
		HL7Field field = new HL7Field();
		field.setDelimiters(delimiters);
		field.setRepetitionsWithoutDelimiters(hl7String);
		return field;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7FieldRepetition parseFieldRepetition(String hl7String,
			String delimiters, boolean escapesInSubcomponents) {
		HL7FieldRepetition repetition = new HL7FieldRepetition();
		repetition.setDelimiters(delimiters);
		EscapedStringTokenizer tokenizer = new EscapedStringTokenizer(
				hl7String, repetition.componentSeparator,
				repetition.escapeCharacter);
		String[] components = tokenizer.getTokens();
		repetition
				.setComponents(components, delimiters, escapesInSubcomponents);
		return repetition;
	}

	/**
	 * @param hl7String
	 * @return
	 */
	public static HL7FieldRepetition parseFieldRepetitionWithoutDelimiters(
			String hl7String) {
		HL7FieldRepetition repetition = new HL7FieldRepetition();
		repetition.setComponentsWithoutDelimiters(hl7String);
		return repetition;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7Component parseComponent(String hl7String,
			String delimiters, boolean escapesInSubcomponents) {
		HL7Component component = new HL7Component();
		component.setDelimiters(delimiters);
		EscapedStringTokenizer tokenizer = new EscapedStringTokenizer(
				hl7String, component.subcomponentSeparator,
				component.escapeCharacter);
		String[] subcomponents = tokenizer.getTokens();
		component.setSubcomponents(subcomponents, delimiters,
				escapesInSubcomponents);
		return component;
	}

	/**
	 * @param hl7String
	 * @return
	 */
	public static HL7Component parseComponentWithoutDelimiters(String hl7String) {
		HL7Component component = new HL7Component();
		component.setSubcomponentsWithoutDelimiters(hl7String);
		return component;
	}

	/**
	 * @param hl7String
	 * @param delimiters
	 * @param escapesInSubcomponents
	 * @return
	 */
	public static HL7Subcomponent parseSubcomponent(String hl7String,
			String delimiters, boolean escapesInSubcomponents) {
		HL7Subcomponent subcomponent = new HL7Subcomponent();
		if (escapesInSubcomponents) {
			subcomponent.setEscapedValue(hl7String, delimiters);
		} else {
			subcomponent.setValue(hl7String);
		}
		return subcomponent;
	}

	/**
	 * @param hl7String
	 * @return
	 */
	public static HL7Subcomponent parseSubcomponentWithoutDelimiters(
			String hl7String) {
		HL7Subcomponent subcomponent = new HL7Subcomponent();
		subcomponent.setValue(hl7String);
		return subcomponent;
	}
}
