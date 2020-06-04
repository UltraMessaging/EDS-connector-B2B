package org.jl7.hl7;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a group of HL7 segments (typically returned by processing
 * classes).
 * 
 * @since 0.1
 * 
 * @author henribenoit
 */
public class HL7SegmentGroup implements Iterable<HL7Segment> {
	/**
	 * List of segments in this segment group.
	 */
	private List<HL7Segment> segments = new ArrayList<HL7Segment>();

	/**
	 * Creates an empty segment group.
	 * 
	 * @since 0.1
	 */
	public HL7SegmentGroup() {
	}

	/**
	 * Creates a segment group containing all segment in the provided list.
	 * 
	 * @param newSegments
	 *            list of segment to take over in the created segment group
	 * 
	 * @since 0.1
	 */
	public HL7SegmentGroup(List<HL7Segment> newSegments) {
		for (HL7Segment segment : newSegments) {
			if (segment instanceof HL7MSHSegment) {
				// Ignore header segments
				continue;
			} else {
				segments.add(segment);
			}
		}
	}

	/**
	 * Adds the provided segment at the end of the segment group and returns
	 * itself.
	 * 
	 * @param segment
	 * 
	 * @return the segment group itself
	 * 
	 * @since 0.1
	 */
	public HL7SegmentGroup add(HL7Segment segment) {
		getSegments().add(segment);
		return this;
	}

	/**
	 * Removes the provided segment from the segment group and returns itself.
	 * 
	 * @param segment
	 *            segment to be removed
	 * 
	 * @return the segment group itself
	 * 
	 * @since 0.1
	 */
	public HL7SegmentGroup remove(HL7Segment segment) {
		getSegments().remove(segment);
		return this;
	}

	/**
	 * Removes all segments of the given types from the segment group.
	 * 
	 * @param types
	 *            segment types to be removed from the list (separated by pipes)
	 * 
	 * @return the segment group itself
	 * 
	 * @since 0.1
	 */
	public HL7SegmentGroup remove(String types) {
		String[] segmentTypes = types.split("|");
		for (HL7Segment segment : getSegments()) {
			for (String segmentType : segmentTypes) {
				if (segment.getSegmentType().equals(segmentType)) {
					getSegments().remove(segment);
					break;
				}
			}
		}
		return this;
	}

