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
 * @site:安卓网-新闻 (Nhiapk)
 * @function 新闻内容页后处理插件，格式化字段及拼接评论页链接
 * 
 * @author bfd_02
 *
 */

public class NhiapkContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NhiapkContentRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param editor编辑
		 * @function 格式化editor值 eg:"编辑：Anonymous"
		 */
		if (resultData.containsKey("editor")) {
			String editor = (String) resultData.get("editor");
			editor = editor.replace("编辑：", "").trim();
			resultData.put("editor", editor);
		}

		/**
		 * @param post_time发表时间
		 * @function 格式化post_time值 
		 * 	eg1:"时间：2016-03-23"
		 */
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			String regex = "\\d+-\\d+-\\d+";
			Matcher match = Pattern.compile(regex).matcher(postTime);
			if (match.find()) {
				postTime = match.group();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}

		/**
		 * @param source来源
		 * @function 格式化source字段
		 * 	eg1:"【来源：网易科技】"
		 */
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			Matcher match = Pattern.compile("【来源：(\\S+)】").matcher(source);
			if (match.find()) {
				source = match.group(1);
				resultData.put(Constants.SOURCE, source);
			}
		}
		
		/**
		 * @param commUrl评论链接
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 * 
		 */

		Map<String, Object> commentTask = new HashMap<String, Object>();
		String reg = null;
		String commUrl = null;
		String client_id = null;
		String title = null;
		String pageData = unit.getPageData();
		String currentUrl = unit.getUrl();
		String commUrlHead = "http://changyan.sohu.com/node/html?";
		if (pageData.contains("var appid =")) {
			// var appid = 'cyqRGufpG',
			reg = "var\\s*appid\\s*=\\s*\'(\\w*)\',";
			Matcher match = Pattern.compile(reg, Pattern.DOTALL).matcher(pageData);
			if (match.find()) {
				client_id = match.group(1);
			}
		}

		if (pageData.contains("</title>")) {
			// <title>没有什么不同---我在华为的日子 - A5站长网</title>
			// reg = <title>(.*)</title>
			reg = "<title>([-\\s[\u4E00-\u9FA5]\\w]+)</title>";
			Matcher match = Pattern.compile(reg, Pattern.DOTALL).matcher(pageData);
			if (match.find()) {
				title = match.group(1).trim();
			}
		}
		
		commUrl = commUrlHead + "client_id=" + client_id + "&title=" + title + "&topicurl=" + currentUrl;
		commentTask.put(Constants.LINK, commUrl);
		commentTask.put(Constants.RAWLINK, commUrl);
		commentTask.put(Constants.LINKTYPE, "newscomment");
		LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get(Constants.LINK)
				+ commentTask.get(Constants.RAWLINK) + commentTask.get(Constants.LINKTYPE));
		if (resultData != null && resultData.size() > 0) {
			resultData.put(Constants.COMMENT_URL, commUrl);
			List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
			tasks.add(commentTask);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}