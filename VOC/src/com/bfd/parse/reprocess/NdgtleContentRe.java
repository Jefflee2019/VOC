package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * 站点名：Ndgtle(数字尾巴新闻)
 * 
 * 主要功能：
 *       处理发表时间，作者
 *       生成评论页链接
 * 
 * @author bfd_03
 *
 */
public class NdgtleContentRe implements ReProcessor {

	private static final Pattern PATTERN_DATE = Pattern.compile("\\d+(\\S|\\s)*");
	private static final Pattern PATTERN_REPLY_CNT = Pattern.compile("\\d+");
	private static final Pattern PATTERN_COMMENTPAGE = Pattern.compile("<ul class=\"comment-list cl\" data-tid=\"(\\d+)\"");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		getCommentUrl(unit, resultData);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	
	
	/**
	 * 拼接评论页的URL的生成任务
	 * @param unit
	 * @param resultData2 
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void getCommentUrl(ParseUnit unit, Map<String, Object> resultData) {
		// "post_time": "数码·12-03 14:49"
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = (String) resultData.get(Constants.POST_TIME);
			Matcher dateM = PATTERN_DATE.matcher(posttime);
			while(dateM.find()){
				String date = dateM.group(0);
				if(!date.substring(0, 2).equals("20")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			        Date yyyy = new Date();
					date = sdf.format(yyyy) + "-" + date;
				}
				resultData.put(Constants.POST_TIME, date);
			}
		}
		//"reply_cnt": "(已有12条评论)"
		String count = "0";
		if(resultData.containsKey(Constants.REPLY_CNT)){
			String replycnt = (String) resultData.get(Constants.REPLY_CNT);
			Matcher countM = PATTERN_REPLY_CNT.matcher(replycnt);
			while(countM.find()){
				count = countM.group(0);
				resultData.put(Constants.REPLY_CNT, count);
			}
		}
		//拼接评论页链接
		if(Integer.parseInt(count) > 0){
			String htmlData = unit.getPageData();
			List<Map<String, Object>> taskList =null;
			if(resultData.get(Constants.TASKS) != null){
				taskList = (List<Map<String,Object>>) resultData.get(Constants.TASKS);					
			}else{
				taskList = new ArrayList<Map<String,Object>>();
			}
			Map<String, Object> commentpageMap= new HashMap<String,Object>(4);
			Matcher commentpageMatcher = PATTERN_COMMENTPAGE.matcher(htmlData);
			String id = null;
			String url = unit.getUrl();
			String link = "https://api.yii.dgtle.com/v2/comment?tid=";
			int index = url.indexOf("token=&");
			if(index > 0){
				link = "https://api.yii.dgtle.com/v2/comment?token=&tid=";
			}
			while(commentpageMatcher.find()){
				id = commentpageMatcher.group(1);
				link = link + id + "&page=1";
				commentpageMap.put(Constants.LINK, link);
				commentpageMap.put(Constants.RAWLINK, link);
				commentpageMap.put(Constants.LINKTYPE, "newscomment");
				taskList.add(commentpageMap);
				resultData.put(Constants.COMMENT_URL, link);
				resultData.put(Constants.TASKS, taskList);
			}
		}
	}

}
