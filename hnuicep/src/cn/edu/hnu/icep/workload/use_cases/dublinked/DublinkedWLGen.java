package cn.edu.hnu.icep.workload.use_cases.dublinked;

import cn.edu.hnu.icep.workload.use_cases.dublinked.Support;
import cn.edu.hnu.icep.workload.history_generation.FileImporter;
import cn.edu.hnu.icep.workload.use_cases.dublinked.Consts;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.Rule;

public class DublinkedWLGen {
	
	private static final String delayEvType = "Delay";
	private static final String congestionEvType = "Congestion";
	private static final String atStopEvType = "AtStop";

	public static Rule generateRule(ParamHandler ph) {
		// TODO Auto-generated method stub
		return null;
	}

	public static History generateTraningHistory(ParamHandler ph) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static History generateEvaluationHistory(ParamHandler ph) {
		String filename = ph.getDublinkedEvaluationFile();
		int numLines = ph.getDublinkedNumLines();
		return generateHistory(filename, numLines);
	}

	public static History generateHistory(String filename, long numLines) {
		History history = new History();
		FileImporter importer = new FileImporter(filename, ",",Consts.numFields, numLines);
		importer.addUnacceptableValue("null");
		generateEvents(importer);
		importer.decorateHistory(history);
		return history;
	}
	
	private static void generateEvents(FileImporter importer) {
		Support.createNewEventType(delayEvType, importer);
		Support.addLineIdToType(delayEvType, importer);
		Support.addVehicleId(delayEvType, importer);
		Support.addDelay(delayEvType, importer);
	}
	
	
}
