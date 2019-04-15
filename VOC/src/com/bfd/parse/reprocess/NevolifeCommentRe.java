package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;

/**
 * 站点名：爱活网
 * 功能：评论时间字段
 * @author bfd01
 *
 */
public class NevolifeCommentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NevolifeCommentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				
				if(resultData.containsKey(Constants.COMMENTS)) {
					
					List<Map<String, Object>> comments = (List<Map<String, Object>>)resultData.get(Constants.COMMENTS);
					for (int i=0;i<comments.size();i++) {
						Map<String, Object> comment = comments.get(i);
						String time = comment.get("comment_time").toString();
						comment.put(Constants.COMMENT_TIME, timeFormat(time));
					}
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		// 解析结果返回值 0代表成功
		return new ReProcessResult(processcode, processdata);
	}
	
	private String timeFormat(String time) {
		// 2017年4月24日 下午3:18
		Pattern p = Pattern.compile("(\\d+)\\S(\\d+)\\S(\\d+)\\S\\s(\\S{2})(\\d+):(\\d+)");
		Matcher m = p.matcher(time);
		String year = null;
		String month = null;
		String day = null;
		String hour = null;
		String minute = null;
		String morning = null;
		
		while (m.find()) {
			year = m.group(1);
			month = m.group(2);
			day = m.group(3);
			hour = m.group(5);
			minute = m.group(6);
			morning = m.group(4);
		}
		
		if ("下午".equals(morning)) {
			int hour1 = Integer.valueOf(hour) + 12;
			hour = String.valueOf(hour1);
		}
		String temp = year + "-" + month + "-" + day + " " + hour + ":" +minute;
		return temp;
	}
	
}
