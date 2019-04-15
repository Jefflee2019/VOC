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
 * 电脑之家 新闻内容页 后处理插件
 * 
 * @author bfd_05
 *
 */
public class NpchomeContentRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NpchomeContentRe.class);
	private static final Pattern COMM_URLPAT = Pattern.compile(
			"sid=\"(\\d+)\".*?appid\\s*=\\s*'(\\w+)'.*?conf\\s*=\\s*'(.*?)'", Pattern.DOTALL);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		List<Map<String, Object>> tasks = null;
		if (resultData != null && resultData.containsKey("tasks")) {
			tasks = (List<Map<String, Object>>) resultData.get("tasks");
		} else {
			tasks = new ArrayList<Map<String, Object>>();
		}
		String commUrl = "http://changyan.sohu.com/node/html?t=1445845177495&conf=#config#&client_id=#appid#"
				+ "&topicurl=http://article.pchome.net/content-#sid#.html&topicsid=#sid#&spSize=5&pageConf=%7B%7D";
		Matcher mch = COMM_URLPAT.matcher(pageData);
		if (mch.find()) {
			String sid = mch.group(1);
			String appid = mch.group(2);
			String conf = mch.group(3);
			commUrl = commUrl.replace("#config#", conf).replace("#appid#", appid).replace("#sid#", sid);
			Map<String, Object> commTask = new HashMap<String, Object>();
			resultData.put(Constants.COMMENT_URL, commUrl);
			commTask.put("link", commUrl);
			commTask.put("rawlink", commUrl);
			commTask.put("linktype", "newscomment");
			resultData.put(Constants.COMMENT_URL, commUrl);
			tasks.add(commTask);
		}
		// 互联网 | 编辑: 潘翔城 2017-04-01 11:54:21 转载
		if (resultData.containsKey(Constants.SOURCE)) {
			String oldSource = (String) resultData.get(Constants.SOURCE);
			// 来源
			if (oldSource.contains("|")) {
				String source = toRegex("(\\S*)\\s*|", oldSource);
				resultData.put(Constants.SOURCE, source.trim());
			}
			
			// 作者
			if(oldSource.contains("编辑:")) {
			String author = toRegex("编辑:\\s*(\\S*)\\s*\\d", oldSource);
			resultData.put(Constants.AUTHOR, author.trim());
			}
			
			// 发表时间
			String postTime = toRegex("(\\d{4}-\\d{2}-\\d{2}\\s*\\d{1,2}:\\d{1,2}:\\d{1,2})", oldSource).trim();
			resultData.put(Constants.POST_TIME, postTime.trim());

			// 去除内容中的摘要部分
			if (resultData.containsKey("brief")) {
				String brief = resultData.get("brief").toString();
				String content = resultData.get("content").toString().replace(brief, "");
				resultData.put(Constants.CONTENT, content);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param sourceRex
	 */
	private String toRegex(String sourceRex, String oldSource) {
		Matcher match = Pattern.compile(sourceRex).matcher(oldSource);
		if (match.find()) {
			return match.group(1);
		} else {
			return null;
		}
	}
}