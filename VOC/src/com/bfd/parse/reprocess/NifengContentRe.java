package com.bfd.parse.reprocess;

import java.net.URLEncoder;
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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:凤凰网-新闻 (Nifeng)
 * @function 新闻内容页后处理插件处理字段和拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class NifengContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NifengContentRe.class);

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
		 * 2019-03-09 凤凰网新闻内容改版修复 考虑到有些关键字只有2月份以前的数据，那么需要在新增的基础上兼容
		 */
		String url = unit.getUrl();
		// http://news.ifeng.com/c/7kgzoARrK15 新增数据的url都是'/c/'结构，不同于以往
		if (url.contains("/c/")) {
			String pageData = unit.getPageData();
			Matcher match = Pattern.compile("var\\s*allData\\s*=\\s*(.*);\\s*var\\s*adData").matcher(pageData);
			if (match.find()) {
				List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
				try {
					Map newsdataMap = (Map<String, Object>) JsonUtil.parseObject(match.group(1));
					if (newsdataMap.containsKey("docData")) {
						Map<String, Object> docDataMap = (Map<String, Object>) newsdataMap.get("docData");
						// 标题
						if (docDataMap.containsKey("title")) {
							String title = (String) docDataMap.get("title");
							resultData.put(Constants.TITLE, title);
						}

						// 来源
						if (docDataMap.containsKey("source")) {
							String source = (String) docDataMap.get("source");
							resultData.put(Constants.SOURCE, source);
						}

						// 发表时间
						if (docDataMap.containsKey("newsTime")) {
							String newsTime = (String) docDataMap.get("newsTime");
							resultData.put(Constants.POST_TIME, newsTime);
						}

						// 内容
						if (docDataMap.containsKey("contentData")) {
							Map<String, Object> contentDataMap = (Map<String, Object>) docDataMap.get("contentData");
							if (contentDataMap.containsKey("contentList")) {
								List contentList = (List) contentDataMap.get("contentList");
								if (contentList != null && !contentList.isEmpty()) {
									Map contentMap = (Map) contentList.get(0);
									if (contentMap.containsKey("data")) {
										String content = (String) contentMap.get("data");
										content = content.replaceAll("<.*?>", "");
										resultData.put(Constants.CONTENT, content);
									}
								}
							}
						}

						// 评论链接
						if (docDataMap.containsKey("commentUrl")) {
							Map<String, Object> commentTask = new HashMap<String, Object>();
							String commentUrl = (String) docDataMap.get("commentUrl");
							String commUrl = new StringBuffer()
									.append("https://comment.ifeng.com/get.php?orderby=&docUrl=").append(commentUrl)
									.append("&format=js&job=1&pageSize=10&p=1").toString();
							commentTask.put(Constants.LINK, commUrl);
							commentTask.put(Constants.RAWLINK, commUrl);
							commentTask.put(Constants.LINKTYPE, "newscomment");
							resultData.put(Constants.COMMENT_URL, commUrl);
							tasks.add(commentTask);
							resultData.put(Constants.TASKS, tasks);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					LOG.info("data after regex failed to  tranform to json: url is " + url);
				}
			}
		} else {
			/**
			 * @param cate
			 * @function 多模板cate来源结构标准化 eg: "[凤凰网科技 > 通信 > 正文]";
			 */

			if (resultData.containsKey(Constants.CATE)) {
				List oldCateList = (List) resultData.get(Constants.CATE);
				String oldCatestr = oldCateList.get(0).toString();
				List newCateList = new ArrayList<String>();
				String[] newCate = oldCatestr.split(">");
				for (String cate : newCate) {
					newCateList.add(cate.trim());
				}
				resultData.put(Constants.CATE, newCateList);
			}

			/**
			 * @param author
			 * @function 作者字段格式化 eg:"author": "作者：铁流",
			 */
			if (resultData.containsKey(Constants.AUTHOR)) {
				String oldAuthor = (String) resultData.get(Constants.AUTHOR);
				if (oldAuthor != null && !oldAuthor.equals("")) {
					if (oldAuthor.contains("：")) {
						int index = oldAuthor.indexOf("：");
						String newAuthor = oldAuthor.substring(index + 1, oldAuthor.length());
						resultData.put(Constants.AUTHOR, newAuthor);
					} else {
						resultData.put(Constants.AUTHOR, oldAuthor);
					}

				}
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
			 * @param source
			 * @function 来源字段格式化 eg:来源：新华社
			 */
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源")) {
					source = source.replace("来源：", "");
					resultData.put(Constants.SOURCE, source);
				}
			}

			/**
			 * @param comment_url评论链接
			 * 
			 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
			 */

			/**
			 * http://comment.ifeng.com/get?job=1&order=DESC&orderBy=create_time
			 * & format
			 * =js&pagesize=10&p=1&docUrl=http%3A%2F%2Fblog.ifeng.com%2Farticle
			 * %2F34690957.html
			 */

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
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}