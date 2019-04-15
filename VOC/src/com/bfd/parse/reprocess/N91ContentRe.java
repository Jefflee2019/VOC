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
 * 站点名：91门户
 * <p>
 * 主要功能：处理字段中的多余信息
 * @author bfd_01
 *
 */
public class N91ContentRe implements ReProcessor {
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		String url = unit.getUrl();
		if (resultData.containsKey(Constants.POST_TIME)) {
			String posttime = resultData.get(Constants.POST_TIME).toString();
			// 发布时间：2015/3/13 10:30:33
			posttime = posttime.replace("时间：", "");
			resultData.put(Constants.POST_TIME, posttime);
		}
		
			Map<String, String> commentTask = new HashMap<String, String>();
			String commentUrl = getCommentUrl(pageData, url);
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
			ParseUtils.getIid(unit, result);
		
		return new ReProcessResult(processcode, processdata);
	}

	private String getCommentUrl(String pageData,String url) {
		String commentUrl = null;
		if (pageData.contains("var appid =")) {
			Pattern p = Pattern.compile("appid\\s*=\\s*\'(\\w*)\',");
			Matcher m = p.matcher(pageData);
			String clientid = null;
			while (m.find()) {
				clientid = m.group(1);
			}
			commentUrl = "http://changyan.sohu.com/api/3/topic/liteload?callback=jQuery&client_id="
					+ clientid + "&topic_url=" + url + "&page_size=10";
		}
		return commentUrl;
	}
}
