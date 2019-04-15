package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 新浪网-新闻(Nsina)
 * @function 下一页正常翻页异常
 * 
 * @author bfd_02
 *
 */
public class NsohutvListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NsohutvListPre.class);

	@Override
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
		if(pageData.contains("style=\"display: none;\"")) {
			pageData = pageData.replace("style=\"display: none;\"", "");
		}
		return pageData;
	}
}
