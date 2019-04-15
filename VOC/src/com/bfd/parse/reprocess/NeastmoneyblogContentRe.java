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
 * @site:东方财富网博客 (Neastmoneyblog)
 * @function 新闻内容页后处理插件,处理发表时间、标题及评论链接
 * 
 * @author bfd_02
 *
 */

public class NeastmoneyblogContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NeastmoneyblogContentRe.class);

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

		// 处理标题
		if (resultData.containsKey(Constants.TITLE)) {
			String title = resultData.get(Constants.TITLE).toString();
			// (2014-08-22 10:39:46)
			String titReg = "(\\(\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+\\))";
			String regRest = regexMatch(titReg, title);
			if (!regRest.equals("")) {
				title = title.replace(regRest, "").trim();
				resultData.put(Constants.TITLE, title);
			}
		}

		// 处理发表时间
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			String timeReg = "\\((\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+)\\)";
			String regRest = regexMatch(timeReg, postTime);
			if (!regRest.equals("")) {
				resultData.put(Constants.POST_TIME, regRest);
			}
		}

		// 评论链接
		// http://gubawebapi.eastmoney.com/v3/read/Article/Reply/MainPostReplyList.aspx?id=400642813&ps=20&p=1&plat=Jsonp&product=UserCenter&version=200
		// http://blog.eastmoney.com/kafukano1/blog_400642813.html
		String replyCnt = resultData.get(Constants.REPLY_CNT).toString();
		if (!replyCnt.equals("0")) {
			try {
				String url = unit.getUrl();
				String urlidReg = "blog_(\\d+).html";

				String urlid = regexMatch(urlidReg, url);
				Map<String, Object> commentTask = new HashMap<String, Object>();
				if (!urlid.equals("")) {
					String commUrl = "http://gubawebapi.eastmoney.com/v3/read/Article/Reply/MainPostReplyList.aspx?callback=jQuery18308804323077734897_1481248637739&id="
							+ urlid + "&ps=20&p=1&plat=Jsonp&product=UserCenter&version=200";
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