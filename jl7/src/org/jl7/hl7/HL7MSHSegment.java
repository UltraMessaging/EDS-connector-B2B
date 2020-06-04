package org.jl7.hl7;

public class HL7MSHSegment extends HL7Segment {
	@Override
	public void setFields(String[] fields, String delimiters,
			boolean escapesInSubcomponents) {
		this.fields.clear();
		boolean first = true;
		boolean second = false;
		for (String field : fields) {
			if (first) {
				this.fields.add(HL7Parser.parseField(field, delimiters,
						escapesInSubcomponents));
				first = false;
				second = true;
			} else if (second) {
				// Do not interpret delimiters for the first MSH segment (MSH-2
				// in the standard)
				this.fields
						.add(HL7Parser.parseMSHFirstField(field, delimiters));
				second = false;
			} else {
				this.fields.add(HL7Parser.parseField(field, delimiters,
						escapesInSubcomponents));
			}
		}
	}

	public String getSendingApplication() {
		return get(2).toString();
	}

	public String getSendingFacility() {
		return get(3).toString();
	}

	public String getReceivingApplication() {
		return get(4).toString();
	}

	public String getReceivingFacility() {
		return get(5).toString();
	}

	public String getMessageDateTime() {
		return get(6).toString();
	}

	public String getMessageType() {
		return get(8).toString();
	}

	public String getMessageControlId() {
		return get(9).toString();
	}

	public String getProcessingId() {
		return get(10).toString();
	}

	public String getGetVersionId() {
		return get(11).toString();
	}

	public int getSequenceNumber() {
		return Integer.parseInt(get(12).toString());
	}

	public String getContinuationPointer() {
		return get(13).toString();
	}

	public String getAcceptAcknowledgementType() {
		return get(14).toString();
	}

	public String getApplicationAcknowledgementType() {
		return get(15).toString();
	}

	public String getCountryCode() {
		return get(16).toString();
	}

	public String getCharacterSet() {
		return get(17).toString();
	}

	public String getPrincipalLanguage() {
		return get(18).toString();
	}
}
