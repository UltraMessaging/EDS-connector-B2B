/**
 * 
 */
package org.jl7.test;

import junit.framework.TestCase;

import org.jl7.hl7.HL7Field;
import org.jl7.hl7.HL7FieldRepetition;
import org.jl7.hl7.HL7Message;

/**
 * @author henribenoit
 * 
 */
public class TestHL7Field extends TestCase {

	private HL7Field field;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		field = new HL7Field();
		field.setDelimiters("|^~\\&");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Field#setDelimiters(java.lang.String)}.
	 */
	public void testSetDelimiters() {
		assertEquals(field.escapeCharacter, '\\');
		assertEquals(field.subcomponentSeparator, '&');
		assertEquals(field.componentSeparator, '^');
		assertEquals(field.repetitionSeparator, '~');
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Field#setRepetitions(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetRepetitionsEscapes() {
		String[] repetitions = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		field.setRepetitions(repetitions, "|^~\\&", true);
		String value = field.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "~\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Field#setRepetitions(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetRepetitionsNoEscapes() {
		String[] repetitions = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		field.setRepetitions(repetitions, "|^~\\&", false);
		String value = field.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "~\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Field#setRepetitionsWithoutDelimiters(java.lang.String)}
	 * .
	 */
	public void testSetRepetitionsWithoutDelimiters() {
		String hl7String = "abc" + HL7Message.segmentTerminator + "\\|~^&def";
		field.setRepetitionsWithoutDelimiters(hl7String);
		String value = field.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "\\|~^&def", value);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7Field#get(int)}.
	 */
	public void testGet() {
		String[] repetitions = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		field.setRepetitions(repetitions, "|^~\\&", false);
		HL7FieldRepetition repetition = field.get(0);
		assertEquals("abc" + HL7Message.segmentTerminator, repetition
				.getValue());
		repetition = field.get(1);
		assertEquals("\\|~^&def", repetition.getValue());
		repetition = field.get(2);
		assertNull(repetition);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7Field#toString()}.
	 */
	public void testToString() {
		String[] repetitions = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		field.setRepetitions(repetitions, "|^~\\&", true);
		String value = field.toString();
		assertEquals("abc\\" + HL7Message.segmentTerminator
				+ "~\\E\\\\F\\\\R\\\\S\\\\T\\def", value);
	}
}
