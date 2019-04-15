package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DateUtil;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：站长之家(新闻)
 * 
 * 主要功能：获取评论信息
 * 
 * @author bfd_03
 *
 */
public class NchinazCommentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(NchinazCommentJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		
		// 遍历dataList
		for(Object obj:dataList){
			JsonData data = (JsonData)obj;
			// 判断该ajax数据是否下载成功
			if(!data.downloadSuccess()){
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,data.getUrl(),unit);
			}catch(Exception e){
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}	
		
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put(Constants.TASKS, taskList);
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if(obj instanceof Map){
			Map objMap = (HashMap)obj;
			Map pramMap = new HashMap();
			//详情页评论信息
			List comments = null;
			if(objMap.containsKey("listData")){
				Map listData = (HashMap)objMap.get("listData");
				comments = (ArrayList)listData.get("comments");
				pramMap.put("topic_id", listData.get("topic_id"));
			}else if(objMap.containsKey("comments")){
				comments = (ArrayList)objMap.get("comments");
				pramMap.put("topic_id", objMap.get("topic_id"));
			}else{
				return;
			}
			List commentsDataList = new ArrayList();
			Map commentsDataMap = null;
			for(Object tempObj:comments){
				Map commentsMap = (HashMap)tempObj;
				commentsDataMap = new HashMap();

				//获取评论时间
				String sCreateTime = commentsMap.get("create_time").toString();
				sCreateTime = DateUtil.getDateTime(Long.parseLong(sCreateTime));
				//获取评论人姓名
				Map passportMap = (HashMap)commentsMap.get("passport");
				String nickname = passportMap.get("nickname").toString();
				//评论的引用
				List referCommentsList = (List)commentsMap.get("comments");
				if(referCommentsList != null && !referCommentsList.isEmpty()){
					Map referCommentsDataMap = new HashMap();
					Map referCommentsMap = (Map) referCommentsList.get(referCommentsList.size() - 1);
					referCommentsDataMap.put(Constants.REFER_COMM_USERNAME, ((HashMap)referCommentsMap.get("passport")).get("nickname"));
					referCommentsDataMap.put(Constants.REFER_COMM_CONTENT, referCommentsMap.get("content"));
					referCommentsDataMap.put(Constants.REFER_UP_CNT, referCommentsMap.get("support_count"));
					referCommentsDataMap.put(Constants.REFER_DOWN_CNT, referCommentsMap.get("oppose_count"));
					
					commentsDataMap.put(Constants.REFER_COMMENTS, referCommentsDataMap);
				}
				
				commentsDataMap.put(Constants.COMMENTER_NAME, nickname);//评论人姓名
				commentsDataMap.put(Constants.COMMENT_TIME, sCreateTime);//评论时间
				commentsDataMap.put(Constants.COMMENT_CONTENT, commentsMap.get("content"));//评论内容
				String sCity = (String) commentsMap.get("ip_location");
				if (sCity != null && !sCity.isEmpty()) {
					commentsDataMap.put(Constants.CITY, commentsMap.get("ip_location"));//评论IP所在城市(来自城市)
				}
				commentsDataMap.put(Constants.UP_CNT, commentsMap.get("support_count"));//评论顶的人数
				commentsDataMap.put(Constants.DOWN_CNT, commentsMap.get("oppose_count"));//评论踩的人数
				
				commentsDataList.add(commentsDataMap);
			}
			
			parsedata.put(Constants.COMMENTS, commentsDataList);			
			
			//拼接下一页链接 
			getNextpageUrl(parsedata, unit, pramMap, taskList);
		}
		
	}
	
	/**
	 * 拼接下一页的URL的生成任务
	 * 评论页第一页URL：http://changyan.sohu.com/node/html?client_id=cyqTnXrVX&topicurl=http://www.donews.com/it/201602/2918094.shtm
	 * 第二页以后的URL：http://changyan.sohu.com/api/2/topic/comments?client_id=cyqTnXrVX&topic_id=1047674308&page_size=30&page_no=2
	 * @param parsedata
	 * @param dataMap(json数据的集合)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(Map<String, Object> parsedata, ParseUnit unit, Map dataMap, List taskList) {
		if (dataMap.get("topic_id") == null	|| dataMap.get("topic_id").equals("")) {
			return;
		}
		String url = (String) unit.getUrl();
		String sClientId = "";
		String sTopicId = dataMap.get("topic_id").toString();
		int pageNo = 0;
		int pageSize= 0;
		
		Pattern pattern = Pattern.compile("http://changyan.sohu.com/api/2/topic/comments\\?client_id=(\\w+)&topic_id=(\\d+)&page_size=(\\d+)&page_no=(\\d+)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
		
		
		if (matcher.find()) {
			sClientId = matcher.group(1);
			sTopicId = matcher.group(2);
			pageSize = Integer.parseInt(matcher.group(3));
			pageNo = Integer.parseInt(matcher.group(4)) + 1;
		}else{							
			pattern = Pattern.compile("http://changyan.sohu.com/node/html\\?client_id=(\\w+)");
			matcher = pattern.matcher(url);
			if (matcher.find()) {
				sClientId = matcher.group(1);
			}
		}

		// 若未取到参数，则给予默认值
		if (pageSize == 0) {
			pageSize = 30;
		}
		if (pageNo == 0) {
			pageNo = 2;
		}
		
		if(!sClientId.equals("")){
			Map<String,Object> nexpageTask = new HashMap<String,Object>();
			StringBuffer sb = new StringBuffer();
			sb.append("http://changyan.sohu.com/api/2/topic/comments?client_id="+sClientId);
			sb.append("&topic_id="+sTopicId);
			sb.append("&page_size="+pageSize);
			sb.append("&page_no="+pageNo);

			// 若当前页面评论数小于页面的评论总数，则不取下一页
			List comments = (List) parsedata.get(Constants.COMMENTS);
			if (comments.size() < pageSize) {
				return;
			}

			String nextpageUrl = sb.toString();
			nexpageTask.put(Constants.LINK, nextpageUrl);
			nexpageTask.put(Constants.RAWLINK, nextpageUrl);
			nexpageTask.put(Constants.LINKTYPE, "newscomment");		
			taskList.add(nexpageTask);
			
			parsedata.put(Constants.NEXTPAGE, nexpageTask);
			parsedata.put(Constants.TASKS, taskList);
		}
		
	}

}
