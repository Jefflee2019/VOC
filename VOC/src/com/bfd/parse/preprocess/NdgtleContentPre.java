package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Ndgtle
 * 
 * 主要功能: 删除站点中img中的.jpg后缀后面的像素
 * 
 * @author bfd_03
 *
 */
public class NdgtleContentPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);

		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * 删除img中的.jpg后缀后面的像素
	 * 
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {

		Pattern patten = Pattern.compile("(src=\"http://img.dgtle.com/[/\\w]+\\.jpg)(\\!\\d+px)\"", Pattern.DOTALL);
		Matcher matcher = patten.matcher(pageData);

		while (matcher.find()) {
			pageData = pageData.replace(matcher.group(), matcher.group(1) + "\"");
		}
		return pageData;
	}

}
