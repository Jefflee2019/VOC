package com.bfd.parse.reprocess;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bdzwww 论坛帖子页
 *
 */
public class BdzwwwPostRe implements ReProcessor {
	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String totalNum = (String) resultData.get("totalpage");
			if(totalNum != null) {
				Matcher match = Pattern.compile("(\\d)").matcher(totalNum);
				if(match.find()) {
					String url = unit.getUrl();
					int curPageNum = Integer.valueOf(getPage(url)).intValue(); // 当前页数
					int pageTotal = Integer.valueOf(match.group(1)).intValue(); // 总页数
					if(curPageNum > 0 && curPageNum < pageTotal) { // 当前页数小于总页数
						Map<String, Object> nextpageTask = new HashMap<>();
						++curPageNum; // 得到下一页
						Pattern iidPatter = Pattern.compile("(http://club.dzwww.com/thread-\\d+-)(\\d+)(-\\d+.html)");
						match = iidPatter.matcher(url);
						if (match.find()) {
							String nextpage = match.group(1) + curPageNum + match.group(3);
							nextpageTask.put("link", nextpage);
							nextpageTask.put("rawlink", nextpage);
							nextpageTask.put("linktype", "bbspost");
							resultData.put("nextpage", nextpage);
							List<Object> tasks = (List<Object>) resultData.get("tasks");
							if (tasks != null) {
								tasks.clear(); // 只生成下一页任务
								tasks.add(nextpageTask);
							}
						}
					}
				} 
			}
			if (resultData.containsKey("view_cnt")) { // 有值说明在第一页，去除楼主信息
				List<String> list = (List<String>) resultData.get("replys");
				list.remove(0);
			} else {
				if (resultData.containsKey("author")) {
					resultData.remove("author");
				}
				if (resultData.containsKey("contents")) {
					resultData.remove("contents");
				}
				if (resultData.containsKey("newstime")) {
					resultData.remove("newstime");
				}
			}
			if (resultData.containsKey("replys")) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				for (Object obj : replys) {
					if ((obj instanceof Map)) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						if (reply.containsKey("replyfloor")) {
							String replyfloor = reply.get("replyfloor").toString();
							replyfloor = replyfloor.replace("楼主", "1").replace("沙发", "2").replace("板凳", "3")
									.replace("地板", "4").replace("地下室", "5").replace("楼", "").replace("#", "");
							reply.put("replyfloor", replyfloor);
						}
						if (reply.containsKey("replydate")) {
							reply.put("replydate", ConstantFunc.getDate((String) reply.get("replydate")));
						}
						if (reply.containsKey("replycontent")) {
							String replycontent = reply.get("replycontent").toString();
							reply.put("replycontent", replycontent);
						}
					}
				}
			}
			if (resultData.containsKey("newstime")) {
				resultData.put("newstime", ConstantFunc.getDate((String) resultData.get("newstime")));
			}
			if (resultData.containsKey("contents")) {
				String content = resultData.get("contents").toString();
				resultData.put("contents", content);
			}
//			if (resultData.containsKey("cate")) {
//				List<String> cate = (List<String>) resultData.get("cate");
//				String[] catetemp = cate.toString().replaceAll(" ", "").replace("[", "").replace("?", ">")
//						.replace("?", ">").replace("]", "").split(">");
//				resultData.put("cate", Arrays.asList(catetemp));
//			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	private String getPage(String url) {
		if (url.contains("club.dzwww.com")) {
			Pattern iidPatter = Pattern.compile("(thread-\\d+-)(\\d+)(-\\d+.html)");
			Matcher match = iidPatter.matcher(url);
			if (match.find()) {
				return match.group(2);
			}
		}
		return "0";
	}
}
