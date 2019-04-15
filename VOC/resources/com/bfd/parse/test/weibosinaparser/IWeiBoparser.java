package com.bfd.parse.test.weibosinaparser;

import java.util.Map;
import java.util.regex.Pattern;


public interface IWeiBoparser {

//	public static Map<String, Integer> downloadedParentUserId = new ConcurrentHashMap<String, Integer>();
//	public static Map<String, Integer> downloadedUsers = new ConcurrentHashMap<String, Integer>();
	public static Pattern pUserId = Pattern.compile("uid=(\\d+)&?");
	public Map<String, Object> parseHtml(String html,Task task) throws Exception;
	public static final Pattern getAllHtmlJson = Pattern
			.compile("<script>FM.view\\(([\\s\\S]*?)\\)</script>");
	public static Pattern getAllHtmlJson_search = Pattern
			.compile("<script>STK && STK.pageletM && STK\\.pageletM\\.view\\(([\\s\\S]*?)\\)</script>");
}
