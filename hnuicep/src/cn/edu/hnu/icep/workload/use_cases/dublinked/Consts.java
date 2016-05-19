package cn.edu.hnu.icep.workload.use_cases.dublinked;

import java.util.HashMap;
import java.util.Map;

public class Consts {
	private static final Map<String, Integer> dictionary;

	static final int numFields = 15;

	static final String timestamp = "timestamp";
	static final String lineId = "lineId";
	static final String direction = "direction";
	static final String journeyPatternId = "journeyPatternId";
	static final String timeFrame = "timeFrame";
	static final String vehicleJourneyId = "vehicleJourneyId";
	static final String operator = "operator";
	static final String congestion = "congestion";
	static final String longitude = "longitude";
	static final String latitude = "latitude";
	static final String delay = "delay";
	static final String blockId = "blockId";
	static final String vehicleId = "vehicleId";
	static final String stopId = "stopId";
	static final String atStop = "atStop";

	static {
		dictionary = new HashMap<String, Integer>();
		dictionary.put(timestamp, 1);
		dictionary.put(lineId, 2);
		dictionary.put(direction, 3);
		dictionary.put(journeyPatternId, 4);
		dictionary.put(timeFrame, 5);
		dictionary.put(vehicleJourneyId, 6);
		dictionary.put(operator, 7);
		dictionary.put(congestion, 8);
		dictionary.put(longitude, 9);
		dictionary.put(latitude, 10);
		dictionary.put(delay, 11);
		dictionary.put(blockId, 12);
		dictionary.put(vehicleId, 13);
		dictionary.put(stopId, 14);
		dictionary.put(atStop, 15);
	}

	static int columnOf(String attrName) {
		return dictionary.get(attrName);
	}

}
