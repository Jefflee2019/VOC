package com.bfd.parse.preprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PreProcessorFactory {
	private static final Log LOG = LogFactory.getLog(PreProcessorFactory.class);
	private static PreProcessor preProcessor = null;
	public static PreProcessor getPreProcessor(String siteId, String pageTypeId) throws PreProcessorNotFound {
		return preProcessor;
	}
	public static void setPreProcessor(PreProcessor p) {
		preProcessor = p; 
	}
}
