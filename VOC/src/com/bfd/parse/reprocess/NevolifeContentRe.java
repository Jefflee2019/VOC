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
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：爱活网
 * 功能：发表时间字段生成评论任务
 * @author bfd01
 *
 */
public class NevolifeContentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NevolifeContentRe.class);

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
					Pattern p = Pattern.compile("((\\d+)/(\\d+)/(\\d+))");
					Matcher m = p.matcher(posttime);
					while (m.find()) {
						posttime = m.group(1);
					}
					
					posttime = posttime.replaceAll("/", "-");
					resultData.put("post_time", posttime);
				}
				
				if (resultData.containsKey(Constants.REPLY_CNT)) {
					String cnt = resultData.get(Constants.REPLY_CNT).toString();
					int replycnt = 0;
					replycnt = Integer.valueOf(cnt);
					String commentUrl = null;
					String url = unit.getUrl();
					Map<String, Object> commentTask= new HashMap<String, Object>();
					if (replycnt > 0) {
						commentUrl = url + "#comment";
						commentTask.put("link", commentUrl);
						commentTask.put("rawlink", commentUrl);
						commentTask.put("linktype", "newscomment");
						resultData.put(Constants.COMMENT_URL, commentUrl);
						if (resultData.containsKey("tasks")) {
							List<Map> tasks = (List<Map>) resultData
									.get("tasks");
							tasks.add(commentTask);
						}

					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		ParseUtils.getIid(unit, result);
		// 解析结果返回值 0代表成功
		return new ReProcessResult(processcode, processdata);
	}
}
