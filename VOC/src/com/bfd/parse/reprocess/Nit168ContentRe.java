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
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：it168(新闻)
 * 
 * 主要功能：处理作者，发表时间
 * 
 * @author bfd_03
 *
 */
public class Nit168ContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 作者
			if (resultData.containsKey(Constants.AUTHOR)) {
				stringToMap(resultData, Constants.AUTHOR);
			}
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				stringToMap(resultData, Constants.POST_TIME);
			}
			
		}
		
		String url = (String) unit.getTaskdata().get("url");
		if(url.contains("/tu/")){
			parsePicInfo(unit, resultData);
		}
		
		getCommentUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 作者
		if (key.equals(Constants.AUTHOR)) {
			resultData.put(
					Constants.AUTHOR,
					parseByRegex((String) resultData.get(key),
							"作者[\\:\\：]\\s*(\\S+)", 1));
		}
		//发表时间 
		if (key.equals(Constants.POST_TIME)) {
			resultData.put(
					Constants.POST_TIME,
					parseByRegex((String) resultData.get(key),
							"(\\d{4}.\\d{1,2}.\\d{1,2}.(?:\\d{1,2}:\\d{1,2})?)", 1));
		}

	}

	/**
	 * 集成正则表达式匹配，
	 * 
	 * @param data
	 *            (需要匹配的数据)
	 * @param regex
	 *            (正则表达式)
	 * @return
	 */
	public String parseByRegex(String data, String regex, int index) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			data = matcher.group(index);
		}
		return data;
	}
	
	
	/**
	 * 拼接评论页的URL的生成任务
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getTaskdata().get("url");
		String topicsId = "";
		
		Pattern pattern = Pattern.compile("http://mobile.it168.com/\\w+/\\d+/\\d+/(\\d+).shtml", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
				
		if (matcher.find()) {
			topicsId = String.valueOf(Long.parseLong(matcher.group(1)));
		}
		
		if(!topicsId.equals("")){
			Map commentTask = new HashMap();
			StringBuffer sb = new StringBuffer();
			
			//http://changyan.sohu.com/node/html?client_id=cyrjqzNlH&topicsid=1720547
			sb.append("http://changyan.sohu.com/node/html?client_id=cyrjqzNlH");
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
		}
		ParseUtils.getIid(unit, result);
	}
	

	/**
	 * 从页面源码中获取到大图和小图列表
	 * @param unit
	 * @param resultData
	 */
	private void parsePicInfo(ParseUnit unit, Map<String, Object> resultData) {
		String pageData = unit.getPageData();
		String largeImg = "";
		List<String> smallImgList = new ArrayList<String>();
		
		
		Pattern pattern = Pattern.compile("\"pic\" : \"(.*?)\",", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(pageData);
		if (matcher.find()) {
			largeImg = matcher.group(1);
		}
				
		if(!largeImg.equals("")){			
			resultData.put(Constants.LARGE_IMG, largeImg);
			String preImg = largeImg.substring(0, largeImg.lastIndexOf("/") + 1);

			pattern = Pattern.compile("\"PicPath\":\"(\\d+\\.\\w+)\"", Pattern.DOTALL);
			matcher = pattern.matcher(pageData);
			while (matcher.find()) {
				smallImgList.add(preImg + matcher.group(1));
			}

			if (!smallImgList.isEmpty()) {
				resultData.put(Constants.SMALL_IMG, smallImgList);
			}
		}
	}

}
