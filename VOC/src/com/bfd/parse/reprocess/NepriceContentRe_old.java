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
 * 站点名：Neprice
 * 
 * 主要功能：
 *       处理正文，来源，回复数,发表时间
 *       处理印象中的打分信息
 *       删除为空的路径
 *       生成评论页链接
 * 
 */
@Deprecated
public class NepriceContentRe_old implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 正文
			if (resultData.containsKey(Constants.CONTENT)) {
				stringToMap(resultData, Constants.CONTENT);
			}
			// 来源
			if (resultData.containsKey(Constants.SOURCE)) {
				stringToMap(resultData, Constants.SOURCE);
			}
			// 回复数
			if (resultData.containsKey(Constants.REPLY_CNT)) {
				stringToMap(resultData, Constants.REPLY_CNT);
			}
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				stringToMap(resultData, Constants.POST_TIME);
			}
			// 路径
			if (resultData.containsKey(Constants.CATE)) {
				stringToMap(resultData, Constants.CATE);
			}
			// 印象(优、缺点)
			if (resultData.containsKey(Constants.BUYER_IMPRESSION)) {
				stringToMap(resultData, Constants.BUYER_IMPRESSION);
			}
			
		}

		getCommentUrl(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData, String key) {
		// 发表时间
		if (key.equals(Constants.POST_TIME)) {
			String sPostTime = (String) resultData.get(Constants.POST_TIME);
			Pattern pattern = Pattern.compile("\\d{4}\\S\\d{1,2}\\S\\d{1,2}");
			Matcher matcher = pattern.matcher(sPostTime);
			if(matcher.find()){
				sPostTime = matcher.group();
				resultData.put(Constants.POST_TIME, sPostTime);
			}
		}
		// 路径
		if (key.equals(Constants.CATE)) {
			List<String> cate = (List<String>) resultData.get(Constants.CATE);
			if (cate.get(0).equals("")) {
				cate.remove(0);
			}
			resultData.put(Constants.CATE, cate);
		}
		// 正文
		if (key.equals(Constants.CONTENT)) {		
			String content = (String) resultData.get(Constants.CONTENT);
			int index = content.indexOf("提示：试试\"← →\"实现快速翻页");
			if (index > 0) {
				content = content.substring(0, index - 1);
			}
			index = content.indexOf("首页 上一页");
			if (index > 0) {
				content = content.substring(0, index - 1);
			}
			content = content.replace("在本页浏览全文", "").trim();
			resultData.put(Constants.CONTENT, content);
		}
		// 来源
		if (key.equals(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			int index = source.indexOf("来源：");
			if (index >= 0) {
				source = source.substring(index + 3);
			}
			resultData.put(Constants.SOURCE, source);
		}
		// 回复数
		if (key.equals(Constants.REPLY_CNT)) {
			String sReplyCnt = (String) resultData.get(Constants.REPLY_CNT);
			sReplyCnt = sReplyCnt.replace("(", "").replace(")", "").trim();
			resultData.put(Constants.REPLY_CNT, sReplyCnt);
		}
		// 印象(优、缺点)径
		if (key.equals(Constants.BUYER_IMPRESSION)) {
			List<String> buyerImpression = (List<String>) resultData.get(Constants.BUYER_IMPRESSION);
			String tempStr = buyerImpression.get(0);
			if (tempStr != null && tempStr.contains("ePrice点评")) {
				buyerImpression.remove(0);
			}
			resultData.put(Constants.BUYER_IMPRESSION, buyerImpression);
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
		int replyCnt = 0;
		try {
			replyCnt = Integer.parseInt((String) resultData
					.get(Constants.REPLY_CNT));
		} catch (Exception e) {
			replyCnt = -1;
		}
		if(replyCnt > 0){
			String url = (String) unit.getUrl();
			Pattern pattern = Pattern.compile("http://www.wyh.tv/\\w+/(\\d+).html", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(url);
			
			if (matcher.find()) {
				Map commentTask = new HashMap();
				StringBuffer sb = new StringBuffer();
				
				sb.append("http://www.wyh.tv/Handler/MemberAjax.ashx?oper=GetComment&articleId="+matcher.group(1));
				sb.append("&pageIndex=1");
				
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
		}
		
		ParseUtils.getIid(unit, result);
		
		
	}

}
