package com.bfd.parse.test.weibosinaparser;

import java.util.HashMap;
import java.util.Map;

public class WeiboParserFactory {
	public static Map<String, IWeiBoparser> parsers = new HashMap<String, IWeiBoparser>();
	static{
		parsers.put("home", new UserInfoParser());
		parsers.put("weibo", new WeiboParser());
		parsers.put("follow", new FensiListParser());
		parsers.put("keyword", new SearchParser());
		parsers.put("repost", new RepostParser());
		parsers.put("comment", new CommentParser());
	}
	public static IWeiBoparser getParserByType(String type){
		return parsers.get(type);
	}
}
