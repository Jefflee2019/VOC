package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：人民网-论坛
 * <p>
 * 主要功能：处理帖子页中的字段
 * @author bfd_01
 *
 */
public class BpeoplePostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BpeoplePostRe.class);
	private static boolean isFirst = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			if (resultData.containsKey(Constants.NEWSTIME)) {
				String newstime = resultData.get(Constants.NEWSTIME).toString();
				resultData.put(Constants.NEWSTIME, getTime(newstime));
			}

			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				for (Object obj : replys) {
					if (obj instanceof Map) {
						Map<String, Object> reply = (Map<String, Object>) obj;

						if (reply.containsKey("replyfloor")) {
							String replyfloor = reply.get("replyfloor")
									.toString();
							if ("1楼".equals(replyfloor)) {
								isFirst = true;
							}
							replyfloor = replyfloor.replace("楼", "");
							reply.put("replyfloor", replyfloor);
						}
						if (reply.containsKey("replydate")) {
							String replydate = reply.get("replydate")
									.toString();
							reply.put(Constants.REPLYDATE, getTime(replydate));
						}
						if (reply.containsKey("reply_up_cnt")) {
							String upCnt = reply.get("reply_up_cnt")
									.toString();
							reply.put("reply_up_cnt", Integer.valueOf(upCnt
									.replace("(", "").replace(")", "")));
						}
					}
				}
			}
			if (resultData.containsKey(Constants.CATE)) {
				List<String> list = (List<String>) resultData.get(Constants.CATE);
				if ("".equals(list.get(0).toString())) {
					list.remove(0);
				}
				resultData.put(Constants.CATE, list);
			}

			// 不是主贴时
			// delete contents,authorname,newstime
			if (!isFirst) {
//				if (resultData.containsKey(Constants.NEWSTIME)) {
//					resultData.remove(Constants.NEWSTIME);
//				}
				if (resultData.containsKey(Constants.AUTHORNAME)) {
					resultData.remove(Constants.AUTHORNAME);
				}
				if (resultData.containsKey(Constants.CONTENTS)) {
					resultData.remove(Constants.CONTENTS);
				}
			}

			// nextpage tasks
			String url = unit.getUrl();
			int pageNum = getPage(url);
			int replycount = 0;
			int pageSize = 20;
			String nextpage = null;
			if (resultData.containsKey(Constants.REPLYCOUNT)) {
				replycount = Integer.valueOf(resultData.get(
						Constants.REPLYCOUNT).toString());
			}
			if (1 == pageNum) {
				nextpage = url.split(".html")[0] + "_2.html";
			} else {
				nextpage = url.split("_(\\d+).html")[0] + "_" + (pageNum + 1)
						+ ".html";
			}
			if (replycount > pageNum * pageSize) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "bbspost");
				if (!resultData.isEmpty()) {
					resultData.put("nextpage", nextpage);
					List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
					tasks.add(nextpageTask);
				}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

	private String getTime(String newstime) {
		Pattern p = Pattern.compile("(\\d+-\\d+-\\d+\\s\\d+:\\d+:\\d+)");
		Matcher m = p.matcher(newstime);
		while (m.find()) {
			newstime = m.group(1);
		}
		return newstime;
	}
	private int getPage(String url) {
		Pattern p = Pattern.compile("_(\\d+).html");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return Integer.valueOf(m.group(1));
		}
		return 1;
	}

}
