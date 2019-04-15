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
 * @site:站长之家-论坛(Bzhanzhang)
 * @function 论坛列表页后处理插件 格式化回复数和发表时间，以及解决下一页循环翻页问题
 * 
 * @author bfd_02
 *
 */

public class BzhanzhangListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BzhanzhangListRe.class);

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

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (items != null && !items.isEmpty()) {
				for (Map<String, Object> item : items) {
					// cal reply_cnt "回复：131"
					if (item.containsKey(Constants.REPLY_CNT)) {
						// 如果reply_cnt 为"",表示帖子没有回复，设为-1024
						String replyCnt = item.get(Constants.REPLY_CNT).toString();
						if (!replyCnt.equals("")) {
							replyCnt = replyCnt.replace("回复：", "").trim();
							item.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt));
						} else {
							item.put(Constants.REPLY_CNT, 0);
						}
//						列表页帖子新回复不会将帖子顶到最前面，无法实现增量
						
					}
					// 标准化发表时间 "发布：2015-09-18"
					if (item.containsKey(Constants.POSTTIME)) {
						String postTime = item.get(Constants.POSTTIME).toString();
						postTime = postTime.replace("发布：", "").trim();
						item.put(Constants.POSTTIME, postTime);
					}
				}

			}
		}

		// cal nextpage
		String url = (String) unit.getTaskdata().get("url");
		Pattern ptn = Pattern.compile("&p=(\\d+)&");
		Matcher m = ptn.matcher(url);
		if (m.find()) {
			int p = Integer.parseInt(m.group(1));
			// p=74 对应的是第75页，下一页会循环到第1页
			if (p >= 74) {
				resultData.remove(Constants.NEXTPAGE);
				List tasks = (ArrayList) resultData.get(Constants.TASKS);
				for (int i = 0; i < tasks.size(); i++) {
					if (((Map) tasks.get(i)).get(Constants.LINKTYPE).equals("bbspostlist")) {
						tasks.remove(i);
					}
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
