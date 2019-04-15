package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.Collections;
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
 * @site:新浪网-新闻 (Nsina)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NsinaContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NsinaContentRe.class);
	private static final Pattern IIDPATTERN = Pattern.compile("comos-([\\w\\d]*)");
	private static final Pattern CHANNEL = Pattern.compile("channel:\\s*\\'(\\S*)\\',");
	private static final String COMM_URL_HEAD = "http://comment.sina.com.cn/page/info?version=1&format=json&channel=comment_channel&newsid=comment_id&group=undefined&compress=0&ie=utf-8&oe=utf-8&page=1&page_size=20";

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		 * @parem cate 路径
		 * @function 模板路径同级多块取不到最后一个值，因为最后一个值与其他的结构不一样 eg:"[新浪科技 > 电信 > 正文]"
		 * 
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object oldCate = resultData.get(Constants.CATE);
			if (oldCate instanceof List) {
				ArrayList oldCateList = (ArrayList) oldCate;
				String oldCatestr = oldCateList.get(0).toString();
				List newCateList = new ArrayList<String>();
				if (!oldCatestr.equals("") && oldCatestr.contains(">")) {
					String[] newCate = oldCatestr.split(">");
					Collections.addAll(newCateList, newCate);
					resultData.put(Constants.CATE, newCateList);
				}
			}
		}

		/**
		 * @param postTime
		 * 
		 * @function 格式化postTime eg:2015年07月13日06:19
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldPostTime = (String) resultData.get(Constants.POST_TIME);
			if (oldPostTime != null && !oldPostTime.equals("")) {
				String postTime = oldPostTime.replace("年", "-").replace("月", "-").replace("日", " ");
				resultData.put(Constants.POST_TIME, postTime);
			}
		}

		/**
		 * @param author
		 * 
		 * @function 格式化author eg:作者:马继华
		 */
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if (null != author && author.contains("作者")) {
				author = author.replaceAll("作者\\S", "");
				resultData.put(Constants.AUTHOR, author);

			}
		}

		/**
		 * @param commUrl评论链接
		 * 
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 */

		Map<String, Object> commentTask = new HashMap<String, Object>();
		// 获取新闻内容页源码
		String sourceData = unit.getPageData();
		Matcher commMatch = IIDPATTERN.matcher(sourceData);
		Matcher channelMatch = CHANNEL.matcher(sourceData);
		// url带article的新闻，评论链接不同。增加逻辑处理
		String url = unit.getUrl();
		if (url.contains("article")) {
			Matcher articleMatch = null;
			String newsid = null;
			String channel = null;
			if (url.contains(".html")) {
				articleMatch = Pattern.compile("article_(\\S*)_(\\S*).html").matcher(url);
				if (articleMatch.find()) {
					// article_1887344341_v707e96d501900htqi
					newsid = articleMatch.group(1) + "-" + articleMatch.group(2);
				}
			} else {
				// articles/view/6477101436/18210b97c00100iub8?
				articleMatch = Pattern.compile("view/(\\S*)/(\\S*)\\?").matcher(url);
				if (articleMatch.find()) {
					newsid = articleMatch.group(1) + "-" + articleMatch.group(2);
				}
			}
			// channel: 'mp',
			if (channelMatch.find()) {
				channel = channelMatch.group(1);
			}
			String commUrl = COMM_URL_HEAD.replaceAll("comment_id", newsid).replaceAll("comment_channel", channel);
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
		} else if (commMatch.find() && channelMatch.find()) {
			String itemId = commMatch.group(0);
			String comment_channel = channelMatch.group(1);
			String commUrl = COMM_URL_HEAD.replaceAll("comment_id", itemId).replaceAll("comment_channel",
					comment_channel);
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
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}