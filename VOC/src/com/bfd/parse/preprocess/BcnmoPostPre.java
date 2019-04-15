package com.bfd.parse.preprocess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bcnmo
 * 
 * 主要功能: 手工删除<i class="pstatus"> 本帖最后由 乐之~~ 于 2013-8-29 18:45 编辑 </i><br />
 * 
 * @author bfd_03
 *
 */
public class BcnmoPostPre implements PreProcessor {

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
//		unit.setPageBytes(pageData.getBytes());
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * 手工删除   <i class="pstatus"> 本帖最后由 乐之~~ 于 2013-8-29 18:45 编辑 </i><br />
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
