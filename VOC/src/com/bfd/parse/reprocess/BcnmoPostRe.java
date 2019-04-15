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
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:手机中国 (Bcnmo)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_04
 *
 */

public class BcnmoPostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BcnmoPostRe.class);
	private static final Pattern NEXTPAGE_PATTERN = Pattern.compile("-(\\d+)-\\d+.html");
	private static Matcher tempMatcher = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.info("未获取到解析数据");
			return null;
		}

		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				for (Map replyData : replys) {
					/**
					 * 发表时间 reply_date:"发表于 2013-8-29 18:43:38"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replyDate = (String) replyData.get(Constants.REPLYDATE);
						replyDate = replyDate.replace("发表于", "").trim();
						replyData.put(Constants.REPLYDATE, replyDate);
					}

					/**
					 * 楼层数 replyfloor": "2#
					 * 
					 * @function 去掉 "#"
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData.get(Constants.REPLYFLOOR);
						if ("".equals(oldReplyfloor)) {
							if (replys.indexOf(replyData) == 0) {
								oldReplyfloor = "2";
							}
							if (replys.indexOf(replyData) == 1) {
								oldReplyfloor = "3";
							}
							if (replys.indexOf(replyData) == 2) {
								oldReplyfloor = "4";
							}
						}else{
							oldReplyfloor = oldReplyfloor.replace("#", "").trim();
						}
						replyData.put(Constants.REPLYFLOOR, oldReplyfloor);
					}
				}
			}
		}

		/**
		 * 生成下一页
		 */
		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty() && pageData.contains("下一页")) {
			String url = unit.getUrl();
			tempMatcher = NEXTPAGE_PATTERN.matcher(url);
			if (tempMatcher.find()) {
				String pageIndex = tempMatcher.group(1);
				int nextIndex = Integer.parseInt(pageIndex) + 1;
				String[] urlArr = url.split("-");
				// http://bbs.cnmo.com/thread-12423430-1-1.html
				if (urlArr.length > 3) {
					StringBuilder sb = new StringBuilder();
					sb.append(urlArr[0]).append("-").append(urlArr[1])
							.append("-").append(nextIndex).append("-")
							.append(urlArr[3]);
					url = sb.toString();
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", url);
					nextpageTask.put("rawlink", url);
					nextpageTask.put("linktype", "bbspost");// 任务为列表页
					resultData.put("nextpage", url);
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
					if (tasks.size() == 0 || tasks == null) {
						tasks = new ArrayList<Map<String, Object>>();
						resultData.put("tasks", tasks);
					}
					tasks.add(nextpageTask);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
