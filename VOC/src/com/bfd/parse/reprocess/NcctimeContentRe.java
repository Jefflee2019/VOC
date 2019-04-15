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
 * @sie:飞象网新闻 (Ncctime)
 * @function 新闻内容页后处理插件 处理摘要，路径，来源，发表时间
 * 
 * @author bfd_02
 *
 */

public class NcctimeContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcctimeContentRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未获取到解析数据");
			return null;
		}

		/**
		 * @param source post_time author
		 * @function 格式化字段 eg:"2015年8月5日 11:09 CCTIME飞象网 作 者：于斌"
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldData = (String) resultData.get(Constants.POST_TIME);
			// 字符串0索引隐含了一个":"
			String firstIndex = oldData.charAt(0) + "";
			if (firstIndex.equals(":")) {
				oldData = oldData.substring(1, oldData.length()).trim();
			}
			if (oldData != null && !oldData.isEmpty()) {
				// 处理少量blog模板 "上一篇 / 下一篇 2009-07-13 09:04:03
				if (oldData.contains("上一篇 / 下一篇")) {
					Matcher match = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s*[\\d:]+").matcher(oldData);
					if (match.find()) {
						String postTime = match.group();
						resultData.put(Constants.POST_TIME, postTime);
					}
				} else {
					String[] dataarr = oldData.split(" ");
					if (oldData.contains("作 者")) {
						resultData.put(Constants.POST_TIME, dataarr[0] + " " + dataarr[1]);
						resultData.put(Constants.SOURCE, dataarr[2]);
						String author = dataarr[4].replace("者：", "");
						resultData.put(Constants.AUTHOR, author);
					} else {
						resultData.put(Constants.POST_TIME, dataarr[0] + " " + dataarr[1]);
						resultData.put(Constants.SOURCE, dataarr[2]);
						if (resultData.containsKey(Constants.AUTHOR)) {
							resultData.remove(Constants.AUTHOR);
						}
					}
				}
			}
		}

		/**
		 * @param cate
		 * @function 格式化字段 eg2:[首页 >> 制造 >> 正文]
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object oldCate = resultData.get(Constants.CATE);
			if (oldCate instanceof List) {
				List cateList = (ArrayList<String>) oldCate;
				// 存放处理之后的cate
				List newCateList = new ArrayList<String>();
				if (cateList.size() == 1) {
					String cateTem = cateList.get(0).toString();
					// 去掉隐含的":"
					if (cateTem.contains(":")) {
						cateTem = cateTem.replace(":", "");
					}
					if (cateTem.contains(">>")) {
						String[] catearr = cateTem.split(">>");
						for (String cate : catearr) {
							newCateList.add(cate.trim());
						}
						resultData.put(Constants.CATE, newCateList);
					}
				} else {
					resultData.put(Constants.CATE, cateList);
				}
			}
		}

		/**
		 * @param brief
		 * @function 去掉包含在content里面的brief部分
		 */
		if (resultData.containsKey(Constants.BRIEF)) {
			// 新闻内容
			String content = resultData.get(Constants.CONTENT).toString();
			String brief = resultData.get(Constants.BRIEF).toString();
			content = content.replace(brief, "");

			resultData.put(Constants.CONTENT, content);
		}

		// 处理 少量blog模板的查看数
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			String oldViewCnt = resultData.get(Constants.VIEW_CNT).toString();
			Matcher match = Pattern.compile("(\\d+)").matcher(oldViewCnt);
			if (match.find()) {
				int viewCnt = Integer.parseInt(match.group(1));
				resultData.put(Constants.VIEW_CNT, viewCnt);
			}
		}

		// 处理有些模板title前的":"
		if (resultData.containsKey(Constants.TITLE)) {
			String oldTitle = (String) resultData.get(Constants.TITLE);
			// 字符串0索引隐含了一个":"
			String firstIndex = oldTitle.charAt(0) + "";
			if (firstIndex.equals(":")) {
				oldTitle = oldTitle.substring(1, oldTitle.length()).trim();
				resultData.put(Constants.TITLE, oldTitle);
			}
		}
		// http://www.skdjfiefjlsjflk(_2).htm
		// 处理有的页面内容有多页而没有提供下一页的情况，如果有多页，则添加一个下一页
		// 获取当前url
		String url = unit.getUrl();
		Pattern pattern = Pattern.compile("(http://\\w+\\.\\w+\\.com/.*?/[\\d\\-]+/[\\w\\-\\_]+)(.htm)?");
		Matcher match = pattern.matcher(url);
		// 获取页面源码
		String pagedata = unit.getPageData();
		// 当前页码
		int currPageid = 0;
		// 下一页页码
		int nextPageid = 0;
		// 如果url包含"_"，说明有多页，并且当前url有页码
		if (url.contains("_")) {
			Matcher m = Pattern.compile("\\d+_(\\d+)").matcher(url);
			if (m.find()) {
				currPageid = Integer.parseInt(m.group(1));
				nextPageid = currPageid + 1;
				// 如果不包含"_"，则认定当前页为第一页
			}
		} else {
			currPageid = 1;
			nextPageid = 2;
		}
		String nextpageptn = "[" + nextPageid + "]</a>";
		// 如果有多页页
		if (pagedata.contains(nextpageptn)) {
			if (match.find()) {
				String nextpageHead = match.group(1);
				String nextpage = null;
				if (match.group(2) != null) {
					String nextpageBack = match.group(2);
					nextpage = nextpageHead + "_" + nextPageid + nextpageBack;
				} else {
					nextpage = nextpageHead + "_" + nextPageid;
				}
				Map nextpageTask = new HashMap();

				nextpageTask.put(Constants.LINK, nextpage);
				nextpageTask.put(Constants.RAWLINK, nextpage);
				nextpageTask.put(Constants.LINKTYPE, "newscontent");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.NEXTPAGE, nextpage);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(nextpageTask);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}