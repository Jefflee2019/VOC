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
/**
 * 站点名：界面网
 * <p>
 * 主要功能：处理字段中的多余信息，生成评论页任务
 * @author bfd_01
 *
 */
public class NjiemianContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NjiemianContentRe.class);
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.replace("来源：", "");
			resultData.put(Constants.SOURCE, source);
		}
		
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			String viewcnt = resultData.get(Constants.VIEW_CNT).toString();
			if (viewcnt.contains("W")) {
				viewcnt = viewcnt.substring(0, viewcnt.length() -1);
				if (viewcnt.contains(".")) {
					viewcnt = viewcnt.replace(".", "");
				}
				
			}
			int cnt = Integer.valueOf(viewcnt + "000");
			resultData.put(Constants.VIEW_CNT, cnt);
		}
		
		if (resultData.containsKey(Constants.POST_TIME)) {
			String posttime = resultData.get(Constants.POST_TIME).toString();
			// 2017/03/05 10:08收藏(13) 23.5W
			if (posttime.contains("收藏")) {
				posttime = posttime.split("收藏")[0];
			}
			resultData.put(Constants.POST_TIME, posttime);
		}
		
		int replycnt = 0;
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			replycnt = Integer.valueOf(resultData.get(Constants.REPLY_CNT).toString());
		}
		String commentUrl = null;
		if (replycnt > 0) {
			Matcher m = Pattern.compile("(\\d+).html").matcher(url);
			String iid = null;
			while (m.find()) {
				iid = m.group(1);
			}
			// http://a.jiemian.com/index.php?m=comment&a=getlistCommentP&aid=918480
			commentUrl = "http://a.jiemian.com/index.php?m=comment&a=getlistCommentP&aid=" + iid;
		}
		
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		if(commentUrl != null) {
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", commentUrl);
			nextpageTask.put("rawlink", commentUrl);
			nextpageTask.put("linktype", "newscomment");
			resultData.put(Constants.COMMENT_URL, commentUrl);
			tasks.add(nextpageTask);	// 添加下一页任务
		}
		return new ReProcessResult(processcode, processdata);
	
	}

}
