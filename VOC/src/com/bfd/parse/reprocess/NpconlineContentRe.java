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
 * @site:太平洋电脑网-新闻 (Npconline)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NpconlineContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NpconlineContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> tasks = null;
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param source来源
		 * 
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String sourceData = resultData.get(Constants.SOURCE).toString();
			if (sourceData.contains("出处")) {
				Matcher match = Pattern.compile("出处\\S(\\S*)").matcher(sourceData);
				if (match.find()) {
					String source = match.group(1);
					// 删掉格式不正确的结果
					resultData.remove(Constants.SOURCE);
					// 以正确格式赋值
					resultData.put(Constants.SOURCE, source);
				}
			}
		}
		/**
		 * @param post_time发表时间
		 * 
		 * @function 格式化post_time的值
		 *           eg1:"2015-03-02 14:44:25?出处：pconline 原创?作者：深蓝?责任编辑：chenjianhang?"
		 *           eg2:"2013-12-03 00:17:11"
		 *           eg3:"2012-05-01 09:06 出处：pconline 原创 作者：佚名 责任编辑：tangqunxing"
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldPostTime = (String) resultData.get(Constants.POST_TIME);
			if (!oldPostTime.equals("")) {
				String rex = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}(:\\d{2})?";
				Matcher matcher = Pattern.compile(rex).matcher(oldPostTime);
				if (matcher.find()) {
					String newPostTime = matcher.group();
					resultData.remove(Constants.POST_TIME);
					resultData.put(Constants.POST_TIME, newPostTime);
				}
			}
		}

		/**
		 * @param author作者
		 */

		if (resultData.containsKey(Constants.AUTHOR)) {
			String oldAuthor = (String) resultData.get(Constants.AUTHOR);
			if (oldAuthor.contains("作者")) {
				Matcher match = Pattern.compile("作者\\S(\\S*)").matcher(oldAuthor);
				if (match.find()) {
					String author = match.group(1);
					resultData.remove(Constants.AUTHOR);
					resultData.put(Constants.AUTHOR, author);
				}
			}
		}

		String url = unit.getUrl();
		// 拼接评论页链接
		if (resultData.containsKey("tasks")) {
			tasks = (List<Map<String, Object>>) resultData.get("tasks");
		} else {
			tasks = new ArrayList<Map<String, Object>>();
			resultData.put(Constants.TASKS, tasks);
		}

		String commUrl = new StringBuffer()
				.append("http://cmt.pconline.com.cn/action/comment/list_new_json.jsp?urlHandle=1&url=").append(url)
				.append("&pageSize=15&pageNo=1").toString();
		resultData.put(Constants.COMMENT_URL, commUrl);
		Map<String, Object> commUrlMap = new HashMap<String, Object>();
		commUrlMap.put("link", commUrl);
		commUrlMap.put("rawlink", commUrl);
		commUrlMap.put("linktype", "newscomment");
		tasks.add(commUrlMap);

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}