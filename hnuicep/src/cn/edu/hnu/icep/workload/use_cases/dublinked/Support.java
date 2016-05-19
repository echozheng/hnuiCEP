package cn.edu.hnu.icep.workload.use_cases.dublinked;

import cn.edu.hnu.icep.workload.history_generation.FileImporter;
import cn.edu.hnu.icep.workload.use_cases.dublinked.Consts;

public class Support {

	static final void createNewEventType(String evType, FileImporter importer) {
		importer.addEvent(evType, 1);
	}

	static final void addLineId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.lineId, importer);
	}

	static final void addLineIdToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.lineId, importer);
	}

	static final void addDirection(String evType, FileImporter importer) {
		addAttribute(evType, Consts.direction, importer);
	}

	static final void addDirectionToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.direction, importer);
	}

	static final void addJourneyPatternId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.journeyPatternId, importer);
	}

	static final void addJourneyPatternIdToType(String evType,
			FileImporter importer) {
		addAttributeToType(evType, Consts.journeyPatternId, importer);
	}

	static final void addTimeFrame(String evType, FileImporter importer) {
		addAttribute(evType, Consts.timeFrame, importer);
	}

	static final void addTimeFrameToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.timeFrame, importer);
	}

	static final void addVehicleJourneyId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.vehicleJourneyId, importer);
	}

	static final void addVehicleJourneyIdToType(String evType,
			FileImporter importer) {
		addAttributeToType(evType, Consts.vehicleJourneyId, importer);
	}

	static final void addOperator(String evType, FileImporter importer) {
		addAttribute(evType, Consts.operator, importer);
	}

	static final void addOperatorToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.operator, importer);
	}

	static final void addCongestion(String evType, FileImporter importer) {
		addAttribute(evType, Consts.congestion, importer);
	}

	static final void addCongestionToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.congestion, importer);
	}

	static final void addLongitude(String evType, FileImporter importer) {
		addAttribute(evType, Consts.longitude, importer);
	}

	static final void addLongitudeToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.longitude, importer);
	}

	static final void addLatitude(String evType, FileImporter importer) {
		addAttribute(evType, Consts.latitude, importer);
	}

	static final void addLatitudeToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.latitude, importer);
	}

	static final void addDelay(String evType, FileImporter importer) {
		addAttribute(evType, Consts.delay, importer);
	}

	static final void addDelayToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.delay, importer);
	}

	static final void addBlockId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.blockId, importer);
	}

	static final void addBlockIdToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.blockId, importer);
	}

	static final void addVehicleId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.vehicleId, importer);
	}

	static final void addVehicleIdToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.vehicleId, importer);
	}

	static final void addStopId(String evType, FileImporter importer) {
		addAttribute(evType, Consts.stopId, importer);
	}

	static final void addStopIdToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.stopId, importer);
	}

	static final void addAtStop(String evType, FileImporter importer) {
		addAttribute(evType, Consts.atStop, importer);
	}

	static final void addAtStopToType(String evType, FileImporter importer) {
		addAttributeToType(evType, Consts.atStop, importer);
	}

	private static final void addAttribute(String evType, String attrName,FileImporter importer) {
		importer.addAttribute(evType, attrName, Consts.columnOf(attrName));
	}

	private static final void addAttributeToType(String evType,String attrName, FileImporter importer) {
		importer.addAttributeToType(evType, Consts.columnOf(attrName));
	}

}
