package com.whaka.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Strings;

public class MessageBuilder {

	private final Map<String, Object> parameters = new LinkedHashMap<>();

	public MessageBuilder putParameter(String key, Object parameter) {
		getParameters().put(key, parameter);
		return this;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public String build(String message) {
		return build(message, new Object[0]);
	}
	
	public String build(String message, Object... args) {
		message = String.format(Strings.nullToEmpty(message), args);
		return build(message, getParameters());
	}
	
	public static String build(String message, Map<String, Object> messageParameters) {
		message = Strings.nullToEmpty(message);
		if (messageParameters == null || messageParameters.isEmpty())
			return message;
		return message + messageParameters.entrySet().toString();
	}
}
