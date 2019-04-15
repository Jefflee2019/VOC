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
 * @site Bmacx
 * @function 帖子列表页后处理
 * @author bfd_02
 * 
 */
public class BmacxListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BmacxListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				/**
				 * 发表时间格式化 (1 小时前)
				 */
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = (String) item.get(Constants.POSTTIME);
					posttime = posttime.replace("(", "").replace(")", "").trim();
					// (12-7-12 15:19:41)
					Matcher match = Pattern.compile("\\d{2}-\\d{1,2}-\\d{1,2}\\s*\\d+:\\d+(:\\d+)?").matcher(posttime);
					if (match.find()) {
						posttime = 20 + posttime;
					} else {
						posttime = ConstantFunc.convertTime(posttime);
					}
					item.put(Constants.POSTTIME, posttime);
				}

				/**
				 * 回复数 (15)
				 */
				if (item.containsKey(Constants.REPLY_CNT)) {
					String replyCnt = item.get(Constants.REPLY_CNT).toString();
					if (replyCnt.contains("(")) {
						replyCnt = replyCnt.replace("(", "").replace(")", "").trim();
						item.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt));
					}
				}
			}
		}
		
		// 控制翻页深度
		String pageData = unit.getPageData();
		if (pageData.contains("下一页")) {
			String url = unit.getUrl();
			// https://www.macx.cn/forum-888-2.html
			Matcher match = Pattern.compile("-(\\d+).html").matcher(url);
			if (match.find()) {
				int pageCnt = Integer.parseInt(match.group(1));
				/**
				 * 统一部分列表页翻页url不规格 https://www.macx.cn/forum-4000-1.html
				 * ->forum-news-2.html
				 */
				String nextPage = url.replace("-" + pageCnt + ".html", "-" + (pageCnt + 1) + ".html");
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextPage);
				nextpageTask.put("rawlink", nextPage);
				nextpageTask.put("linktype", "bbspostlist");
				resultData.put("nextpage", nextPage);
				tasks.add(nextpageTask);
				
				// 限制为50页
				if (pageCnt >= 50) {
					// 删除下一页链接
					resultData.remove("nextpage");
					// 清楚task中的下一页任务
					tasks.clear();
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
