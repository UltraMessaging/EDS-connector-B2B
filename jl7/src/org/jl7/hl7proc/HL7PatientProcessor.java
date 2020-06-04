package org.jl7.hl7proc;

import org.jl7.hl7.HL7Field;
import org.jl7.hl7.HL7Segment;
import org.jl7.hl7.HL7SegmentGroup;

public class HL7PatientProcessor {
	public static HL7Field GetPatientID(HL7SegmentGroup segs) {
		for (HL7Segment seg : segs) {
			HL7Field value = GetPatientID(seg);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public static HL7Field GetPatientID(HL7Segment seg) {
		if (seg.getSegmentType().equals("PID")) {
			return seg.get(3);
		} else if (seg.getSegmentType().equals("MRG")) {
			return seg.get(1);
		}
		return null;
	}

	public static HL7Field GetPatientName(HL7SegmentGroup segs) {
		for (HL7Segment seg : segs) {
			HL7Field value = GetPatientName(seg);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public static HL7Field GetPatientName(HL7Segment seg) {
		if (seg.getSegmentType().equals("PID")) {
			return seg.get(5);
		} else if (seg.getSegmentType().equals("MRG")) {
			return seg.get(7);
		}
		return null;
	}

	public static HL7Field GetAlternatePatientID(HL7SegmentGroup segs) {
		for (HL7Segment seg : segs) {
			HL7Field value = GetAlternatePatientID(seg);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public static HL7Field GetAlternatePatientID(HL7Segment seg) {
		if (seg.getSegmentType().equals("PID")) {
			return seg.get(4);
		} else if (seg.getSegmentType().equals("MRG")) {
			return seg.get(2);
		}
		return null;
	}

	public static HL7Field getPatientAccountNumber(HL7SegmentGroup segs) {
		for (HL7Segment seg : segs) {
			HL7Field value = getPatientAccountNumber(seg);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public static HL7Field getPatientAccountNumber(HL7Segment seg) {
		if (seg.getSegmentType().equals("PID")) {
			return seg.get(18);
		} else if (seg.getSegmentType().equals("MRG")) {
			return seg.get(3);
		}
		return null;
	}

	public static HL7Field getPatientIDExternal(HL7SegmentGroup segs) {
		for (HL7Segment seg : segs) {
			HL7Field value = getPatientIDExternal(seg);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public static HL7Field getPatientIDExternal(HL7Segment seg) {
		if (seg.getSegmentType().equals("PID")) {
			return seg.get(2);
		} else if (seg.getSegmentType().equals("MRG")) {
			return seg.get(4);
		}
		return null;
	}
}
