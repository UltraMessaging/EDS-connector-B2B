package org.jl7.hl7proc;

import java.util.List;

import org.jl7.hl7.HL7Component;
import org.jl7.hl7.HL7Field;
import org.jl7.hl7.HL7FieldRepetition;
import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Segment;
import org.jl7.hl7.HL7SegmentGroup;
import org.jl7.hl7.HL7Subcomponent;

public class Hl7MessageExtractor {
	public static String ExtractString(HL7SegmentGroup group, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, group, null, null, null, null, null, null);
	}

	public static String ExtractString(HL7Message msg, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, null, msg, null, null, null, null, null);
	}

	public static String ExtractString(HL7Segment msg, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, null, null, msg, null, null, null, null);
	}

	public static String ExtractString(HL7Field msg, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, null, null, null, msg, null, null, null);
	}

	public static String ExtractString(HL7FieldRepetition msg, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, null, null, null, null, msg, null, null);
	}

	public static String ExtractString(HL7Component msg, String locator) {
		String[] strings = new org.jl7.textutils.EscapedStringTokenizer(
				locator, "|~^&", '\\', true).getTokens();
		return ExtractString(strings, null, null, null, null, null, msg, null);
	}

	private static String ExtractString(String[] strings,
			HL7SegmentGroup group, HL7Message msg, HL7Segment segment,
			HL7Field field, HL7FieldRepetition repetition,
			HL7Component component, HL7Subcomponent subcomponent) {
		for (String s : strings) {
			if (s.startsWith("|")) {
				field = segment.get(Integer.parseInt(s.substring(1)));
			} else if (s.startsWith("~")) {
				repetition = field.get(Integer.parseInt(s.substring(1)) - 1);
			} else if (s.startsWith("^")) {
				if (repetition == null) {
					repetition = field.get(0);
				}
				component = repetition
						.get(Integer.parseInt(s.substring(1)) - 1);
			} else if (s.startsWith("&")) {
				subcomponent = component
						.get(Integer.parseInt(s.substring(1)) - 1);
			} else if (msg != null) {
				List<HL7Segment> segments = msg.get(s);
				segment = segments.get(0);
			} else if (group != null) {
				List<HL7Segment> segments = group.get(s);
				segment = segments.get(0);
			}
		}
		if (subcomponent != null) {
			return subcomponent.getValue();
		} else if (component != null) {
			return component.getValue();
		} else if (repetition != null) {
			return repetition.getValue();
		} else if (field != null) {
			return field.getValue();
		} else if (segment != null) {
			return segment.getValue();
		} else if (msg != null) {
			return msg.getValue();
		}
		return null;
	}
}
