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
 * 站点名：Njsw
 * 
 * 主要功能：
 * 		   处理顶数量，倒数量
 *       生成下一页任务
 * 
 * @author bfd_03
 *
 */
public class NjswCommentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 评论的顶数量和倒数量
			if (resultData.containsKey(Constants.COMMENTS)) {
				stringToMap(resultData, Constants.COMMENTS);
			}			
		}

		// 组装下一页生成任务
		getNextpageUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData, String key) {
		// 评论的顶数量和倒数量
		if (key.equals(Constants.COMMENTS)) {
			List<Map<String,String>> comments = (List<Map<String, String>>) resultData.get(Constants.COMMENTS);
			for (int i = 0; comments != null && i < comments.size(); i++) {
				String sUpCnt = comments.get(i).get(Constants.UP_CNT); 
				String sDownCnt = comments.get(i).get(Constants.DOWN_CNT); 
				
				sUpCnt = sUpCnt.replace("[", "").replace("]", "");
				sDownCnt = sDownCnt.replace("[", "").replace("]", "");
				
				comments.get(i).put(Constants.UP_CNT, sUpCnt);
				comments.get(i).put(Constants.DOWN_CNT, sDownCnt);
			}
		}
		
	}

	/**
	 * 拼接下一页的URL的生成任务
	 * 
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String parseUrl = (String) unit.getTaskdata().get("url");

		Pattern pattern = Pattern.compile("(http://218.3.114.9:8080/nis/servlet/CommentServlet\\?actionType=commentList&nodeID=0&nsID=0&pageNo=)(\\d+)(&pageSize=20)");
		Matcher matcher = pattern.matcher(parseUrl);

		String nextpage = null;
		int pageNo = 0;
		if (matcher.find()) {
			pageNo = Integer.parseInt(matcher.group(2)) + 1; // 取到了当前页面页码，则获取下一页
			nextpage = matcher.group(1) + pageNo + matcher.group(3);
		}
		Map nextpageTask = new HashMap();
		List comments = (List) resultData.get("comments");
		if (nextpage != null && comments.size() >= 20) {
			nextpageTask.put(Constants.LINK, nextpage);
			nextpageTask.put(Constants.RAWLINK, nextpage);
			nextpageTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, nextpage);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);
			}
			ParseUtils.getIid(unit, result);
		}
	}

}
