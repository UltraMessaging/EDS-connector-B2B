/**
 * 
 */
package org.jl7.test;

import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Subcomponent;

import junit.framework.TestCase;

/**
 * @author henribenoit
 * 
 */
public class TestHL7Subcomponent extends TestCase {

	private HL7Subcomponent subcomponent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		subcomponent = new HL7Subcomponent();
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
	 * {@link org.jl7.hl7.HL7Subcomponent#setValue(java.lang.String)}.
	 */
	public void testSetValue() {
		subcomponent.setValue("abc");
		String value = subcomponent.getValue();
		assertEquals("abc", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueSegmentTerminator() {
		String value = "\\" + HL7Message.segmentTerminator;
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals(""+HL7Message.segmentTerminator, value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueFieldSeparator() {
		String value = "\\|";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("|", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueComponentSeparator() {
		String value = "\\^";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("^", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueRepetitionSeparator() {
		String value = "\\~";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("~", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueSubcomponentSeparator() {
		String value = "\\&";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("&", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueEscapeCharacter() {
		String value = "\\E\\";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("\\", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueFieldSeparator2() {
		String value = "\\F\\";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("|", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueComponentSeparator2() {
		String value = "\\S\\";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("^", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueRepetitionSeparator2() {
		String value = "\\R\\";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("~", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Subcomponent#setEscapedValue(java.lang.String, java.lang.String)}
	 */
	public void testSetEscapedValueSubcomponentSeparator2() {
		String value = "\\T\\";
		subcomponent.setEscapedValue(value, "|^~\\&");
		value = subcomponent.getValue();
		assertEquals("&", value);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7Subcomponent#toString()}.
	 */
	public void testToString() {
		subcomponent.setValue("abc"+HL7Message.segmentTerminator+"\\|~^&def");
		String value = subcomponent.toString();
		assertEquals("abc\\"+HL7Message.segmentTerminator+"\\E\\\\F\\\\R\\\\S\\\\T\\def", value);
	}

}
