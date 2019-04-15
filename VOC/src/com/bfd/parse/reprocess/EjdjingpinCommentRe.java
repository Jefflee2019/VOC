package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：京东
 * 
 * 主要功能：后处理插件处理评论页数据
 * 
 * @author bfd_03
 *
 */
public class EjdjingpinCommentRe implements ReProcessor {
	private static final Pattern pattern = Pattern.compile("\\d{4}.\\d{1,2}.\\d{1,2}.\\d{1,2}.\\d{1,2}",Pattern.DOTALL);
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		resultData.put(Constants.MSG_TYPE, "comment");
		// 处理多余括号的问题
		Set<String> keySet = resultData.keySet();
		for (String key : keySet) {
			if (!key.endsWith("_cnt") && !key.endsWith("_rate")) {
				continue;
			}
			Object obj = resultData.get(key);
			if (!(obj instanceof String)) {
				continue;
			}
			String value = (String) obj;
			value = value.replace("(", "").replace(")", "").trim();
			resultData.put(key, value);
			if (key.endsWith("_rate") && value.contains("%")) {
				value = value.replace("%", "");
				float fValue = Float.parseFloat(value);
				resultData.put(key, fValue / 100);
			}
		}
		//处理评论总数、好评数等字段(如200+、3.6万+)
		if(resultData.containsKey(Constants.REPLY_CNT)) {
			String oldReplyCnt = resultData.get(Constants.REPLY_CNT).toString();
			String oldGoodCnt = resultData.get(Constants.GOOD_CNT).toString();
			String oldGeneralCnt = resultData.get(Constants.GENERAL_CNT).toString();
			String oldPoorCnt = resultData.get(Constants.POOR_CNT).toString();
			String oldWithpic = resultData.get(Constants.WITHPIC_CNT).toString();
			NumFormat(resultData, oldReplyCnt);
			resultData.put(Constants.REPLY_CNT, NumFormat(resultData, oldReplyCnt));
			NumFormat(resultData, oldGoodCnt);
			resultData.put(Constants.GOOD_CNT, NumFormat(resultData, oldGoodCnt));
			NumFormat(resultData, oldGeneralCnt);
			resultData.put(Constants.GENERAL_CNT, NumFormat(resultData, oldGeneralCnt));
			NumFormat(resultData, oldPoorCnt);
			resultData.put(Constants.POOR_CNT, NumFormat(resultData, oldPoorCnt));
			NumFormat(resultData, oldWithpic);
			resultData.put(Constants.WITHPIC_CNT, NumFormat(resultData, oldWithpic));
		}
		
