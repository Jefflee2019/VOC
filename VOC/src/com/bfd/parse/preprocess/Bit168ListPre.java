package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：it168(论坛) 
 * 
 * 主要功能: 对于页面多余的div进行预处理，使其可以在模板中解析出来
 * 
 * @author bfd_03
 *
 */
public class Bit168ListPre implements PreProcessor {

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {

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
		Pattern patten = Pattern.compile("<div class=\"drag\">.*?<!--\\[/diy\\]-->\\s*</div>",Pattern.DOTALL);
		Matcher matcher = patten.matcher(pageData);
		StringBuffer sb = new StringBuffer();
	
		if(matcher.find()){
			sb.append("<div class=\"drag\">");
			sb.append("	<div id=\"diy4\" class=\"area\">");
			sb.append("	</div>");
			sb.append("</div>");
			return matcher.replaceFirst(sb.toString());
		}
		return pageData;
	}
	
}
