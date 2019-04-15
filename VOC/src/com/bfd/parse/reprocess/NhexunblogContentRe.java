package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
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
 * @site:和讯博客 (Nhexunblog)
 * @function 新闻内容页后处理插件,处理路径、发表时间及评论链接
 * 
 * @author bfd_02
 *
 */

public class NhexunblogContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NhexunblogContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		// 处理标签中空字符串,如"[房价, , 深圳, ]"
		if (resultData.containsKey(Constants.TAG)) {
			Object tag = resultData.get(Constants.TAG);
			if (tag instanceof List) {
				List<String> taglist = (List<String>) tag;
				for (Iterator<String> it = taglist.iterator(); it.hasNext();) {
					String str = it.next();
					if (str.equals("")) {
						it.remove();
					}
				}
				resultData.put(Constants.TAG, taglist);
			}
		}

		// 发表时间标准化 "深圳高房价高地价逼走华为是必然的事 [原创 2016-11-21 7:29:41]"
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+?(:\\d+)").matcher(postTime);
			if (match.find()) {
				postTime = match.group();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}

		// 评论链接
		// http://comment.blog.hexun.com/comment/get.aspx?articleid=109158938&blogid=4601397&page=1
		// 获取页面源码
		try {
			String pageData = unit.getPageData();
			String url = unit.getUrl();
			String blogreg = "cache-sidebar.blog.hexun.com/inc/ARecommend.aspx\\?blogid=(\\d+)&";
			String artreg = "blog.hexun.com/(\\d+)_d.html";

			String blogid = regexMatch(blogreg, pageData);
			String articleid = regexMatch(artreg, url);
			Map<String, Object> commentTask = new HashMap<String, Object>();
			if (!blogid.equals("") && !articleid.equals("")) {
				String commUrl = "http://comment.blog.hexun.com/comment/get.aspx?articleid=" + articleid + "&blogid="
						+ blogid + "&page=1";
				commentTask.put(Constants.LINK, commUrl);
				commentTask.put(Constants.RAWLINK, commUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				resultData.put(Constants.COMMENT_URL, commUrl);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);
			}
			ParseUtils.getIid(unit, result);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String regexMatch(String regex, String data) {
		String targetid = "";
		Matcher match = Pattern.compile(regex).matcher(data);
		if (match.find()) {
			targetid = match.group(1);
		}
		return targetid;
	}
}