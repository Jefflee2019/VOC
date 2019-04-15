package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.loginutil.JsonUtils;
import com.bfd.crawler.utils.htmlcleaner.HtmlCleanerUtil;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bzol(论坛) 
 * 
 * 主要功能: 对于页面多余的div进行预处理，使其可以在模板中解析出来
 * 
 * @author bfd_05
 *
 */
public class BzolPostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BzolPostPre.class);

	//
	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("BzolPostPre preprocess error");
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
//		截取掉源码中一部分代码导致content字段多后缀的问题
		int i = pageData.indexOf("<div class=\"pro-examine\" id=\"proScoreModule\">");
		if (i != -1) {
			int j = pageData.indexOf("<div class=\"hostPost-options\">");
			pageData = pageData.substring(0, i) + "</div>" + pageData.substring(j, pageData.length());
//			System.err.println(pageData);
		}
		
		Pattern cityPat = Pattern.compile("<li><span>城&nbsp;&nbsp;市：",Pattern.DOTALL);
		Pattern regPat = Pattern.compile("<li><span>注&nbsp;&nbsp;册：",Pattern.DOTALL);
		Pattern loginPat = Pattern.compile("<li><span>登&nbsp;&nbsp;录：",Pattern.DOTALL);
		pageData = matcherReplace(pageData, cityPat, "li", "li class='city'");
		pageData = matcherReplace(pageData, regPat, "li", "li class='regtime'");
		pageData = matcherReplace(pageData, loginPat, "li", "li class='logintime'");
		return pageData;
	}
	
	private String matcherReplace(String pageData, Pattern p, String... replaces){
		Matcher matcher = p.matcher(pageData);
		StringBuilder sb =  new StringBuilder();
		if(matcher.find()){
			sb.append(matcher.group(0).replace(replaces[0], replaces[1]));
			pageData = matcher.replaceAll(sb.toString());
		}
		return pageData;
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
		return str;
	}
	
}
