package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bhongdou
 * 
 * 主要功能: 对于页面不规范的html便签不闭合问题进行预处理，使其可以在模板中解析出来
 * 
 * @author bfd_05
 *
 */
public class BhongdouPostPre implements PreProcessor {
	
	private static final Log LOG = LogFactory.getLog(BhongdouPostPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("BhongdouPostPre preprocess error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData){
		pageData = pageData.replace("<if condition=\"0\">", "")
				.replace("<if condition=\"1\">", "")
				.replace("<if condition=\"\">", "")
				.replace("</if>", "");
		return pageData;
	}
	
}
