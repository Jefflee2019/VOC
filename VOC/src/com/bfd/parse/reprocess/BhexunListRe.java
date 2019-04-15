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
 * @site:和讯网-论坛
 * @function 论坛列表页后处理插件 处理没有回复的帖子，以实现增量抓取
 * 
 * @author bfd_02
 *
 */

public class BhexunListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhexunListRe.class);

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

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			String tempUrl = "";
			for (int i = 0; items != null && i < items.size(); i++) {
				Map<String, Object> item = items.get(i);
				// reply_cnt "---/---"
				//如果回复数不显示，则删除该贴
				if (item.containsKey(Constants.REPLY_CNT)) {
					String oldreplyCnt = item.get(Constants.REPLY_CNT).toString();
					Matcher match = Pattern.compile("(\\d+)/").matcher(oldreplyCnt);
					if (match.find()) {
						int reply_cnt = Integer.parseInt(match.group(1));
						item.put(Constants.REPLY_CNT, reply_cnt);
					} else {
						Map<String, String> itemMap = (Map<String, String>) item.get(Constants.ITEMLINK);
						tempUrl = tempUrl + itemMap.get(Constants.LINK) + ",";
						items.remove(item);
						i--;
					}
				}
				// 标准化发表时间 "15-12-26 12:02"
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = item.get(Constants.POSTTIME).toString();
					posttime = "20" + posttime;
					item.put(Constants.POSTTIME, posttime);
				}
			}
			for (int i = 0; tasks != null && i < tasks.size(); i++) {
				Map<String, Object> task = tasks.get(i);
				if (task.containsKey(Constants.LINK)) {
					String link = task.get(Constants.LINK).toString();
					if (tempUrl.contains(link)) {
						tasks.remove(task);
						i--;
					}
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
