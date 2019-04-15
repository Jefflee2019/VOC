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
import com.bfd.parse.util.ParseUtils;

public class NguangmingContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NguangmingContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String url = result.getSpiderdata().get("location").toString();
			if (url.contains("epaper")) {
				if (resultData.containsKey(Constants.AUTHOR)) {
					String author = resultData.get(Constants.AUTHOR).toString();
					author = author.replace("本报记者", "").replace("作者：", "").trim();
					resultData.put(Constants.AUTHOR, author);
				}
				if (resultData.containsKey(Constants.SOURCE)) {
					String source = resultData.get(Constants.SOURCE).toString();
					resultData.put(Constants.SOURCE, source.split(" ")[0]);
				}
				if (resultData.containsKey(Constants.POST_TIME)) {
					String posttime = resultData.get(Constants.POST_TIME).toString();
					resultData.put(Constants.POST_TIME, formatDate(posttime.split(" ")[0]));
				}
			}
			if (resultData.containsKey(Constants.CATE)) {
				List<String> cate = (List<String>)resultData.get(Constants.CATE);
				for (int i=0;i<cate.size();i++) {
					if (cate.get(0).equals("")) {
						cate.remove(0);
					}
				}
			}
			
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString()
						.replace("来源：", "");
				resultData.put(Constants.SOURCE, source);
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
		String urlHead = "http://changyan.sohu.com/node/html?client_id=cyr45LmB4";
		String topic = null;
		Pattern iidPatter = Pattern.compile("(\\d+).htm");
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
		if (resultData!=null && !resultData.isEmpty()) {
			resultData.put("comment_url", comUrl);
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
			tasks.add(commentTask);
		}
		// 后处理插件加上iid
		ParseUtils.getIid(unit, result);
	}
	
	private String formatDate(String date) {
		String posttime = null;
		if (date.contains("年") && date.contains("月") && date.contains("日")) {
			posttime = date.replace("年", "-").replace("月", "-").replace("日", "");
		}
		return posttime;
	}
}