		// 处理买家印象
		if (resultData.containsKey(Constants.BUYER_IMPRESSION)) {
			String buyerImpression = (String) resultData
					.get(Constants.BUYER_IMPRESSION);
			buyerImpression = buyerImpression.replaceAll("\\s+", "")
					.replace("(", ":").replace(")", ",");
			buyerImpression = buyerImpression.substring(0,
					buyerImpression.length() - 1);
			resultData.put(Constants.BUYER_IMPRESSION, buyerImpression);
		}
		List<Map<String, Object>> comments = (List<Map<String, Object>>)resultData.get(Constants.COMMENTS);
		List<String> time_list = new ArrayList<String>();
		if (comments != null) {
			String pageData = unit.getPageData();
			//获取评分和GUID
			getItemInfoByHtml(Constants.SCORE, "<span class=\"star sa(\\d+)\"></span>", pageData, comments);
			getItemInfoByHtml(Constants.GUID, "<div class=\"useful\" id=\"(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})\">", pageData, comments);
			
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			for (int i = comments.size() - 1; i>=0; i--) {
				Map<String, Object> comment = comments.get(i);
				comment.put(Constants.COMMENT_REPLY, getCommentReply(comment));
				
				//删除掉评论时间错误的数据
				String comTime = (String) comment.get(Constants.COMMENT_TIME);
				//控制评论时间是2000年以后的数据
				if(!comTime.startsWith("20")){
					comments.remove(i);
					continue;
				}
				
				Date comDate = null;
				long lessSenconds = 0l;
				Date now = new Date();
				try {
					comDate = sf.parse(comTime);
					lessSenconds = (comDate.getTime()-now.getTime())/1000;
				} catch (ParseException e) {
				    // 如果时间解析失败，果断删除本评论
					comments.remove(i);
					continue;
				}
				
				//由于服务器时间比页面时间晚3分钟，故需要剔除超出时间的数据
				if (comDate == null || lessSenconds > 200) {
					comments.remove(i);
					continue;
				}
				
				// 处理有用数，当不存在时给出默认值0，避免d10、d11解析不到
				if (comment.containsKey(Constants.FAVOR_CNT)) {
					Object obj = comment.get(Constants.FAVOR_CNT);
					if (obj instanceof String) {
						String favorCnt = (String) obj;
						favorCnt = favorCnt.replace("有用", "").replace("(", "")
								.replace(")", "").trim();
						comment.put(Constants.FAVOR_CNT, favorCnt);
					}
				} else {
					comment.put(Constants.FAVOR_CNT, 0); //默认为0
				}
				
				//如果评价内容不存在则删除该评论
				if(!comment.containsKey("dl0")){ 
					comments.remove(i);
					continue;
				}
				getContentAndTag(comment, "dl0");    // 评价内容
				if(comment.containsKey("dl1")){      // 评价标签
					getContentAndTag(comment, "dl1");
				}
				time_list.add(comTime);
			}
			
			/**
			 * 评论多余10页的情况手动翻页
			 */
			Pattern p = Pattern.compile("(\\d+)-0.html");
			Matcher m = p.matcher(url);
			int pageNo = 1; 
			while (m.find()) {
				pageNo = Integer.valueOf(m.group(1));
			}
			String nextpage =  url.replace(pageNo + "-0.html", (pageNo+1) + "-0.html");
//			String nextpage = checkNextPage(url);
			if(nextpage!=null && pageNo >= 10) {
				resultData.put("nextpage", nextpage);
				List<Map<String, Object>> tasks  = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				if(tasks==null || tasks.isEmpty()){
					Map<String,Object> nextpageMap = new HashMap<String,Object>();
					nextpageMap.put(Constants.LINK, nextpage);
					nextpageMap.put(Constants.RAWLINK, nextpage);
					nextpageMap.put(Constants.LINKTYPE, "eccomment");
					tasks.add(nextpageMap);
				}
			}
			
			/**
			 * 判断评论时间是否小于当前服务器时间month
			 */
			if (time_list != null && time_list.size() != 0) {
				Collections.sort(time_list);
				int month = -1;  //暂定1个月进行测试
				int day = -7;  //暂定N天进行测试
				String comment_time = time_list.get(time_list.size() - 1);
				Date maxCommentDate = null;
				Date dNow = new Date();
				Date dBefore = new Date();
				Calendar calendar = Calendar.getInstance(); //得到日历
				calendar.setTime(dNow);//把当前时间赋给日历
//				calendar.add(calendar.MONTH, month);  //设置为前1月
				calendar.add(calendar.DATE, day);  //设置为前N天
				dBefore = calendar.getTime();   //得到前1月的时间
				try {
					maxCommentDate = sf.parse(comment_time);
					if (dBefore.getTime() > maxCommentDate.getTime()) {
						resultData.remove("nextpage");
						resultData.put(Constants.TASKS,new ArrayList());
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}
	    }	
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String checkNextPage(String url) {
		Pattern p = Pattern.compile("(\\d+)-0.html");
		Matcher m = p.matcher(url);
		int pageNo = 1; 
		while (m.find()) {
			pageNo = Integer.valueOf(m.group(1));
		}
		return url.replace(pageNo + "-0.html", (pageNo+1) + "-0.html");
	}

	private int NumFormat(Map<String, Object> resultData, String str) {
		int replyCnt = 0;
		if(str.contains("+")) {
			str = str.replace("+", "").trim();
			if(str.contains("万")) {
				str = str.replace("万", "").trim();
				float tem = Float.parseFloat(str)*10000;
				 replyCnt = (int)tem;
				 return replyCnt;
			}else {
				return (int)Float.parseFloat(str);
			}
		}else {
			return Integer.parseInt(str);
		}
	}
	
	/**
	 * 处理评价标签和评价内容
	 * @param comment
	 * @param string
	 */
	private void getContentAndTag(Map<String, Object> comment, String key) {
		Object obj = comment.get(key);
		comment.remove(key);
		if(!(obj instanceof String)){
			return;
		}
		String value = (String) obj;
		if(value.contains("心 得：")){
			value = value.replace("心 得：", "");
			comment.put(Constants.COMMENT_CONTENT, value.trim());
		}else if(value.contains("标 签：")){
			value = value.replace("标 签：", "").trim().replace(" ", ",");
			comment.put(Constants.COMMENT_TAG, value.trim());
		}
		
	}
	
	private void getItemInfoByHtml(String key, String pattern, String pageData,
			List<Map<String, Object>> comments) {
		Pattern scorePattern = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher matcher = scorePattern.matcher(pageData);
		List<String> scoreList = new ArrayList<String>();
		while (matcher.find()) {
			scoreList.add(matcher.group(1));
		}
		if (scoreList == null || scoreList.size() != comments.size()) {
			return;
		}
		for (int i = 0; i < comments.size(); i++) {
			comments.get(i).put(key, scoreList.get(i));
		}

	}

	private List<Map<String, String>> getCommentReply(Map<String, Object> comment) {
		List<Map<String, String>> comReplyList = new ArrayList<Map<String, String>>();
		Set<String> keySet = comment.keySet();
		if(keySet==null || keySet.size() == 0){
			return comReplyList;
		}
		List<String> keyArray = new ArrayList<String>();
		for (String key : keySet) {
			if (key.startsWith("comment_reply_str")) {
				keyArray.add(key);
			}
		}
		if(keyArray != null && !keyArray.isEmpty()){
			comReplyList = parseComReplyString(keyArray, comment);
		}
		for(String key:keyArray){
			comment.remove(key);
		}
		return comReplyList;
	}
	
	private static List<Map<String, String>> parseComReplyString(List<String> keyArray, Map<String, Object> comment) {
		List<Map<String, String>> comReplyList = new ArrayList<Map<String, String>>(); 
		Map<String, String> comReplyMap = null;
		Matcher matcher =null;
		for (int i = 0; i < keyArray.size(); i++) {
			String strComReply = (String) comment.get(keyArray.get(i));
			comReplyMap = new HashMap<String, String>();
			matcher = pattern.matcher(strComReply);
			if (matcher.find()) {
				String comReplyTime = matcher.group();
				comReplyMap.put(Constants.COMMENT_REPLY_TIME, comReplyTime);
				int indexTime = strComReply.indexOf(comReplyTime);
				if (indexTime > 0) {
					strComReply = strComReply.substring(0, indexTime - 1);
				}
			}
			strComReply = strComReply.replace("：", ":");
			String[] arr = strComReply.split(":");
			if (arr != null && arr.length == 2) {
				String comReplyName = arr[0];
				String comReplyContent = arr[1];
				comReplyMap.put(Constants.COMMENT_REPLY_NAME, comReplyName);
				comReplyMap.put(Constants.COMMENT_REPLY_CONTENT,
						comReplyContent);
			}
			comReplyList.add(comReplyMap);
		}
		return comReplyList;
		
	}

}
