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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：爱搞机
 * <p>
 * 主要功能：处理评论数字段,评论链接
 * @author bfd_01
 *
 */
public class Nigao7ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nigao7ContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty() && resultData != null) {
			int replycnt = 0;
			if(resultData.containsKey(Constants.REPLY_CNT)) {
				String cnt = resultData.get(Constants.REPLY_CNT).toString();
				cnt = cnt.replace("条", "");
				replycnt = Integer.valueOf(cnt);
			}
			resultData.put(Constants.REPLY_CNT, replycnt);
			
			
			// http://home.igao7.com/comment/loadCommentJson?item_id=121297&type=2
			String data = unit.getPageData();
			String url = unit.getUrl();
			// 评论链接
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String topicid = findIid(data);
			String comUrl = "http://home.igao7.com/comment/loadCommentJson?item_id=" + topicid
					+ "&type=2";
			commentTask.put("link", comUrl);
			commentTask.put("rawlink", comUrl);
			commentTask.put("linktype", "newscomment");
			LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
					+ commentTask.get("rawlink")
					+ commentTask.get("linktype"));
			if (!resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, comUrl);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(commentTask);
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 取到iid，拼出url
	 * @param url
	 * @return
	 */
	private String findIid(String data) {
		Pattern iidPatter = Pattern.compile("var _item_id = \"(\\d+)\"");
		Matcher match = iidPatter.matcher(data);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	
}
