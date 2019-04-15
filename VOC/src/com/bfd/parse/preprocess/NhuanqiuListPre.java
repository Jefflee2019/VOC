package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NhuanqiuListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NhuanqiuListPre.class);

	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();

		pageData = parsePageData(pageData);

		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.warn("Charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	public String parsePageData(String pageData) {
		if (pageData.contains(">下一页<")) {
			int index = pageData.indexOf(">下一页<");
			pageData = pageData.substring(0, index) + " class='nextpage'" + pageData.substring(index);
		}
		return pageData;
	}
}