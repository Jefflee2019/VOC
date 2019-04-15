package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bmydigit
 * 
 * 主要功能: 从标签属性中取出时间
 * 
 * @author bfd_06
 *
 */
public class BmydigitPostPre implements PreProcessor {

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		Pattern pattern = Pattern.compile("<span title=\"(\\d{4}-\\d{1,2}-\\d{1,2}.\\d{1,2}.\\d{1,2}.\\d{1,2})\">(.*?)</span>",Pattern.DOTALL);
		Matcher matcher = pattern.matcher(pageData);
		while(matcher.find()){
			String firstValue = matcher.group();
			String lastValue = firstValue.replace(matcher.group(2), matcher.group(1));
			pageData = pageData.replace(firstValue, lastValue);
		}
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("gbk"));// 一定要加上这句代码
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		unit.setPageEncode("gbk");
		
		return true;
	}
	
}
