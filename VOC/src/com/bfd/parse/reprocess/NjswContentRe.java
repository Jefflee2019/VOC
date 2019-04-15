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
 *       处理发表时间和来源
 *       生成评论页链接
 * 
 * @author bfd_03
 *
 */
public class NjswContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 来源
			if (resultData.containsKey(Constants.SOURCE)) {
				stringToMap(resultData, Constants.SOURCE);
			}		
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				stringToMap(resultData, Constants.POST_TIME);
			}
		}
		
		//getCommentUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 发表时间
		if (key.equals(Constants.POST_TIME)) {
			String sValue = (String) resultData.get(Constants.POST_TIME);
			int index = sValue.indexOf("来源：");
			if (index > 0) {
				// 发布时间：2007-12-23 来源：镇江日报
				String sPostTime = sValue.substring(0, index - 1).trim();
				sPostTime = sPostTime.replace("发布时间：", "");
				resultData.put(Constants.POST_TIME, sPostTime);
				return;
			}
			
			sValue = sValue.replace("【字体：放大 缩小 默认】", "").replace("发布时间：", "");
			index = sValue.indexOf("金山网");
			if (index > 0) {
				// 发布时间：2012-10-15 08:23 金山网 www.jsw.com.cn 【字体：放大 缩小 默认】
				String sPostTime = sValue.substring(0, index - 1).trim();
				resultData.put(Constants.POST_TIME, sPostTime);
			}
		}
		// 来源
		if (key.equals(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);	
			int index = source.indexOf("来源：");
			if(index >= 0){
				source = source.substring(index + 3);
				resultData.put(Constants.SOURCE, source);
			}
		}
		
	}
	
	/**
	 * 拼接评论页的URL的生成任务
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getUrl();
		Pattern pattern = Pattern.compile("http://www.jsw.com.cn/\\d+-\\d+/\\d+/content_\\d+.htm");
		Matcher matcher = pattern.matcher(url);
		
		if (matcher.find()) {
			Map commentTask = new HashMap();
			StringBuffer sb = new StringBuffer();
			
			sb.append("http://218.3.114.9:8080/nis/servlet/CommentServlet?actionType=commentList&nodeID=0&nsID=0&pageNo=1&pageSize=20");
			
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
