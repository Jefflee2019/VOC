package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Nzhonghua(新闻) 
 * 
 * 主要功能: 对于页面多余的div进行预处理，使其可以在模板中解析出来
 * 
 * @author bfd_05
 *
 */
public class NzhonghuaContentPre implements PreProcessor {
	
	private static final Log LOG = LogFactory.getLog(NzhonghuaContentPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("NzhonghuaContentPre preprocess error");
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
		Pattern patten = Pattern.compile("<div  class=\"foosun_pagebox\".*(>下一页)",Pattern.DOTALL);
		Matcher matcher = patten.matcher(pageData);
		StringBuilder sb =  new StringBuilder();
		if(matcher.find()){
			sb.append(matcher.group(0).replace(matcher.group(1), " class='nextpage'>下一页"));
			pageData = matcher.replaceFirst(sb.toString());
		}
		return pageData;
	}
	
}
