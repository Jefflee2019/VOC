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
 * 站点名：乐信网
 * <p>
 * 主要功能：处理字段中的多余信息,生成评论任务
 * @author bfd_01
 *
 */
public class NlexunContentRe implements ReProcessor {
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData.containsKey(Constants.CONTENT)
				&& resultData.containsKey(Constants.POST_TIME)
				&& resultData.containsKey(Constants.VIEW_CNT)) {
			String content = resultData.get(Constants.CONTENT).toString();
			String posttime = resultData.get(Constants.POST_TIME).toString();
			String viewcnt = resultData.get(Constants.VIEW_CNT).toString();
			content = content.replace(posttime, "").replace(viewcnt, "");
			resultData.put(Constants.CONTENT, content);
		}
		
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			String viewcnt = resultData.get(Constants.VIEW_CNT).toString();
			viewcnt = viewcnt.replace("阅读:", "");
			resultData.put(Constants.VIEW_CNT, Integer.valueOf(viewcnt));
		}
		
		String pagedata = unit.getPageData();
		if (isComment(pagedata)) {
			Map<String, String> commentTask = new HashMap<String, String>();
			String commentUrl = getCommentUrl(pagedata);
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
			ParseUtils.getIid(unit, result);
		}
		
		
		return new ReProcessResult(processcode, processdata);
	}

	private boolean isComment(String data) {
		Pattern p = Pattern.compile("更多评论（\\d+");
		Matcher m = p.matcher(data);
		boolean flag = false;
		while (m.find()) {
			flag = true;
		}
		return flag;
	}
	
	private String getCommentUrl(String data) {
		Pattern p = Pattern.compile("ajax/rlylist\\.aspx\\?topicid=(\\d+)&");
		Matcher m = p.matcher(data);
		String iid = null;
		String commentUrl = null;
		while (m.find()) {
			iid = m.group(1);
		}
		commentUrl = "http://sjnews.lexun.cn/touch/ajax/rlylist.aspx?topicid=" + iid + "&page=1&op=list";
		return commentUrl;
	}
}
