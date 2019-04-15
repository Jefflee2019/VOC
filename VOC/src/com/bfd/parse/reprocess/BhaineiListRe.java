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
 * @site:海内社区-论坛
 * @function 处理列表页下一页，发表时间，回复数
 * 
 * @author bfd_02
 *
 */

public class BhaineiListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhaineiListRe.class);

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
		String url = unit.getUrl();
		// 两个列表页(站内搜索和版块)分别处理
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (url.contains("http://so.hainei.org/")) {
				for (int i = 0; items != null && i < items.size(); i++) {
					Map<String, Object> item = items.get(i);
					// 处理发表时间 "发布：2017-01-19"
					if (item.containsKey(Constants.POSTTIME)) {
						String postTime = item.get(Constants.POSTTIME).toString();
						postTime = postTime.replace("发布：", "").trim();
						item.put(Constants.POSTTIME, postTime);
					}
					// 处理回复数 "回复：21"或是""
					if (item.containsKey(Constants.REPLY_CNT)) {
						String replyCnt = item.get(Constants.REPLY_CNT).toString();
						if (replyCnt.equals("")) {
							replyCnt = "0";
						} else if (replyCnt.contains("回复")) {
							replyCnt = replyCnt.replace("回复：", "").toString();
						}
						item.put(Constants.REPLY_CNT, replyCnt);
					}
				}
			} else {
				// 处理发表时间"1 小时前"
				for (int i = 0; items != null && i < items.size(); i++) {
					Map<String, Object> item = items.get(i);
					if (item.containsKey(Constants.POSTTIME)) {
						String postTime = item.get(Constants.POSTTIME).toString();
						postTime = ConstantFunc.convertTime(postTime);
						item.put(Constants.POSTTIME, postTime);
					}
				}
			}
		}

		/**
		 * item:处理下一页 note：版块列表页模板标定，不需要处理，而站内搜索在76页时会循环到第1页
		 * http://so.hainei.org/cse/search?q=%E5%8D%
		 * 8E%E4%B8%BA&p=0&s=13490577094485005644&srt=cse_createTime
		 */
		String pagedata = unit.getPageData();
		if (url.contains("http://so.hainei.org/") && pagedata.contains("下一页")) {
			Matcher match = Pattern.compile("&p=(\\d+)&").matcher(url);
			int pagenum = 0;
			if (match.find()) {
				pagenum = Integer.parseInt(match.group(1));
			}
			if (pagenum < 74) {
				String nextpage = url.replace("&p=" + pagenum, "&p=" + (pagenum + 1));
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
