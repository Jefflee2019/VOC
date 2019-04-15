package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点：数字尾巴（新闻）
 * 功能：获取评论
 * @author dph 2017年11月29日
 *
 */
public class NdgtleCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(NdgtleCommentJson.class);
//	private static final Pattern PATTERN_ITEM = Pattern.compile("<item>(.|\n)*?(<reply/>|</reply>)(\n)*</item>");
	private static final Pattern PATTERN_ITEM = Pattern.compile("<item>(.|\n)*?(<reply/>|</reply>)");
//	private static final Pattern PATTERN_ITEM = Pattern.compile("<item>(.|\n)*?</item>");
	private static final Pattern PATTERN_MESSAGE = Pattern.compile("<message>((.|\n)*?)</message>");
	private static final Pattern PATTERN_DATE = Pattern.compile("<date>((.|\n)*?)</date>");
	private static final Pattern PATTERN_AUTHOR = Pattern.compile("<author>((.|\n)*?)</author>");
	private static final Pattern PATTERN_PAGE = Pattern.compile("page=\\d+");
	private static final Pattern PATTERN_TOTAL = Pattern.compile("<total>(\\d+)</total>");
	private static final Pattern PATTERN_PID = Pattern.compile("<pid>(\\d+)</pid>");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		JsonParserResult result = new JsonParserResult();
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			if(null == json || json.equals("")){
				return result;
			}
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,unit);
			}catch(Exception e){
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}
		// 组装返回结果
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	/**
	 * 从json中提取信息
	 * @param parsedata
	 * @param json
	 * @param unit 
	 */
	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
		LOG.info("*********************************");
		LOG.info(json);
		Matcher itemM = PATTERN_ITEM.matcher(json);
//		Matcher itemMT = PATTERN_ITEM_T.matcher(json);
		Matcher totalM = PATTERN_TOTAL.matcher(json);
		List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		List<Map<String, Object>> comments =null;
		if(parsedata.get(Constants.COMMENTS) != null){
			comments = (List<Map<String,Object>>) parsedata.get(Constants.COMMENTS);					
		}else{
			comments = new ArrayList<Map<String,Object>>();
		}
		String pid = null;
		getComments(itemM, pid,comments);
//		getComments(itemMT, pid,comments);
//		while(itemMT.find()){
//			String item = itemMT.group(0);
//			LOG.info("+++++++++++++++3333333333333333333333++++++++++");
//			LOG.info(item);
//			Matcher pidM = PATTERN_PID.matcher(item);
//			if(pidM.find() && !pidM.group(1).equals(pid)){
//				pid = pidM.group(1);
//				String message = null;
//				String date = null;
//				String replyusername = null;
//				//获取评论内容
//				Matcher messageM = PATTERN_MESSAGE.matcher(item);
//				while(messageM.find()){
//					message = messageM.group(1);
//				}
//				//获取评论日期
//				Matcher dateM = PATTERN_DATE.matcher(item);
//				if(dateM.find()){
//					date = dateM.group(1);
//					//处理回复时间 "<date>2 周前</date>...
//					int index = date.indexOf("周前");
//					if(index > 0){
//						int num = Integer.parseInt(date.replace("周前", "").trim());
//						num = num * 7;
//						date = num + "天前";
//					}
//					date = ConstantFunc.convertTime(date);
//				}
//				//获取评论人名称
//				Matcher replyusernameM = PATTERN_AUTHOR.matcher(item);
//				if(replyusernameM.find()){
//					replyusername = replyusernameM.group(1);
//				}
//				Map<String,Object> itemMap =new HashMap<String,Object>(16);
//				
//				itemMap.put(Constants.COMMENT_CONTENT, message);
//				itemMap.put(Constants.USERNAME, replyusername);
//				itemMap.put(Constants.COMMENT_TIME, date);
//				comments.add(itemMap);
//			}
//			
//		}
//		while(itemM.find()){
//			String item = itemM.group(0);
//			LOG.info("---------------------------------------");
//			LOG.info(item);
//			Matcher pidM = PATTERN_PID.matcher(item);
//			if(pidM.find() && !pidM.group(1).equals(pid)){
//				pid = pidM.group(1);
//				String message = null;
//				String date = null;
//				String replyusername = null;
//				//获取评论内容
//				Matcher messageM = PATTERN_MESSAGE.matcher(item);
//				while(messageM.find()){
//					message = messageM.group(1);
//				}
//				//获取评论日期
//				Matcher dateM = PATTERN_DATE.matcher(item);
//				if(dateM.find()){
//					date = dateM.group(1);
//					//处理回复时间 "<date>2 周前</date>...
//					int index = date.indexOf("周前");
//					if(index > 0){
//						int num = Integer.parseInt(date.replace("周前", "").trim());
//						num = num * 7;
//						date = num + "天前";
//					}
//					date = ConstantFunc.convertTime(date);
//				}
//				//获取评论人名称
//				Matcher replyusernameM = PATTERN_AUTHOR.matcher(item);
//				if(replyusernameM.find()){
//					replyusername = replyusernameM.group(1);
//				}
//				Map<String,Object> itemMap =new HashMap<String,Object>(16);
//				
//				itemMap.put(Constants.COMMENT_CONTENT, message);
//				itemMap.put(Constants.USERNAME, replyusername);
//				itemMap.put(Constants.COMMENT_TIME, date);
//				comments.add(itemMap);
//			}
//			
//		}
		
		parsedata.put("comments", comments);
