package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
//import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;

/**
 * Bxcar
 * 
 * @function：标准化部分字段
 * @author BFD_06
 */
public class BmydigitPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
				.get(Constants.TASKS);
		List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
				.get(Constants.REPLYS);
		String url = unit.getUrl();
		/**
		 * 第一类模板类型
		 */
		if (!url.contains("?tid")) {
			List<String> cate = (List<String>) resultData.get(Constants.CATE);
			int lashIndex = cate.size() - 1;
			if (lashIndex >= 0 && cate.get(lashIndex).equals("[打印本页]")) {
				cate.remove(lashIndex);
			}
			int totalPageNum = match("Pages: \\( (\\d+) total \\)",
					unit.getPageData());
			int pageNum = match("_(\\d+).html", url);
			// 添加下一页
			if(pageNum==0 && pageNum<totalPageNum){
				addNextUrl(url.replace(".html", "_2.html"), rtasks, resultData);
				ParseUtils.getIid(unit, result);
			} else if(pageNum!=0&&pageNum<totalPageNum){
				addNextUrl(
						url.replace("_" + pageNum + ".html", "_"
								+ (pageNum + 1) + ".html"), rtasks, resultData);
				ParseUtils.getIid(unit, result);
			}
			// 格式化不同页面字段
			if (pageNum == 0 || pageNum == 1)
				deleteAttr(replys);
			else
				deleteAttr(resultData);
			/**
			 * 第二类模板类型
			 */
		} else {
			/*// NEWSTIME
			if (resultData.containsKey(Constants.NEWSTIME)) {
				formatAttr(Constants.NEWSTIME,
						(String) resultData.get(Constants.NEWSTIME), resultData);
			}*/
			// REPLYS
			if (resultData.containsKey(Constants.REPLYS)) {
				int replycount = Integer.parseInt((String) resultData
						.get(Constants.REPLYCOUNT)); // 总回复数
				// 添加下一页
				int pageNum = match("&page=(\\d+)", url);
				if (pageNum == 0 && replycount > 10) { //第一页
					addNextUrl(url + "&page=2", rtasks, resultData);
					ParseUtils.getIid(unit, result);
				} else if (pageNum != 0 && pageNum < Math.ceil(replycount / 10)) { //非第一页
					addNextUrl(
							url.replace("page=" + pageNum, "page="
									+ (pageNum + 1)), rtasks, resultData);
					ParseUtils.getIid(unit, result);
				}
				// 格式化不同页面字段
				if (pageNum == 0 || pageNum == 1)
					deleteAttr(replys);
				else
					deleteAttr(resultData);
				// 标准化页面字段
				for (Map<String, Object> reply : replys) {
					// REPLYFLOOR
					formatAttr(Constants.REPLYFLOOR,
							(String) reply.get(Constants.REPLYFLOOR), reply);
					/*// REPLYDATE
					formatAttr(Constants.REPLYDATE,
							(String) reply.get(Constants.REPLYDATE), reply);*/
					// REPLY_FORUM_MONEY
					formatAttr(Constants.REPLY_FORUM_MONEY,
							(String) reply.get(Constants.REPLY_FORUM_MONEY),
							reply);
				}
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		if (keyName.equals(Constants.REPLYFLOOR)) {
			value = value.replace("楼", "");
			result.put(keyName, Integer.parseInt(value));
		} else if (keyName.equals(Constants.REPLYDATE)
				|| keyName.equals(Constants.NEWSTIME)) {
//			value = value.substring(value.indexOf(":") + 2);
			// Calendar c = Calendar.getInstance();
			// result.put(keyName, c.get(Calendar.YEAR) + "-" + value);
//			value = ConstantFunc.convertTime(value);
//			result.put(keyName, value);
		} else if (keyName.equals(Constants.REPLY_FORUM_MONEY)) {
			result.put(keyName, Integer.parseInt(value));
		}
	}

	public int match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 0;
	}

	public void deleteAttr(Map<String, Object> resultData) {
		resultData.remove(Constants.AUTHOR);
		resultData.remove(Constants.CONTENTS);
		resultData.remove(Constants.NEWSTIME);
	}

	public void deleteAttr(List<Map<String, Object>> replys) {
		if (replys != null) {
			replys.remove(0);
		}
	}

	public void addNextUrl(String nextUrl, List<Map<String, Object>> rtasks,
			Map<String, Object> resultData) {
		Map<String, Object> rtask = new HashMap<String, Object>();
		rtask.put("link", nextUrl);
		rtask.put("rawlink", nextUrl);
		rtask.put("linktype", "bbspost");
		rtasks.add(rtask);
		resultData.put(Constants.NEXTPAGE, nextUrl);
	}

}
