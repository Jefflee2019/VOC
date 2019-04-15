package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bcnmo
 * 
 * 主要功能: 
 * 
 * @author bfd_03
 *
 */
public class EladyLee_hondurasListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EladyLee_hondurasListPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
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

	/**
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		//删除价格中de字段
		pageData = pageData.replaceAll("<span class='previous-price'>L\\S+</span>", "").trim();
		
		return pageData;
	}
	
}
