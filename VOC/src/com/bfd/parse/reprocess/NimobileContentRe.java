package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点：手机之家
 * 功能：新闻内容页后处理
 * 处理字段，拼接评论页url
 * 
 * @author bfd_05
 *
 */

public class NimobileContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NimobileContentRe.class);
	private static final Pattern IIDPATTER = Pattern.compile("news.imobile.com.cn/articles/(\\d+)/(\\d+)/(\\d+).shtml");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		String pageData = unit.getPageData();
		Matcher match = IIDPATTER.matcher(pageData);
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String comUrl = "http://widget.weibo.com/distribution/comments.php?url=http://news.imobile.com.cn/articles/%s/%s/%s.shtml";
		if(match.find()){
			try {
				comUrl = String.format(comUrl, match.group(1), match.group(2), match.group(3));
				commentTask.put("link", comUrl);
				commentTask.put("rawlink", comUrl);
				commentTask.put("linktype", "newscomment");//任务为评论页
				LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get("link") + commentTask.get("rawlink") + commentTask.get("linktype"));
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put("comment_url", comUrl);
					List<Map> tasks = (List<Map>) resultData.get("tasks");
					if(tasks == null){
						tasks = new ArrayList<Map>();
						resultData.put("tasks", tasks);
					}
					tasks.add(commentTask);
				}
			} catch (Exception e) {
				LOG.error(e.toString());
			}
		}
		
		if(resultData.containsKey(Constants.POST_TIME)){
			String postTime = (String)resultData.get(Constants.POST_TIME);
			if(postTime.indexOf("|") > -1){
				String[] postTimes = postTime.split("\\|");
				resultData.put(Constants.POST_TIME, postTimes[1].trim());
			}
		}
		if(resultData.containsKey("content1")){
			String content = new StringBuilder().append(resultData.get("content"))
			.append(resultData.get("content1")).toString();
			resultData.remove("content1");
			resultData.put(Constants.CONTENT, content);
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
