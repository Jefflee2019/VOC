package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：天极网论坛
 * <p>
 * 主要功能:<i class="pstatus"> 本帖最后由 阳亮 于 2013-5-22 21:46 编辑 </i><br />
 * @author bfd_03
 */
public class ByeskyPostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostPre.class);
	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * 手工删除   <i class="pstatus"> 本帖最后由 阳亮 于 2013-5-22 21:46 编辑 </i><br />
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		Pattern pattern = Pattern.compile("<i class=\"pstatus\">.*?</i><br />");
		Matcher matcher = pattern.matcher(pageData);
		if(matcher.find()){
			pageData = pageData.replace(matcher.group(), "");
		}
		return pageData;
	}
}
