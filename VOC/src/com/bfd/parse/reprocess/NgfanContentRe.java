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
 * 站点名：机锋网(新闻)
 * 
 * 主要功能：处理作者，摘要，正文
 *       生成评论页任务
 * 
 * @author bfd_03
 *
 */
public class NgfanContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		//作者
		if(resultData.containsKey(Constants.AUTHOR)){
			stringToMap(resultData, Constants.AUTHOR);
		}
		//摘要
		if(resultData.containsKey(Constants.BRIEF)){
			stringToMap(resultData, Constants.BRIEF);
		}
		//正文
		if(resultData.containsKey(Constants.CONTENT)){
			stringToMap(resultData, Constants.CONTENT);
		}
		
		getCommentUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 作者
		if (key.equals(Constants.AUTHOR)) {
			//【投稿】 作者：张川
			String author = (String) resultData.get(key);
			if(author.contains("作者：")){
				author = author.substring(author.indexOf("作者：")+3);			
			}
			resultData.put(Constants.AUTHOR, author);
		}
		// 摘要
		if (key.equals(Constants.BRIEF)) {
			String brief = (String) resultData.get(key);
			if(brief.contains("摘要：")){
				brief = brief.replaceFirst("摘要：", "").trim();			
			}
			resultData.put(Constants.BRIEF, brief);
		}
		// 正文
		if (key.equals(Constants.CONTENT)) {
			String content = (String) resultData.get(key);
			if(content.startsWith("/")){
				content = content.replaceFirst("/", "");			
			}
			resultData.put(Constants.CONTENT, content);
		}

	}

	
	
	/**
	 * 拼接评论页的URL的生成任务
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String topicsId = "";
		String sClientId = "";
		//从url中获取topicsid
		String url = (String) unit.getUrl();
		Pattern pattern = Pattern.compile("http://www.gfan.com/[a-z]+/\\d{8}(\\d+)\\.html", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
		
		if (matcher.find()) {
			topicsId = matcher.group(1);
		}
		
		//从页面获取client_id
		String pageData = unit.getPageData();
		pattern = Pattern.compile("appid = '(\\w+)',", Pattern.DOTALL);
		matcher = pattern.matcher(pageData);
		if (matcher.find()) {
			sClientId = matcher.group(1);
		}
		
		if(!topicsId.equals("") && !sClientId.equals("")){
			Map commentTask = new HashMap();
			StringBuffer sb = new StringBuffer();
			
			//http://changyan.sohu.com/node/html?client_id=cyr1PvQCP&topicsid=72829&spSize=5
			sb.append("http://changyan.sohu.com/node/html?client_id="+sClientId);
			sb.append("&topicsid="+topicsId);
			
			String sCommUrl = sb.toString();
			commentTask.put(Constants.LINK, sCommUrl);
			commentTask.put(Constants.RAWLINK, sCommUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, sCommUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);	
			}
			ParseUtils.getIid(unit, result);
			
		}
		
	}

}
