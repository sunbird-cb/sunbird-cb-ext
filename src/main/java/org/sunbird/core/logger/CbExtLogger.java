package org.sunbird.core.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

public class CbExtLogger {
	private Logger logger;
	private boolean isDebugEnabled;
	private boolean isInfoEnabled;
	private boolean isTraceEnabled;

	public CbExtLogger(String className) {
		this.logger = LogManager.getLogger(className);
		isDebugEnabled = logger.isDebugEnabled();
		isInfoEnabled = logger.isInfoEnabled();
		isTraceEnabled = logger.isTraceEnabled();
	}

	public void debug(String message) {
		logger.log(Level.DEBUG, message);
	}

	public void info(String message) {
		logger.log(Level.INFO, message);
	}

	public void warn(String message) {
		logger.log(Level.WARN, message);
	}

	public void error(Exception exception) {
		ObjectMapper ow = new ObjectMapper();

		// log the exception
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("event", exception.getClass());
			message.put("message", exception.getMessage());
			message.put("trace", Throwables.getStackTraceAsString(exception));
			logger.log(Level.ERROR, ow.writeValueAsString(message));
		} catch (Exception e) {
			logger.log(Level.ERROR,
					"{\"event\":\"" + exception.getClass() + "\", \"message\":\"" + exception.getMessage()
							+ "\", \"trace\":\"" + Throwables.getStackTraceAsString(exception) + "\"}");
		}
	}

	public void fatal(Exception exception) {
		ObjectMapper ow = new ObjectMapper();

		// log the exception
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("event", exception.getClass());
			message.put("message", exception.getMessage());
			message.put("trace", Throwables.getStackTraceAsString(exception));
			logger.log(Level.FATAL, ow.writeValueAsString(message));
		} catch (Exception e) {
			logger.log(Level.FATAL,
					"{\"event\":\"" + exception.getClass() + "\", \"message\":\"" + exception.getMessage()
							+ "\", \"trace\":\"" + Throwables.getStackTraceAsString(exception) + "\"}");
		}
	}

	public void trace(String message) {
		logger.log(Level.TRACE, message);
	}

	public void performance(String message) {
		Level performance = Level.forName("PERF", 350);
		logger.log(performance, message);
	}

	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public boolean isInfoEnabled() {
		return isInfoEnabled;
	}

	public boolean isTraceEnabled() {
		return isTraceEnabled;
	}
}