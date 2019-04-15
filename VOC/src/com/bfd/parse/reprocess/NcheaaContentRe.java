package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;

public class NcheaaContentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NcheaaContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (resultData != null && resultData.size() > 0) {
				if(resultData.containsKey("post_time")) {
					Pattern time = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})");
					Matcher matcher = time.matcher(resultData.get("post_time").toString());
					if(matcher.find()) {
						resultData.put("post_time", matcher.group(1));
					}
				}
				if(resultData.containsKey("source")) {
					Pattern time = Pattern.compile("来源: (\\S+)");
					Matcher matcher = time.matcher(resultData.get("source").toString());
					if(matcher.find()) {
						resultData.put("source", matcher.group(1));
					}
				}
				if(resultData.containsKey("cate")) {
					Object o = resultData.get("cate");
					if(o instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<String> cate = (List<String>) o;
						String cateStr = cate.get(0);
						resultData.put("cate", cateStr.substring(0, cateStr.indexOf("正文") + 2));
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
}
