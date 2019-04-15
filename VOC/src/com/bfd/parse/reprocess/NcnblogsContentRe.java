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
 * 站点名：博客园
 * <p>
 * 主要功能：发表时间字段
 * @author bfd_01
 *
 */
public class NcnblogsContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NcnblogsContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				String time = resultData.get(Constants.POST_TIME).toString();
				time = time.replace("发布于 ", "");
				resultData.put(Constants.POST_TIME, time);
			}
			String url = unit.getUrl();
			Map<String, String> commentTask = new HashMap<String, String>();
			String commentUrl = getCommentUrl(url);
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

	// https://news.cnblogs.com/n/570628/
private String getCommentUrl(String url) {
	Pattern p = Pattern.compile("(\\d+)");
	Matcher m = p.matcher(url);
	String iid = null;
	String commentUrl = null;
	while (m.find()) {
		iid = m.group(1);
	}
	commentUrl = "https://news.cnblogs.com/CommentAjax/GetComments?contentId=" + iid;
	return commentUrl;
}
}
