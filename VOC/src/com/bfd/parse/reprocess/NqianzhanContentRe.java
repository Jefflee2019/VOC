package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;

/**
 * 站点名：前瞻网
 * 功能：处理作者、发表时间等字段
 * @author bfd01
 *
 */
public class NqianzhanContentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NqianzhanContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				if(resultData.containsKey("post_time")) {
					String posttime = resultData.get("post_time").toString();
					posttime = ConstantFunc.convertTime(posttime);
						resultData.put("post_time", posttime);
				}
				if (resultData.containsKey(Constants.VIEW_CNT)) {
					String viewcnt = resultData.get(Constants.VIEW_CNT).toString();
					int cnt = Integer.valueOf(viewcnt.replace("E", ""));
					resultData.put(Constants.VIEW_CNT, cnt);
				}
				
				if(resultData.containsKey("source")) {
					Pattern time = Pattern.compile("来源：(\\S+)");
					Matcher matcher = time.matcher(resultData.get("source").toString());
					if(matcher.find()) {
						resultData.put("source", matcher.group(1));
					}
				}
				
				if (resultData.containsKey(Constants.AUTHOR)) {
					String author = resultData.get(Constants.AUTHOR).toString();
					if (author.contains(" • ")) {
						author = author.split(" • ")[0];
						resultData.put(Constants.AUTHOR, author.trim());
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
