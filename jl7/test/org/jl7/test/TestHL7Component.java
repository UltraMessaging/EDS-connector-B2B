/**
 * 
 */
package org.jl7.test;

import org.jl7.hl7.HL7Component;
import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Subcomponent;

import junit.framework.TestCase;

/**
 * @author henribenoit
 * 
 */
public class TestHL7Component extends TestCase {

	private HL7Component component;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		component = new HL7Component();
		component.setDelimiters("|^~\\&");
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
	 * {@link org.jl7.hl7.HL7Component#setDelimiters(java.lang.String)}.
	 */
	public void testSetDelimiters() {
		assertEquals(component.escapeCharacter, '\\');
		assertEquals(component.subcomponentSeparator, '&');
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Component#setSubcomponents(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetSubcomponentsEscapes() {
		String[] subComponents = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		component.setSubcomponents(subComponents, "|^~\\&", true);
		String value = component.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "&\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Component#setSubcomponents(java.lang.String[], java.lang.String, boolean)}
	 * .
	 */
	public void testSetSubcomponentsNoEscapes() {
		String[] subComponents = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		component.setSubcomponents(subComponents, "|^~\\&", false);
		String value = component.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "&\\|~^&def", value);
	}

	/**
	 * Test method for
	 * {@link org.jl7.hl7.HL7Component#setSubcomponentsWithoutDelimiters(java.lang.String)}
	 * .
	 */
	public void testSetSubcomponentsWithoutDelimiters() {
		String hl7String = "abc" + HL7Message.segmentTerminator + "\\|~^&def";
		component.setSubcomponentsWithoutDelimiters(hl7String);
		String value = component.getValue();
		assertEquals("abc" + HL7Message.segmentTerminator + "\\|~^&def", value);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7Component#get(int)}.
	 */
	public void testGet() {
		String[] subComponents = { "abc" + HL7Message.segmentTerminator,
				"\\|~^&def" };
		component.setSubcomponents(subComponents, "|^~\\&", false);
		HL7Subcomponent subcomponent = component.get(0);
		assertEquals("abc" + HL7Message.segmentTerminator, subcomponent
				.getValue());
		subcomponent = component.get(1);
		assertEquals("\\|~^&def", subcomponent.getValue());
		subcomponent = component.get(2);
		assertNull(subcomponent);
	}

	/**
	 * Test method for {@link org.jl7.hl7.HL7Component#toString()}.
	 */
	public void testToString() {
		String[] subComponents = { "abc\\" + HL7Message.segmentTerminator,
				"\\E\\\\F\\\\R\\\\S\\\\T\\def" };
		component.setSubcomponents(subComponents, "|^~\\&", true);
		String value = component.toString();
		assertEquals("abc\\" + HL7Message.segmentTerminator
				+ "&\\E\\\\F\\\\R\\\\S\\\\T\\def", value);
	}
}
