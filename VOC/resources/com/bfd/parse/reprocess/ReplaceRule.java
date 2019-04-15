package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.List;

public class ReplaceRule {
	public static List<String> floorRule = new ArrayList<String>();
	public static List<String> replyTimeRule = new ArrayList<String>();
	static{
		floorRule.add("楼");
		floorRule.add("沙发|2");
		floorRule.add("板凳|3");
		floorRule.add("地板|4");
		floorRule.add("地下室|5");
		
		replyTimeRule.add("发表于");
		replyTimeRule.add("发表于：");
	}
	
}
