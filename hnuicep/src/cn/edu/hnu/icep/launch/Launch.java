package cn.edu.hnu.icep.launch;

import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.eval.EvalRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Launch {
	public static void main(String args[]) {
		LogManager manager = LogManager.getLogManager();
		try {
			manager.readConfiguration(new FileInputStream(new File(Consts.LOG_FILE_NAME)));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}

		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		logger.addHandler(consoleHandler);

		EvalRunner evalRunner = new EvalRunner();
		evalRunner.runAll();
	}
}
