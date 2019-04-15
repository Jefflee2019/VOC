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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：比价网(新闻)
 * 
 * 主要功能：获取评论信息
 * 
 * 评论使用后处理插件，此插件没有使用
 */
@Deprecated
public class NepriceCommentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(NepriceCommentJson.class);
	
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
			//详情页评论信息
			List comments = null;
			if(objMap.containsKey("Data")){
				comments = (ArrayList)objMap.get("Data");
			}else{
				return;
			}
			List commentsDataList = new ArrayList();
			Map commentsDataMap = null;
			for(Object tempObj:comments){
				Map commentsMap = (HashMap)tempObj;
				commentsDataMap = new HashMap();

				//获取评论时间
				String sCreateTime =(String)commentsMap.get("createdate");
				//获取评论人姓名
				String sNickname = (String)commentsMap.get("LoginName");
				//获取评论内容
				String sCommentContent = (String)commentsMap.get("CommentContent");
				int indexBegin = sCommentContent.indexOf("<");
				int indexEnd = sCommentContent.indexOf(">");
				if(indexBegin >= 0 && indexBegin < indexEnd){					
					sCommentContent = sCommentContent.replaceFirst("<img.*?>", "");
				}
				
				//TODO
				/*此处需要处理评论的回复信息*/
				
				commentsDataMap.put(Constants.COMMENTER_NAME, sNickname);//评论人姓名
				commentsDataMap.put(Constants.COMMENT_TIME, sCreateTime);//评论时间
				commentsDataMap.put(Constants.COMMENT_CONTENT, sCommentContent);
				commentsDataMap.put(Constants.CITY, commentsMap.get("IpCountry"));//评论IP所在城市(来自城市)
				commentsDataMap.put(Constants.UP_CNT, commentsMap.get("Agree"));//评论顶的人数
				commentsDataMap.put(Constants.DOWN_CNT, commentsMap.get("DisAgree"));//评论踩的人数
				
				commentsDataList.add(commentsDataMap);
			}
			
			parsedata.put(Constants.COMMENTS, commentsDataList);
			
			if(commentsDataList.size() >= 5){				
				getNextpageUrl(url, parsedata);
			}
		}
		
	}
	
	
	/**
	 * 拼接下一页的URL的生成任务
	 * @param parsedata
	 * @param dataMap(json数据的集合)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(String url, Map<String, Object> parsedata) {
		StringBuffer sb = new StringBuffer();
		List taskList = new ArrayList();
		Map nexpageTask = new HashMap();
			
		//拼接下一页链接   http://www.wyh.tv/lib/handler/GetComment.ashx?act=get&aid=15592&pg=1
		Pattern pattern = Pattern.compile("(http://www.wyh.tv/lib/handler/GetComment.ashx\\?act=get&aid=\\d+&pg=)(\\d+)");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			int curPage = Integer.parseInt(matcher.group(2));
			sb.append(matcher.group(1));
			sb.append(curPage+1);
		}
		if(sb.length() > 0){			
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
