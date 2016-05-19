package cn.edu.hnu.icep.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 设定系统的一些常量
 * @author hduser
 * **/
public class Consts {
	private static final Properties properties = new Properties();
	private static final String FILES_PATH;

	public static final String RESULTS_PATH;
	public static final String LOG_FILE_NAME;

	public static final String DUBLINKED_PATH;

	public static final String EVENT_TYPE_PREFIX;
	public static final String ATTRIBUTE_TYPE_PREFIX;
	public static final String COMPOSITE_EVENT_TYPE;

	public static final int HISTORY_SIZE;
	public static final int NUM_ARTIFICIAL_COMPOSITE_EVENTS;
	public static final int NUM_ARTIFICIAL_NEGATIONS;
	public static final int NUM_THREADS;
	public static final int NUM_REPETITIONS;

	static {
		try {
			FileInputStream input = new FileInputStream("icep.properties");
			properties.load(input);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 如果machine属性未找到，那么新建一个属性为“machine”，其value值为：“MBP”
		String machine = properties.getProperty("machine", "MBP");

		if (machine.equals("MBP")) {
			FILES_PATH = "/Volumes/RamDisk/";
			DUBLINKED_PATH = "/Users/margara/Desktop/";
		} else {
			FILES_PATH = "./";
			DUBLINKED_PATH = "./Input/";
		}

		RESULTS_PATH = FILES_PATH + "Results/";
		LOG_FILE_NAME = "logging.properties";

		EVENT_TYPE_PREFIX = "Event";
		ATTRIBUTE_TYPE_PREFIX = "Attribute";
		COMPOSITE_EVENT_TYPE = "CompositeEvent";

		String historySize = properties.getProperty("historySize", "200000");
		HISTORY_SIZE = Integer.valueOf(historySize);

		String numArtificialCompositeEvents = properties.getProperty(
				"numArtificialCompositeEvents", "1000");
		NUM_ARTIFICIAL_COMPOSITE_EVENTS = Integer
				.valueOf(numArtificialCompositeEvents);

		String numArtificialNegations = properties.getProperty(
				"numArtificialNegations", "1000");
		NUM_ARTIFICIAL_NEGATIONS = Integer.valueOf(numArtificialNegations);

		String numThreads = properties.getProperty("numThreads", "1");
		NUM_THREADS = Integer.valueOf(numThreads);

		String numRepetitions = properties.getProperty("numRepetitions", "1");
		NUM_REPETITIONS = Integer.valueOf(numRepetitions);
	}
}
