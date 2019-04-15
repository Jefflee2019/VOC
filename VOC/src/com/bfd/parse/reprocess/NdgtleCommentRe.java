package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Ndgtle(数字尾巴新闻)
 * 
 * 主要功能：
 *       处理评论时间
 *       处理引用他人评论的评论
 * 
 * @author bfd_03
 *
 */
public class NdgtleCommentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NdgtleCommentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 评论
			if (resultData.containsKey(Constants.COMMENTS)) {
				stringToMap(resultData, Constants.COMMENTS);
			}
			/*
			 对于http://dgtle.com/article-11056-1.html需要动态加载的评论，后处理插件生成下一页任务
			在标记模板时，标记评论数字段，判断是否需要继续生成下一页。
			*/
			if (!resultData.containsKey(Constants.NEXTPAGE)) {
				getNextpageUrl(unit, result);
			}
			
		}
		
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	public void stringToMap(Map<String, Object> resultData, String key) {	
		// 评论
		if (key.equals(Constants.COMMENTS)) {
			List<Map<String,Object>> commentsList = (List<Map<String, Object>>) resultData.get(Constants.COMMENTS);		
			List<Map<String,Object>> newCommentsList = new ArrayList<Map<String, Object>>();
			for(Map<String,Object> temp:commentsList){
				String sCommentTime = (String) temp.get(Constants.COMMENT_TIME);
				String sCommentContent = (String) temp
						.get(Constants.COMMENT_CONTENT);
				sCommentTime = ConstantFunc.convertTime(sCommentTime);
				
				//对于有引用的评论，需要获取引用信息，并且调整评论内容
				if (temp.containsKey(Constants.REFER_COMMENTS)) {
					Map<String, Object> referenceComments = new HashMap<String, Object>();
					
					String strReference = (String) temp
							.get(Constants.REFER_COMMENTS);
					sCommentContent = sCommentContent.replace(strReference, "")
							.trim();
				
					if(strReference.contains(" 发表于 ")){
						String[] arr = strReference.split(" 发表于 ");
						if(arr.length == 2){     //引用内容，引用人，引用时间
							String s = arr[1];
							String username = arr[0];
							String content = s.replaceFirst("[\\d\\-]+\\s*[\\d\\:]+", "").trim();
							String time = s.replace(content, "").trim();
							
							referenceComments.put(Constants.REFER_COMM_USERNAME, username);
							referenceComments.put(Constants.REFER_COMM_CONTENT, content);
							referenceComments.put(Constants.REFER_COMM_TIME, time);
						}
					}else if(strReference.contains("引用自")){
						strReference = strReference.replace("引用自 ", "");		
						String[] arr = strReference.split("的评论:");
						if (arr.length == 2) { // 引用内容，引用人
							referenceComments.put(Constants.REFER_COMM_USERNAME, arr[0].trim());
							referenceComments.put(Constants.REFER_COMM_CONTENT, arr[1].trim());
						}
					}else{
						referenceComments.put(Constants.REFER_COMM_CONTENT, strReference); //引用内容
					}
					
					temp.put(Constants.REFER_COMMENTS, referenceComments);
				}
				temp.put(Constants.COMMENT_TIME, sCommentTime);
				temp.put(Constants.COMMENT_CONTENT, sCommentContent);
				newCommentsList.add(temp);
			}
			resultData.put(Constants.COMMENTS, newCommentsList);
		}

	}
	
	/**
	 * 拼接下一页的URL的生成任务
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getNextpageUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String parseUrl = (String) unit.getTaskdata().get("url");
		StringBuffer sb = new StringBuffer();
		int pageNo = 0;

		Pattern pattern = Pattern.compile("http://www.dgtle.com/portal.php\\?mod=view&aid=(\\d+)&page=(\\d+)");
		Matcher matcher = pattern.matcher(parseUrl);
		if (matcher.find()) {
			pageNo = Integer.parseInt(matcher.group(2)) + 1; // 取到了当前页面页码，则获取下一页
			sb.append("http://www.dgtle.com/portal.php?mod=view&aid=");
			sb.append(matcher.group(1));
			sb.append("&page="+pageNo);
		}
		
		String sReplyCnt = (String)resultData.get(Constants.REPLY_CNT);
		if(sReplyCnt == null){
			return;
		}
		sReplyCnt = sReplyCnt.replace("条", "").trim();
		int replyCnt = 0;
		int pageMaxNo = 0;
		try {
			replyCnt = Integer.parseInt(sReplyCnt);
			resultData.remove(Constants.REPLY_CNT);
			pageMaxNo = replyCnt / 50;
			if (replyCnt % 50 > 0) {
				pageMaxNo++;
			}
		} catch (Exception e) {
			LOG.warn("NdgtleCommentRe后处理插件获取评论数异常！！！");
		}
		
		Map nextpageTask = new HashMap();
		if (pageNo > 0 && pageNo <= pageMaxNo) {
			String nextpage = sb.toString();
			nextpageTask.put(Constants.LINK, nextpage);
			nextpageTask.put(Constants.RAWLINK, nextpage);
			nextpageTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, nextpage);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);
			}
			ParseUtils.getIid(unit, result);
		}
	}

}
