package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点名：Ntudou
 * 
 * 功能：网页源码中提取发表时间字段
 * 
 * @author bfd_06
 */
public class NtudouContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pt = match(">([\\d\\-\\:\\s]*)上传<", unit.getPageData());
		if (pt != null) {
			// POST_TIME
			resultData.put(Constants.POST_TIME, pt);
		}

		// 点播次数
		if (resultData.containsKey(Constants.PLAY_CNT)) {
			String oldplayCnt = resultData.get(Constants.PLAY_CNT).toString();
			int playCnt = 0;
			if (oldplayCnt.contains("万")) {
				playCnt = (int) Double.parseDouble(oldplayCnt.replace("万", "")) * 10000;
			} else {
				playCnt = Integer.parseInt(oldplayCnt);
			}
			resultData.put(Constants.PLAY_CNT, playCnt);
		}

		// 评论链接
		String url = unit.getUrl();
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		resultData.put("tasks", tasks);
		// http://new-play.tudou.com/v/XMzU4MTg5NTg3Mg==.html?spm=a2h28.8313475.main.dvideo
		String objectId = match("v/(\\S*).html", url);
		if (objectId != null) {
			Map<String, Object> rtask = new HashMap<String, Object>();
			StringBuilder commentUrl = new StringBuilder(
					"http://p.comments.youku.com/ycp/comment/pc/commentList?app=700-cJpvjG4g&objectId=");
			commentUrl
					.append(objectId)
					.append("&objectType=1&listType=0&currentPage=1&pageSize=30&sign=e8105f58e7b9557c286ed8d2ee0a7d70&time=1526982002");
			rtask.put("link", commentUrl);
			rtask.put("rawlink", commentUrl);
			rtask.put("linktype", "newscomment");
			resultData.put(Constants.COMMENT_URL, commentUrl);
			tasks.add(rtask);
		}
		return new ReProcessResult(processcode, processdata);
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}
}
