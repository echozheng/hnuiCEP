package cn.edu.hnu.icep.workload.history_generation;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.event.model.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class FileImporter {

	private final Map<String, InputEvent> events = new HashMap<String, InputEvent>();
	private final String fileName;
	private final String delimiter;
	private final int correctNumColumns;
	private final long maxNumLines;
	private final List<String> unacceptableValues = new ArrayList<String>();

	public FileImporter(String fileName, String delimiter,int correctNumColumns, long maxNumLines) {
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.correctNumColumns = correctNumColumns;
		this.maxNumLines = maxNumLines;
	}
	
	public void addUnacceptableValue(String unacceptableValue) {
		unacceptableValues.add(unacceptableValue);
	}

	public void addEvent(String eventType, int timestampColumn) {
		assert (!events.containsKey(eventType));
		InputEvent ev = new InputEvent(timestampColumn - 1);
		events.put(eventType, ev);
	}
	
	public void addAttribute(String eventType, String attrName, int attrColumn) {
		assert (events.containsKey(eventType));
		assert (attrColumn > 0);
		InputEvent ev = events.get(eventType);
		ev.addAttribute(attrName, attrColumn - 1);
	}
	
	public void addAttributeToType(String eventType, int attrColumn) {
		assert (events.containsKey(eventType));
		assert (attrColumn > 0);
		InputEvent ev = events.get(eventType);
		ev.addNameAttribute(attrColumn - 1);
	}
	
	public void decorateHistory(History history) {
		Set<Event> generatedEvents = new HashSet<Event>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
			String line = null;
			long numLines = 0;
			while ((line = reader.readLine()) != null) {
				List<String> columns = extractColumns(line);
				columns.removeAll(unacceptableValues);
				if (correctNumColumns != 0 && columns.size() != correctNumColumns) {
					continue;
				}
				generateEventsFromColumns(columns, generatedEvents);
				if (maxNumLines > 0 && ++numLines >= maxNumLines) {
					break;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Event event : generatedEvents) {
			history.addPrimitiveEvent(event);
		}
	}
	
	private List<String> extractColumns(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
		List<String> result = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			result.add(token);
		}
		return result;
	}
	
	private void generateEventsFromColumns(List<String> columns,Set<Event> generatedEvents) {
		for (String type : events.keySet()) {
			InputEvent inputEvent = events.get(type);
			long timestamp = getTimestamp(columns,inputEvent.getTimestampColumn());
			Set<Attribute> attributes = createAttributesFrom(inputEvent,columns);
			String eventType = type;
			
			for (Integer attrColumn : inputEvent.getAttributesToType()) {
				String attr = columns.get(attrColumn);
				eventType += ("_" + attr);
			}
			generatedEvents.add(new Event(eventType, timestamp, attributes));
		}
	}
	
	private long getTimestamp(List<String> columns, int timestampColumn) {
		return Long.parseLong(columns.get(timestampColumn));
	}

	private Set<Attribute> createAttributesFrom(InputEvent inputEvent,
			List<String> columns) {
		Set<Attribute> attributes = new HashSet<Attribute>();
		Map<String, Integer> attrColumns = inputEvent.getAttributesColumns();
		for (String attrName : attrColumns.keySet()) {
			int column = attrColumns.get(attrName);
			assert (column < columns.size());
			String columnVal = columns.get(column);
			Value value = generateValueFrom(columnVal);
			attributes.add(new Attribute(attrName, value));
		}
		return attributes;
	}
	
	private Value generateValueFrom(String columnVal) {
		try {
			long longVal = Long.parseLong(columnVal);
			return new Value(longVal);
		} catch (NumberFormatException e1) {
			try {
				double doubleVal = Double.parseDouble(columnVal);
				return new Value(doubleVal);
			} catch (NumberFormatException e2) {
				return new Value(columnVal);
			}
		}
	}

	private class InputEvent {
		private final int timestampColumn;
		private final Map<String, Integer> attributes = new HashMap<String, Integer>();
		private final List<Integer> attributesToType = new ArrayList<Integer>();

		InputEvent(int timestampColumn) {
			this.timestampColumn = timestampColumn;
		}

		void addAttribute(String attrName, int column) {
			attributes.put(attrName, column);
		}

		void addNameAttribute(int column) {
			attributesToType.add(column);
		}

		int getTimestampColumn() {
			return timestampColumn;
		}

		Map<String, Integer> getAttributesColumns() {
			return attributes;
		}

		List<Integer> getAttributesToType() {
			return attributesToType;
		}
	}
}
