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
 * 站点名：同花顺金融服务网
 * <p>
 * 主要功能：处理字段中多余的数据
 * 
 * @author bfd_01
 *
 */
public class N10jqkaContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N10jqkaContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			// String data = unit.getPageData();
			String url = result.getSpiderdata().get("location").toString();
			if (resultData.containsKey(Constants.SOURCE)) {
				// 处理来源和发表时间
				// source 来源： 挖贝网 2016-11-02 11:31:43 0
				//replaceAll("\\d*","") --处理源码中发表时间后面附带的数字
				String source = resultData.get(Constants.SOURCE).toString();
				Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+").matcher(source);
				if (match.find()) {
					resultData.put(Constants.POST_TIME, match.group());
					resultData.put(Constants.SOURCE, source.replace(match.group(), "").replace("来源： ", "").replaceAll("\\d*","").trim());
				} else {
					resultData.put(Constants.SOURCE, source.replace("来源： ", ""));
				}
			}
			
			//处理作者字段 
			//author 券商中国 暂无评分
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if(author.contains("暂无评分")) {
					author = author.replace("暂无评分", "").trim();
					resultData.put(Constants.AUTHOR, author);
				}
			}
			// 热门评论
			// 添加评论页任务
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String urlHead = "http://comment.10jqka.com.cn/hotcomment/";
			String param1 = "";
			Pattern p = Pattern.compile("c(\\d+).shtml");
			Matcher m = p.matcher(url);
			while (m.find()) {
				param1 = m.group(1);
			}
			String comUrl = urlHead + param1.substring(0, 3) + "/" + param1 + ".txt";
			commentTask.put("link", comUrl);
			commentTask.put("rawlink", comUrl);
			commentTask.put("linktype", "newscomment");
			LOG.info("url:" + url + "taskdata is " + commentTask.get("link") + commentTask.get("rawlink")
					+ commentTask.get("linktype"));
			if (!resultData.isEmpty()) {
				resultData.put("comment_url", comUrl);
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				tasks.add(commentTask);
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
}
