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
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * @site C114 (N114)
 * @function：新闻评论页后处理
 * @author bfd_04
 */

public class N114CommentRe implements ReProcessor {
	private static final Pattern PAGE_PATTERN = Pattern.compile("page=(\\d+)");
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		List commentList = new ArrayList();
		if(resultData != null && resultData.containsKey("comments1")) {
			List comment1 = (List)resultData.get("comments1");
			if(resultData != null && resultData.containsKey("comments2")) {
				List comment2 = (List)resultData.get("comments2");
				int flag = 0;
				for(Object obj1 : comment1) {
//					for(Object obj2: comment2) {
						Map map1 = (Map)obj1;
						
						if(map1.containsKey(Constants.REPLYFLOOR) && map1.containsKey(Constants.COMMENT_TIME)) {
							String replyfloor = map1.get(Constants.REPLYFLOOR).toString();
							String commentTime = map1.get(Constants.COMMENT_TIME).toString();
							replyfloor = replyfloor.split("楼")[0].replace(commentTime, "").trim();
							map1.put(Constants.REPLYFLOOR, replyfloor);
						}
						
						Map map2 = (Map)comment2.get(flag);
						map1.putAll(map2);
						commentList.add(map1);
						resultData.remove("comments1");
						resultData.remove("comments2");
						flag ++;
//					}
				}
				resultData.put(Constants.COMMENTS, commentList);
			}
		}
		if(resultData.containsKey(Constants.COMMENTS)) {
			List commList = (List)resultData.get(Constants.COMMENTS);
			if (commList !=null && !commList.isEmpty()) {
				for(Object obj: commList) {
					Map tempMap = (Map)obj;
					if(tempMap.containsKey("referCommContent")) {
						String referCommContent = tempMap.get(Constants.REFER_COMM_CONTENT).toString();
						Map referComments = new HashMap();
						referComments.put(Constants.REFER_COMM_CONTENT, referCommContent);
						tempMap.put(Constants.REFER_COMMENTS, referComments);
						tempMap.remove(Constants.REFER_COMM_CONTENT);
					}
				}
			}
		}
		
		//deal with nextpage
		if(resultData.containsKey(Constants.REPLY_CNT)) {
			String replyCnt = resultData.get(Constants.REPLY_CNT).toString();
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			if (! url.contains("page=")) {
				url = url + "&page=1";
			}
			Matcher match = PAGE_PATTERN.matcher(url);
			
			String pageNum = "";
			if(match.find()) {
				pageNum = match.group(1);
			}
			if(Math.ceil((Integer.parseInt(replyCnt))-Integer.parseInt(pageNum)) > 0) {
				String nextpage = url.replace("page=" + match.group(1), "page=" + 
						String.valueOf((Integer.parseInt(pageNum) + 1))); 
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newscomment");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}