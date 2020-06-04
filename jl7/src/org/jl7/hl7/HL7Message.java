package org.jl7.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Represents an HL7 message
 * 
 * @since 0.1
 * 
 * @author henribenoit
 * 
 */
public class HL7Message {
	/**
	 * Character used to mark the end of a segment and the beginning of the next
	 * segment (i.e. segment delimiter). Default: ASCII 10
	 * 
	 * @since 0.1
	 */
	public static char segmentTerminator = 0x0D;
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
	 * List of all segments in this message.
	 */
	private List<HL7Segment> segments = new ArrayList<HL7Segment>();

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
	 * Sets all delimiters defined for this message.
	 * 
	 * @param value
	 *            a string containing all delimiters defined for this message:
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
	 * Returns the message type.
	 * 
	 * @return the message type
	 * 
	 * @since 0.1
	 */
	public String getMessageType() {
		return getHeader().getMessageType();
	}

	/**
	 * Returns the MSH segment of this message.
	 * 
	 * @return the MSH segment of this message
	 * 
	 * @since 0.1
	 */
	public HL7MSHSegment getHeader() {
		for (HL7Segment seg : segments) {
			if (seg instanceof HL7MSHSegment) {
				return (HL7MSHSegment) seg;
			}
		}
		return null;
	}

	/**
	 * Returns a list of segment groups where each group starts with the first
	 * segment of one of the given types and ends with the last segment before a
	 * segment of one of the given types e.g. if the message contains the
	 * following segments: MSH PID PV1 PV2 PID PV1 PID PV1 PV2, the following
	 * groups will be returned:
	 * <ul>
	 * <li>PID PV1 PV2</li>
	 * <li>PID PV1</li>
	 * <li>PID PV1 PV2</li>
	 * </ul>
	 * 
	 * @param segmentType
	 *            segment types (separated by pipes) to be searched for
	 * 
	 * @param segmentList
	 *            list of segments to be processed.
	 * 
	 * @return a list of segment groups
	 * 
	 * @since 0.1
	 */
	public List<HL7SegmentGroup> getSegmentGroups(String segmentTypes) {
		return HL7SegmentGroup.getSegmentGroups(segmentTypes, segments);
	}

	/**
	 * Adds a segment at the end of this message.
	 * 
	 * @param segment
	 *            string representing the segment to be added
	 * 
	 * @param delimiters
	 *            a string containing all delimiters defined for this message:
	 *            fieldSeparator + componentSeparator + repetitionSeparator +
	 *            escapeCharacter + subcomponentSeparator
	 * 
	 * @param escapesInSubcomponents
	 *            whether escape characters are used or not
	 * 
	 * @since 0.1
	 */
	public void addSegment(String segment, String delimiters,
			boolean escapesInSubcomponents) {
		segments.add(HL7Parser.parseSegment(segment, delimiters,
				escapesInSubcomponents));
	}

	/**
	 * Adds a segment at the end of the message.
	 * 
	 * @param message
	 *            message to which the segment should be added
	 * 
	 * @param segment
	 *            segment to be added
	 * 
	 * @return the input message (with the additional segment)
	 * 
	 * @since 0.1
	 */
	public static HL7Message addSegment(HL7Message message, HL7Segment segment) {
		message.getSegments().add(segment);
		return message;
	}

	/**
	 * Returns the number of segment in this message.
	 * 
	 * @return the number of segment in this message
	 * 
	 * @since 0.1
	 */
	public int getCount() {
		return segments.size();
	}

	/**
	 * Returns the list of segments in this message
	 * 
	 * @return the list of segments in this message
	 * 
	 * @since 0.1
	 */
	public List<HL7Segment> getSegments() {
		return segments;
	}

	/**
	 * Returns the segment at the specified position in this message.
	 * 
	 * @param index
	 *            position of the segment to be returned in this message
	 * 
	 * @return the segment at the specified position in this message.
	 * 
	 * @since 0.1
	 */
	public HL7Segment get(int index) {
		if (index < segments.size()) {
			return (HL7Segment) segments.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns a list containing all segments in this message of the specified
	 * types.
	 * 
	 * @param segmentTypes
	 *            pipe separated list of segment types to be returned
	 * 
	 * @return a list containing all segments in this message of the specified
	 *         types
	 * 
	 * @since 0.1
	 */
	public List<HL7Segment> get(String segmentTypes) {
		return HL7SegmentGroup.GetSegments(segmentTypes, segments);
	}

	/**
	 * Returns the string representation of this message.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @since 0.1
	 */
	@Override
	public String toString() {
		String s = null;
		for (HL7Segment seg : segments) {
			if (s == null) {
				s = seg.toString();
			} else {
				s = s + segmentTerminator + seg.toString();
			}
		}
		return s;
	}

	/**
	 * Returns the string representation of this message where all escape
	 * sequences have been replaced by their values.
	 * 
	 * @return the string representation of this message where all escape
	 *         sequences have been replaced by their values
	 * 
	 * @since 0.1
	 */
	public String getValue() {
		String s = null;
		for (HL7Segment seg : segments) {
			if (s == null) {
				s = seg.getValue();
			} else {
				s = s + segmentTerminator + seg.getValue();
			}
		}
		return s;
	}

	/**
	 * Returns a string representing the segment structure of this message.
	 * 
	 * @return a string representing the segment structure of this message.
	 * 
	 * @since 0.1
	 */
	public String getStructure() {
		String s = "";
		for (HL7Segment segment : segments) {
			if (!s.equals("")) {
				s += " ";
			}
			s += segment.getSegmentType();
		}
		return s;
	}

	/**
	 * Returns an enumerator through the segments in this message.
	 * 
	 * @return an enumerator through the segments in this message
	 * 
	 * @since 0.1
	 */
	public Enumeration<HL7Segment> getEnumerator() {
		return Collections.enumeration(segments);
	}
}
