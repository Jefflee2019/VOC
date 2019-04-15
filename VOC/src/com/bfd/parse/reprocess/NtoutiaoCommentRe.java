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
 * @site：今日头条
 * @function：给出评论页下一页
 * @author bfd_04
 */

public class NtoutiaoCommentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NtoutiaoCommentRe.class);
	private static final Pattern PATTERN = Pattern.compile("offset=(\\d+)");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		String postTime = url.substring(url.lastIndexOf("#") + 1);
		String year = null;
		if (!postTime.equals(""))
			year = postTime.split("-")[0];
		if (resultData != null && resultData.containsKey("comments")) {
			List<Map<String, Object>> commentslist = (List<Map<String, Object>>) resultData
					.get("comments");
			if (null != commentslist && !commentslist.isEmpty()) {
				for (Object obj : commentslist) {
					Map tempMap = (Map) obj;
					if (tempMap.containsKey(Constants.COMMENT_TIME)) {
						String commentTime = tempMap
								.get(Constants.COMMENT_TIME).toString();
						if (year != null)
							commentTime = year + '-' + commentTime;
						tempMap.put(Constants.COMMENT_TIME, commentTime);
					}
				}
			}
			// deal with nextpage
			Matcher match = PATTERN.matcher(url);
			if (commentslist.size() >= 10 && match.find()) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				String oldOffset = match.group(1);
				int newOffset = Integer.parseInt(oldOffset) + 10;
				String nextpage = url.replace("offset=" + oldOffset, "offset="
						+ String.valueOf(newOffset));
				try {
					commentTask.put("link", nextpage);
					commentTask.put("rawlink", nextpage);
					commentTask.put("linktype", "newscomment");
					if (resultData != null && resultData.size() > 0) {
						resultData.put(Constants.NEXTPAGE, nextpage);
						List<Map> tasks = (List<Map>) resultData
								.get(Constants.TASKS);
						if (tasks == null) {
							tasks = new ArrayList<Map>();
							resultData.put(Constants.TASKS, tasks);
						}
						tasks.add(commentTask);
					}
				} catch (Exception e) {
					LOG.debug(e.toString());
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
