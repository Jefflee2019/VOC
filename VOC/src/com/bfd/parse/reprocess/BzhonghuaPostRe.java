package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 中华网论坛帖子页 后处理插件
 * 
 * @author bfd_05
 *
 */
public class BzhonghuaPostRe implements ReProcessor {

	private static final Pattern PNUM = Pattern.compile("\\d+");
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s([0-9]{2}:[0-9]{2})*");
	private static final Pattern NEXTPAGEPAT = Pattern.compile("_(\\d+).html");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		// 最后一个节点的路径没有取到,要取全部路径然后处理
		if (resultData.containsKey(Constants.VIEW_CNT) && !resultData.containsKey(Constants.REPLYCOUNT)) {
			String views = resultData.get(Constants.VIEW_CNT).toString();
			if (views.contains("（") && views.indexOf("（") >= 0 && views.indexOf("）") >= 0
					&& (views.indexOf("（") < views.indexOf("）"))) {
				String viewCnt = views.substring(views.indexOf("（") + 1, views.indexOf("）"));
				String replycount = views.substring(views.lastIndexOf("（") + 1, views.lastIndexOf("）"));
				resultData.put(Constants.VIEW_CNT, viewCnt);
				resultData.put(Constants.PARTAKE_CNT, replycount);
				resultData.put(Constants.REPLYCOUNT, replycount);
			}
		}
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			if (cateList.get(0).contains(">")) {
				String[] cates = cateList.get(0).replace("当前位置:", "").split(">");
				List<String> newCates = new ArrayList<String>();
				for (String cate : cates) {
					newCates.add(cate.trim());
				}
				resultData.put(Constants.CATE, newCates);
			}
		}
		if (resultData.containsKey(Constants.NEWSTIME)) {
			parseByReg(resultData, Constants.NEWSTIME, PATTIME);
		}
		if (resultData.containsKey(Constants.CONTENTS)) {
			resultData.put(Constants.CONTENTS, ConstantFunc.replaceBlank((String) resultData.get(Constants.CONTENTS)));
		}
		if (resultData.containsKey(Constants.AUTHOR) && resultData.get(Constants.AUTHOR) instanceof List) {
			List tempList = (List) resultData.get(Constants.AUTHOR);
			if (tempList.get(0) instanceof Map) {
				for (int i = 0; i < tempList.size(); i++) {
					Map<String, Object> author = (Map<String, Object>) tempList.get(i);
					if (author.containsKey(Constants.FORUM_SCORE)) {
						parseByReg(author, Constants.FORUM_SCORE, PNUM);
					}
					if (author.containsKey(Constants.AUTHOR_LEVEL)) {
						String authorLevel = ((String) author.get(Constants.AUTHOR_LEVEL)).replace("级别：", "");
						author.put(Constants.AUTHOR_LEVEL, authorLevel);
					}
				}
			} else if (tempList.get(0) instanceof String) {
				String author = tempList.get(0).toString();
				resultData.put(Constants.AUTHOR, author);
			}
		}
		List<Map<String, Object>> replys = new ArrayList<>();
		if (resultData.containsKey(Constants.REPLYS)) {
			Object obj = resultData.get(Constants.REPLYS);
			if (obj instanceof List) {
				replys = (List<Map<String, Object>>) obj;
				for (Map<String, Object> reply : replys) {
					if (reply.containsKey(Constants.REPLYDATE)) {
						parseByReg(reply, Constants.REPLYDATE, PATTIME);
					}
					if (reply.containsKey(Constants.REPLYFLOOR)) {
						parseByReg(reply, Constants.REPLYFLOOR, PNUM);
						matchUpcnt(pageData, reply);
					}
					if (reply.containsKey(Constants.REPLY_FORUM_SCORE)) {
						parseByReg(reply, Constants.REPLY_FORUM_SCORE, PNUM);
					}
					if (reply.containsKey(Constants.REPLY_LEVEL)) {
						String replyLevel = ((String) reply.get(Constants.REPLY_LEVEL)).replace("级别：", "");
						reply.put(Constants.REPLY_LEVEL, replyLevel);
					}
					if (reply.containsKey(Constants.REPLYCONTENT)) {
						reply.put(Constants.REPLYCONTENT,
								ConstantFunc.replaceBlank((String) reply.get(Constants.REPLYCONTENT)));
					}
				}
			}
		}
		List<Map<String, Object>> tasks = null;
		if (resultData.containsKey("tasks")) {
			tasks = (List<Map<String, Object>>) resultData.get("tasks");
		} else {
			tasks = new ArrayList<Map<String, Object>>();
			resultData.put("tasks", tasks);
		}
		Map<String, Object> nextpMap = new HashMap<String, Object>();
		String url = unit.getTaskdata().get("url").toString();
		String[] urls = url.split("_");
		if (urls.length > 1) {
			if (resultData.containsKey(Constants.REPLYCOUNT) && replys.size() == 49) {
				int replyCount = Integer.valueOf(resultData.get(Constants.REPLYCOUNT).toString());
				int totalPage = replyCount % 49 == 0 ? replyCount / 49 : replyCount / 49 + 1;
				Matcher mch = NEXTPAGEPAT.matcher(url);
				if (mch.find()) {
					int pageIndex = Integer.valueOf(mch.group(1));
					if (pageIndex < totalPage) {
						String nextpage = urls[0] + "_" + (pageIndex + 1) + ".html";
						resultData.put(Constants.NEXTPAGE, nextpage);
						nextpMap.put("link", nextpage);
						nextpMap.put("rawlink", nextpage);
						nextpMap.put("linktype", "bbspost");
						tasks.add(nextpMap);
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 匹配获取楼层顶赞数
	 * 
	 * @param pageData
	 * @param reply
	 */
	private void matchUpcnt(String pageData, Map<String, Object> reply) {
		String mchStr = "<a\\s+target=\"_self\" href=\"javascript:showquickimpeachframe\\(\\d+,\\d+,(\\d+),%s\\)\">";
		mchStr = String.format(mchStr, reply.get(Constants.REPLYFLOOR));
		Pattern upCntPat = Pattern.compile(mchStr);
		Matcher mch = upCntPat.matcher(pageData);

		if (mch.find()) {
			String msgID = mch.group(1);
			String s = "messageid:'%s',ding:'(\\d+)'";
			Pattern getDing = Pattern.compile(String.format(s, msgID));
			Matcher upCntMch = getDing.matcher(pageData);
			if (upCntMch.find()) {
				reply.put(Constants.UP_CNT, upCntMch.group(1));
			} else {
				reply.put(Constants.UP_CNT, 0);
			}
		}
	}

	public void parseByReg(Map<String, Object> dataMap, String conststr, Pattern p) {
		String resultStr = (String) dataMap.get(conststr);
		Matcher mch = p.matcher(resultStr);
		if (mch.find()) {
			resultStr = mch.group(0);
		}
		dataMap.put(conststr, resultStr);
	}
}
