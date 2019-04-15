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
 * 站点名：天极网-新闻
 * <p>
 * 主要功能：处理新闻内容页字段
 * @author bfd_01
 *
 */
public class NyeskyContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NyeskyContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String author = null;
			if (resultData.containsKey(Constants.AUTHOR)) {
				author = resultData.get(Constants.AUTHOR).toString()
						.replace("作者：", "");
			}
			if (resultData.containsKey(Constants.VIEW_CNT)) {
				List<String> list = (List<String>) resultData.get(Constants.VIEW_CNT);
				resultData.put(Constants.CONTENTIMGS, list);
			}
			if (resultData.containsKey(Constants.CONTENT)) {
				if (resultData.get(Constants.CONTENT) instanceof List) {
					List<String> list = (List<String>) resultData.get(Constants.CONTENT);
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < list.size(); i++) {
						sb.append(list.get(i));
					}
					resultData.put(Constants.CONTENT, sb.toString());
				}
			}
			resultData.remove(Constants.VIEW_CNT);
			
			if (resultData.containsKey(Constants.AUTHOR)
					&& resultData.get(Constants.AUTHOR).toString()
							.contains("作者：")) {
				author = resultData.get(Constants.AUTHOR).toString();
				if (author.split("作者：").length>1) {
					author = author.split("作者：")[1];
					author = author.split(" ")[0];					
				}
			}
			if (author != null) {
				resultData.put(Constants.AUTHOR, author);
			}
			if (resultData.containsKey(Constants.SOURCE)
					&& resultData.get(Constants.SOURCE).toString()
					.contains("来源：")) {
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.split("来源：").length>1) {
					source = source.split("来源：")[1];
					source = source.split(" ")[0];
					resultData.put(Constants.SOURCE, source);
				}
			}
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				Pattern p = Pattern.compile("\\d+-\\d+-\\d+ \\d+:\\d+");
				Matcher m = p.matcher(posttime);
				while (m.find()) {
					posttime = m.group();
				}
				resultData.put(Constants.POST_TIME, posttime);
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
		String urlHead = "http://changyan.sohu.com/node/html?client_id=cyrBg7Hbp";
		String topic = null;
		Pattern iidPatter = Pattern.compile("(\\d+).shtml");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			topic = match.group(1);
		}
		String comUrl = urlHead + "&topicsid=" + topic;
		commentTask.put("link", comUrl);
		commentTask.put("rawlink", comUrl);
		commentTask.put("linktype", "newscomment");
		LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
				+ commentTask.get("rawlink")
				+ commentTask.get("linktype"));
		if (resultData != null && !resultData.isEmpty()) {
			resultData.put("comment_url", comUrl);
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
			tasks.add(commentTask);
		}
		// 后处理插件加上iid
		ParseUtils.getIid(unit, result);
	}
}
