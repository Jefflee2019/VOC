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
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:硅谷动力 (Benet)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BenetPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BenetPostRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				for (int i = 0; i < replys.size(); i++) {
					Map<String, Object> replyData = replys.get(i);

					/**
					 * 楼层数 replyfloor": "2# 如果帖子页包含楼主，就是1楼即楼主，则删除1楼
					 * 
					 * @function 去掉 "#"
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData.get(Constants.REPLYFLOOR);
						// 去掉楼主回复层
						if (!oldReplyfloor.equals("") && oldReplyfloor.equals("楼主")) {
							replys.remove(replyData);
							i--;
						}
						if (!oldReplyfloor.equals("") && oldReplyfloor.contains("#")) {
							String newReplyfloor = oldReplyfloor.replace("#", "").trim();
							replyData.put(Constants.REPLYFLOOR, newReplyfloor);
						}
					}

					/**
					 * 回复内容 "contents": ": 小伙伴们听说了吗"
					 */
					String oldReplycontent = "";
					if (replyData.containsKey(Constants.REPLYCONTENT)) {
						oldReplycontent = (String) replyData.get(Constants.REPLYCONTENT);
					}
					if (!oldReplycontent.equals("") && oldReplycontent.startsWith(":")) {
						replyData.put("oldReplycontent", oldReplycontent);
						String newReplycontent = oldReplycontent.substring(1, oldReplycontent.length()).trim();
						replyData.put(Constants.REPLYCONTENT, newReplycontent);
					}

					/**
					 * 发表时间 reply_date:"发表于2015-07-22"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String oldReplydate = (String) replyData.get(Constants.REPLYDATE);
						if (!oldReplydate.equals("")) {
							String newReplydate = oldReplydate.replace("发表于", "");
							newReplydate = ConstantFunc.convertTime(newReplydate);
							replyData.put(Constants.REPLYDATE, newReplydate);
						}
					}
				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * 删掉 首页外的主贴重复信息：contents、author、newstime
		 */
		// 获取当前页url
		String currentUrl = unit.getUrl();
		String pagedata = unit.getPageData();
		String nextPage = null;
		/**
		 * deal nextpage循环翻页问题
		 */
		if (pagedata.contains(">下一页<")) {
			if (!currentUrl.contains("&page")) {
				nextPage = currentUrl + "&page=2";
			} else {
				Pattern ptn = Pattern.compile("page=(\\d+)");
				Matcher match = ptn.matcher(currentUrl);
				if (match.find()) {
					int currentPage = Integer.parseInt(match.group(1));
					int page = currentPage + 1;
					nextPage = currentUrl.replaceAll("page=" + currentPage, "page=" + page);
				}
			}
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put(Constants.LINK, nextPage);
			nextpageTask.put(Constants.RAWLINK, nextPage);
			nextpageTask.put(Constants.LINKTYPE, "bbspost");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, nextPage);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.clear();
				tasks.add(nextpageTask);
			}
		} else {
			List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
			// 如果没有下一页，就清空任务
			tasks.clear();
			if (resultData.containsKey(Constants.NEXTPAGE)) {
				resultData.remove(Constants.NEXTPAGE);
			}
		}

		/**
		 * 去掉回复贴中的author、newstime、contents
		 */
		Matcher match = Pattern.compile("page=(\\d+)").matcher(currentUrl);
		if (match.find()) {
			int pageindex = Integer.parseInt(match.group(1));
			if (pageindex > 1 && resultData.containsKey(Constants.NEWSTIME) | resultData.containsKey(Constants.AUTHOR)
					| resultData.containsKey(Constants.CONTENTS)) {
				resultData.remove(Constants.NEWSTIME);
				resultData.remove(Constants.AUTHOR);
				resultData.remove(Constants.CONTENTS);
			}
		}

		/**
		 * 发表时间 标准化 "newstime": "发表于 2015-6-2 11:33:05"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String oldNewstime = (String) resultData.get(Constants.NEWSTIME);
			if (!oldNewstime.equals("")) {
				String newstime = oldNewstime.replace("发表于", "");
				newstime = ConstantFunc.convertTime(newstime);
				resultData.put(Constants.NEWSTIME, newstime);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}