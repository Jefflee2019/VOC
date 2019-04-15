package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author 08
 *
 */
public class NyoukuContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
//		<meta name="title" content="老罗: 华为技术功底扎实, 其他国产厂商的黑科技都是装X" />
		String title = this.getCresult(pageData, "<meta name=\"title\" content=\"(.*)\" />");
		String post_time = this.getCresult(pageData, "(\\d+-\\d+-\\d+)");
//		<a href="//i.youku.com/i/UMzQ0MTA5MjI3Mg==" class="sub-name" target="_blank">VDGER</a>
		String author = this.getCresult(pageData, "class=\"sub-name\" target=\"_blank\">(.*)</a>");
		resultData.put(Constants.POST_TIME, post_time.trim());
		resultData.put(Constants.AUTHOR, author.trim());
		resultData.put(Constants.TITLE, title.trim());
		resultData.put(Constants.CONTENT, title.trim());
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return "默认标题视频";
	}

}
