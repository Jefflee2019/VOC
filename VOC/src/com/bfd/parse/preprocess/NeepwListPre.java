package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Neepw(新闻) 
 * 
 * 主要功能: 对于页面多余的font进行预处理，使其可以在模板中解析出来
 * 
 * @author bfd_05
 *
 */
public class NeepwListPre implements PreProcessor {
	
	private static final Log LOG = LogFactory.getLog(NeepwListPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("NeepwListPre preprocess error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * 删除<div id="diy4"></div>中的内容
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData){
		pageData = pageData.replace("<p><font>", "<p>");
		return pageData;
	}
	
}
