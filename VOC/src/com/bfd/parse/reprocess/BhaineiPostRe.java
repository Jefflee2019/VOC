package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:海内社区
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BhaineiPostRe implements ReProcessor {

	@SuppressWarnings({ "unchecked" })
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
					 * 楼层数 replyfloor": "5#
					 * 
					 * @function 去掉非数字符号
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String replyfloor = (String) replyData.get(Constants.REPLYFLOOR);
						if (replyfloor.equals("沙发")) {
							replyfloor = "2";
						} else if (replyfloor.equals("板凳")) {
							replyfloor = "3";
						} else if (replyfloor.equals("地板")) {
							replyfloor = "4";
						} else {
							String floorRex = "(\\d+)(.*)";
							replyfloor = regex(replyfloor, floorRex);
						}
						replyData.put(Constants.REPLYFLOOR, replyfloor);
					}

					/**
					 * 回复时间 "replydate": "发表于 10 小时前" 或"发表于 2017/03/19 12:42:42"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replydate = (String) replyData.get(Constants.REPLYDATE);
						if(replydate.contains("发表于")) {
							replydate = replydate.replace("发表于", "").trim();
						}
						replydate = ConstantFunc.convertTime(replydate).trim();
						replyData.put(Constants.REPLYDATE, replydate);

					}

				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * 发表时间 标准化 "newstime": "10 小时前"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			if(newstime.contains("发布于：")) {
				newstime = newstime.replace("发布于：", "").trim();
			}
			newstime = ConstantFunc.convertTime(newstime);
			resultData.put(Constants.NEWSTIME, newstime);
		}

		/**
		 * 回复数处理 replycount": "17 位用户参与讨论
		 */
		if (resultData.containsKey(Constants.REPLYCOUNT)) {
			String replyCount = resultData.get(Constants.REPLYCOUNT).toString();
			String countRex = "(\\d+)(\\W*)";
			replyCount = regex(replyCount, countRex);
			resultData.put(Constants.REPLYCOUNT, replyCount);
		}

		/**
		 * 浏览数处理"view_cnt": "2962 次浏览"
		 */
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			String viewCnt = resultData.get(Constants.VIEW_CNT).toString();
			String viewRex = "(\\d+)(\\W*)";
			viewCnt = regex(viewCnt, viewRex);
			resultData.put(Constants.VIEW_CNT, viewCnt);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String regex(String replyfloor, String floorRex) {
		Pattern pattern = Pattern.compile(floorRex);
		Matcher matcher = pattern.matcher(replyfloor);
		if (matcher.find()) {
			replyfloor = replyfloor.replace(matcher.group(), matcher.group(1));
		}
		return replyfloor;
	}

}
