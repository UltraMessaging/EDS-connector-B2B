package org.jl7.hl7proc;

import java.util.ArrayList;
import java.util.List;

import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Segment;
import org.jl7.hl7.HL7SegmentGroup;

public class HL7MessageSplitter {
	public static List<HL7SegmentGroup> GetPatients(HL7Message msg) {
		return msg.getSegmentGroups("PID|MRG");
	}

	public static List<HL7SegmentGroup> GetVisits(HL7Message msg) {
		List<HL7SegmentGroup> visits = null;
		List<HL7SegmentGroup> patients = GetPatients(msg);
		// Handle messages without PID segment
		if (patients.size() == 0) {
			patients = new ArrayList<HL7SegmentGroup>();
			patients.add(new HL7SegmentGroup(msg.getSegments()));
		}
		for (HL7SegmentGroup patient : patients) {
			List<HL7Segment> patientData = patient
					.getAllSegmentsBeforeTypeExceptHeader("PV1");
			visits = patient.getSegmentGroups("PV1");
			for (HL7SegmentGroup visit : visits) {
				visit.insertAtBeginning(patientData);
			}
		}
		return visits;
	}

	public static List<HL7SegmentGroup> GetProcedures(HL7Message msg) {
		List<HL7SegmentGroup> procedures = null;
		List<HL7SegmentGroup> visits = GetVisits(msg);
		// Handle messages without PV1 segment
		if (visits.size() == 0) {
			visits = GetPatients(msg);
			// Handle messages with neither PV1 nor PID segment
			if (visits.size() == 0) {
				visits = new ArrayList<HL7SegmentGroup>();
				visits.add(new HL7SegmentGroup(msg.getSegments()));
			}
		}
		for (HL7SegmentGroup visit : visits) {
			List<HL7Segment> visitData = visit
					.getAllSegmentsBeforeTypeExceptHeader("PR1");
			procedures = visit.getSegmentGroups("PR1");
			for (HL7SegmentGroup procedure : procedures) {
				// Remove guarantor and insurance info
				procedure.removeAllSegmentsAfter("GT1|IN1");
				// Added patient and visit info
				procedure.insertAtBeginning(visitData);
			}
		}
		return procedures;
	}

	public static List<HL7SegmentGroup> GetGuarantors(HL7Message msg) {
		List<HL7SegmentGroup> guarantors = null;
		List<HL7SegmentGroup> visits = GetVisits(msg);
		// Handle messages without PV1 segment
		if (visits.size() == 0) {
			visits = GetPatients(msg);
			// Handle messages with neither PV1 nor PID segment
			if (visits.size() == 0) {
				visits = new ArrayList<HL7SegmentGroup>();
				visits.add(new HL7SegmentGroup(msg.getSegments()));
			}
		}
		for (HL7SegmentGroup visit : visits) {
			HL7SegmentGroup visitData = new HL7SegmentGroup(visit
					.getAllSegmentsBeforeTypeExceptHeader("GT1"));
			guarantors = visit.getSegmentGroups("GT1");
			for (HL7SegmentGroup guarantor : guarantors) {
				// Remove procedure info
				visitData.remove("PR1|ROL");
				// Remove insurance info
				guarantor.removeAllSegmentsAfter("IN1");
				// Added patient and visit info
				guarantor.insertAtBeginning(visitData.getSegments());
			}
		}
		return guarantors;
	}

	public static List<HL7SegmentGroup> GetInsurances(HL7Message msg) {
		List<HL7SegmentGroup> insurances = null;
		List<HL7SegmentGroup> visits = GetVisits(msg);
		// Handle messages without PV1 segment
		if (visits.size() == 0) {
			visits = GetPatients(msg);
			// Handle messages with neither PV1 nor PID segment
			if (visits.size() == 0) {
				visits = new ArrayList<HL7SegmentGroup>();
				visits.add(new HL7SegmentGroup(msg.getSegments()));
			}
		}
		for (HL7SegmentGroup visit : visits) {
			HL7SegmentGroup visitData = new HL7SegmentGroup(visit
					.getAllSegmentsBeforeTypeExceptHeader("IN1"));
			insurances = visit.getSegmentGroups("IN1");
			for (HL7SegmentGroup insurance : insurances) {
				// Remove procedure and guarantor info
				visitData.remove("PR1|ROL|GT1");
				// Added patient and visit info
				insurance.insertAtBeginning(visitData.getSegments());
			}
		}
		return insurances;
	}

	public static List<HL7SegmentGroup> GetOrders(HL7Message msg) {
		List<HL7SegmentGroup> orders = null;
		List<HL7SegmentGroup> visits = GetVisits(msg);
		// Handle messages without PV1 segment
		if (visits.size() == 0) {
			visits = GetPatients(msg);
			// Handle messages with neither PV1 nor PID segment
			if (visits.size() == 0) {
				visits = new ArrayList<HL7SegmentGroup>();
				visits.add(new HL7SegmentGroup(msg.getSegments()));
			}
		}
		for (HL7SegmentGroup visit : visits) {
			List<HL7Segment> visitData = visit
					.getAllSegmentsBeforeTypeExceptHeader("ORC");
			orders = visit.getSegmentGroups("ORC");
			// Handle messages with OBR segments but no ORC segment
			if (orders.size() == 0) {
				visitData = visit.getAllSegmentsBeforeTypeExceptHeader("OBR");
				orders = visit.getSegmentGroups("OBR");
			}
			for (HL7SegmentGroup order : orders) {
				order.insertAtBeginning(visitData);
			}
		}
		return orders;
	}
}