	/**
	 * Removes all segments occurring after any segment of the given types from
	 * the segment group.
	 * 
	 * @param types
	 *            segment types (separated by pipes) to be searched for
	 * 
	 * @since 0.1
	 */
	public void removeAllSegmentsAfter(String types) {
		String[] segmentTypes = types.split("|");
		boolean found = false;
		for (HL7Segment segment : segments) {
			if (!found) {
				for (String segmentType : segmentTypes) {
					if (segment.getSegmentType().equals(segmentType)) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				getSegments().remove(segment);
			}
		}
	}

	/**
	 * Returns a list of all segments in the segment group located before the
	 * first occurence of any segment of one of the given types.
	 * 
	 * @param segmentType
	 *            segment types (separated by pipes) to be searched for
	 * 
	 * @return list of all segments in the segment group located before the
	 *         first occurence of any segment of one of the given types.
	 * 
	 * @since 0.1
	 */
	public List<HL7Segment> getAllSegmentsBeforeTypeExceptHeader(
			String segmentType) {
		List<HL7Segment> segmentsBefore = new ArrayList<HL7Segment>();
		for (HL7Segment seg : segments) {
			if (seg instanceof HL7MSHSegment) {
				// Ignore Header
				continue;
			} else if (seg.getSegmentType().equals(segmentType)) {
				break;
			}
			segmentsBefore.add(seg);
		}
		return segmentsBefore;
	}

	/**
	 * Inserts a list of segments at the beginning of the segment group.
	 * 
	 * @param segmentsToInsert
	 *            list of segments to insert
	 * 
	 * @since 0.1
	 */
	public void insertAtBeginning(List<HL7Segment> segmentsToInsert) {
		segments.addAll(0, segmentsToInsert);
	}

	/**
	 * Returns a list of segment groups where each group starts with the first
	 * segment of one of the given types and ends with the last segment before a
	 * segment of one of the given types e.g. if the segment group contains the
	 * following segments: PID PV1 PV2 PID PV1 PID PV1 PV2, the following groups
	 * will be returned:
	 * <ul>
	 * <li>PID PV1 PV2</li>
	 * <li>PID PV1</li>
	 * <li>PID PV1 PV2</li>
	 * <ul>
	 * 
	 * @param segmentType
	 *            segment types (separated by pipes) to be searched for
	 * 
	 * @return a list of segment groups
	 * 
	 * @since 0.1
	 */
	public List<HL7SegmentGroup> getSegmentGroups(String segmentType) {
		return getSegmentGroups(segmentType, segments);
	}

	/**
	 * Returns a list of segment groups where each group starts with the first
	 * segment of one of the given types and ends with the last segment before a
	 * segment of one of the given types e.g. if the segment group contains the
	 * following segments: PID PV1 PV2 PID PV1 PID PV1 PV2, the following groups
	 * will be returned:
	 * <ul>
	 * <li>PID PV1 PV2</li>
	 * <li>PID PV1</li>
	 * <li>PID PV1 PV2</li>
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
	public static List<HL7SegmentGroup> getSegmentGroups(String segmentType,
			List<HL7Segment> segmentList) {
		String[] segmentTypes = segmentType.split("|");
		List<HL7SegmentGroup> segmentGroups = new ArrayList<HL7SegmentGroup>();
		HL7SegmentGroup segmentGroup = null;
		for (HL7Segment seg : segmentList) {
			if (seg instanceof HL7MSHSegment) {
				// Ignore Header
				continue;
			} else {
				for (String type : segmentTypes) {
					if (seg.getSegmentType().equals(type)) {
						if (segmentGroup != null) {
							segmentGroups.add(segmentGroup);
						}
						segmentGroup = new HL7SegmentGroup();
						break;
					}
				}
			}
			if (segmentGroup != null) {
				segmentGroup.add(seg);
			}
		}
		if (segmentGroup != null) {
			segmentGroups.add(segmentGroup);
		}
		return segmentGroups;
	}

	/**
	 * Returns the number of segment in this segment group
	 * 
	 * @return the number of segment in this segment group
	 * 
	 * @since 0.1
	 */
	public int getCount() {
		return segments.size();
	}

	/**
	 * Returns the list of segments in this segment group
	 * 
	 * @return the list of segments in this segment group
	 * 
	 * @since 0.1
	 */
	public List<HL7Segment> getSegments() {
		return segments;
	}

	/**
	 * Returns the segment at the given position in this segment group
	 * 
	 * @param index
	 *            index of the segment to be returned
	 * 
	 * @return the segment at the given position in this segment group
	 * 
	 * @since 0.1
	 */
	public HL7Segment get(int index) {
		if (index < segments.size()) {
			return segments.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns a list of all segments of the given type
	 * 
	 * @param segmentType
	 *            type of segments to be returned.
	 * 
	 * @return a list of all segments of the given type
	 * 
	 * @since 0.1
	 */
	public List<HL7Segment> get(String segmentType) {
		return GetSegments(segmentType, segments);
	}

	/**
	 * Returns a list of all segments of the given type
	 * 
	 * @param segmentType
	 *            type of segments to be returned.
	 * 
	 * @param segmentList
	 *            list of segments to be processed
	 * 
	 * @return a list of all segments of the given type
	 * 
	 * @since 0.1
	 */
	public static List<HL7Segment> GetSegments(String segmentType,
			List<HL7Segment> segmentList) {
		List<HL7Segment> filteredSegments = new ArrayList<HL7Segment>();
		for (HL7Segment segment : segmentList) {
			if (segment.getSegmentType().equals(segmentType)) {
				filteredSegments.add(segment);
			}
		}
		return filteredSegments;
	}

	/**
	 * Returns a newline separated string representing all segments in this
	 * segment group.
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
				s = s + "\n" + seg.toString();
			}
		}
		return s;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 * 
	 * @since 0.1
	 */
	public Iterator<HL7Segment> iterator() {
		return segments.iterator();
	}
}
