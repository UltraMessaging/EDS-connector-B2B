/**
 * 
 */
package org.jl7.test;

import junit.framework.TestCase;

import org.jl7.hl7.HL7Component;
import org.jl7.hl7.HL7FieldRepetition;
import org.jl7.hl7.HL7Message;

/**
 * @author henribenoit
 * 
 */
public class TestHL7FieldRepetition extends TestCase {

	private HL7FieldRepetition repetition;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		repetition = new HL7FieldRepetition();
		repetition.setDelimiters("|^~\\&");
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
	 * {@link org.jl7.hl7.HL7FieldRepetition#setDelimiters(java.lang.String)}.
	 */
	public void testSetDelimiters() {
		assertEquals(repetition.escapeCharacter, '\\');
		assertEquals(repetition.subcomponentSeparator, '&');
		assertEquals(repetition.componentSeparator, '^');
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7FieldRepetition#setComponents(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetComponentsEscapes() {
		String[] components = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		repetition.setComponents(components, "|^~\\&", true);
		String value = repetition.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "^\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7FieldRepetition#setComponents(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetComponentsNoEscapes() {
		String[] components = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		repetition.setComponents(components, "|^~\\&", false);
		String value = repetition.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "^\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7FieldRepetition#setComponentsWithoutDelimiters(java.lang.String)}
	 * .
	 */
	public void testSetComponentsWithoutDelimiters() {
		String hl7String = "abc" + HL7Message.segmentTerminator + "\\|~^&def";
		repetition.setComponentsWithoutDelimiters(hl7String);
		String value = repetition.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "\\|~^&def", value);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7FieldRepetition#get(int)}.
	 */
	public void testGet() {
		String[] components = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		repetition.setComponents(components, "|^~\\&", false);
		HL7Component component = repetition.get(0);
		assertEquals("abc" + HL7Message.segmentTerminator, component.getValue());
		component = repetition.get(1);
		assertEquals("\\|~^&def", component.getValue());
		component = repetition.get(2);
		assertNull(component);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7FieldRepetition#toString()}.
	 */
	public void testToString() {
		String[] components = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		repetition.setComponents(components, "|^~\\&", true);
		String value = repetition.toString();
		assertEquals("abc\\" + HL7Message.segmentTerminator
				+ "^\\E\\\\F\\\\R\\\\S\\\\T\\def", value);
	}
}