//		下一页URL
		if(totalM.find()){
			String total = totalM.group(1);
			if(total != null && !total.equals("")){
				//		下一页URL
				String link = null;
				String page = null;
				int nextpage = 0;
				Matcher pageM = PATTERN_PAGE.matcher(unit.getUrl());
				while(pageM.find()){
					page = pageM.group(0);
					page = page.replace("page=", "");
				}
				if(null != page && Integer.valueOf(page) < Integer.valueOf(total)){
					nextpage = Integer.valueOf(page) + 1;
					String url = unit.getUrl().replaceAll("page=\\d+", "page=");
					link = url + nextpage;
					Map<String, Object> nextpageMap= new HashMap<String,Object>();
					nextpageMap.put(Constants.LINK, link);
					nextpageMap.put(Constants.RAWLINK, link);
					nextpageMap.put(Constants.LINKTYPE, "newscomment");
					taskList.add(nextpageMap);
					parsedata.put("nextpage", nextpageMap);
					parsedata.put("tasks", taskList);
				}
			}
		}
		
	}
	
	
	public static void getComments(Matcher itemM,String pid,List<Map<String, Object>> comments){
		while(itemM.find()){
			String item = itemM.group(0);
			LOG.info("--------------------------------------------------");
			LOG.info(item);
			Matcher pidM = PATTERN_PID.matcher(item);
			if(pidM.find() && !pidM.group(1).equals(pid)){
				pid = pidM.group(1);
				String message = null;
				String date = null;
				String replyusername = null;
				//获取评论内容
				Matcher messageM = PATTERN_MESSAGE.matcher(item);
				while(messageM.find()){
					message = messageM.group(1);
				}
				//获取评论日期
				Matcher dateM = PATTERN_DATE.matcher(item);
				if(dateM.find()){
					date = dateM.group(1);
					//处理回复时间 "<date>2 周前</date>...
					int index = date.indexOf("周前");
					if(index > 0){
						int num = Integer.parseInt(date.replace("周前", "").trim());
						num = num * 7;
						date = num + "天前";
					}
					date = ConstantFunc.convertTime(date);
				}
				//获取评论人名称
				Matcher replyusernameM = PATTERN_AUTHOR.matcher(item);
				if(replyusernameM.find()){
					replyusername = replyusernameM.group(1);
				}
				Map<String,Object> itemMap =new HashMap<String,Object>(16);
				
				itemMap.put(Constants.COMMENT_CONTENT, message);
				itemMap.put(Constants.USERNAME, replyusername);
				itemMap.put(Constants.COMMENT_TIME, date);
				comments.add(itemMap);
			}
			
		}
	}
}
