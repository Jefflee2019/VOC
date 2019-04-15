package com.bfd.parse.reprocess;

import java.net.URLEncoder;
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
 * @site:凤凰网-论坛 (NifengblogContentRe)
 * @note：因为论坛内容都是博客，类新闻，所以当做新闻类站点开发
 * @function 新闻内容页后处理插件处理字段和拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class NifengblogContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NifengblogContentRe.class);

	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		/**
		 * @param view_cnt
		 * @function 浏览数字段格式化 eg:"view_cnt": "浏览 6555 次"
		 */
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			String oldViewCnt = resultData.get(Constants.VIEW_CNT).toString();
			Matcher m = Pattern.compile("(\\d+)").matcher(oldViewCnt);
			if (m.find()) {
				int viewCnt = Integer.parseInt(m.group(1));
				resultData.put(Constants.VIEW_CNT, viewCnt);
			}
		}

		/**
		 * @param comment_url评论链接
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 * 
		 */
		/**
		 * http://comment.ifeng.com/get?job=1&order=DESC&orderBy=create_time&
		 * format
		 * =js&pagesize=10&p=1&docUrl=http%3A%2F%2Fblog.ifeng.com%2Farticle
		 * %2F34690957.html
		 */

		// 获取新闻内容页url
		String url = unit.getUrl();
		// 存放评论任务的map
		Map<String, Object> commentTask = new HashMap<String, Object>();
		try {
			// 附带的url需要解码
			url = URLEncoder.encode(url);
			String commUrlHead = "http://comment.ifeng.com/get?job=1&order=DESC&orderBy=create_time&format=js&pagesize=10&p=1&docUrl=";
			String commUrl = commUrlHead + url;
			commentTask.put(Constants.LINK, commUrl);
			commentTask.put(Constants.RAWLINK, commUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get(Constants.LINK)
					+ commentTask.get(Constants.RAWLINK) + commentTask.get(Constants.LINKTYPE));
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, commUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);
			}
			ParseUtils.getIid(unit, result);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}