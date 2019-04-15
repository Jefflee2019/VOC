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
 * 站点名：和讯网-新闻
 * <p>
 * 主要功能：生成评论页任务
 * @author bfd_01
 *
 */
public class NhexunContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NhexunContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains(" ") && author.split(" ").length > 3) {
					author = author.split(" ")[3];
					resultData.put(Constants.AUTHOR, author);
					//针对没有作者的时候置空操作20160720
				}else{
					resultData.put(Constants.AUTHOR, "");
				}
			}
			// 生成评论任务
			getCommentUrl(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 生成评论页任务
	 * @param unit
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		// 生成评论任务
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = result.getSpiderdata().get("location").toString();
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String urlHead = "http://comment.tool.hexun.com/Comment/GetComment.do?articleid=";
		String topic = null;
		Pattern iidPatter = Pattern.compile("(\\d+).html");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			topic = match.group(1);
		}
		String comUrl = urlHead + topic + "&pagesize=10&pagenum=1";
		commentTask.put("link", comUrl);
		commentTask.put("rawlink", comUrl);
		commentTask.put("linktype", "newscomment");
		LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
				+ commentTask.get("rawlink")
				+ commentTask.get("linktype"));
		if (!resultData.isEmpty()) {
			resultData.put("comment_url", comUrl);
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
			tasks.add(commentTask);
		}
		// 后处理插件加上iid
		ParseUtils.getIid(unit, result);
	}
}
