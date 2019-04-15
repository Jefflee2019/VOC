package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：腾讯手机/数码(Nqqdigi)
 * 
 * 主要功能：获取评论信息
 * 
 * @author bfd_03
 *
 */
public class NqqdigiCommentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(NqqdigiCommentJson.class);
	
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
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
			
			if(obj instanceof Map){	
				Object dataObj = ((Map) obj).get("data");
				if(dataObj!=null && !dataObj.equals("")){
					Map data = (Map)dataObj;
					//详情页--评论信息
					if(data.containsKey("commentid")){
						if(data.get("commentid") instanceof List){
							List commentList = (List)data.get("commentid");
							List commentDataList = new ArrayList();
							Map tempMap = null;
							for(Object objTemp:commentList){
								Map commentMap = (Map)objTemp;
								tempMap = new HashMap();
								
								tempMap.put(Constants.USERNAME, ((Map)commentMap.get("userinfo")).get("nick"));//评论者姓名
 								tempMap.put(Constants.COMMENT_CONTENT, commentMap.get("content"));//评论内容
 								String sCommentTime = (String) commentMap.get("timeDifference");
								tempMap.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(sCommentTime));//评论时间
								tempMap.put(Constants.UP_CNT, commentMap.get("up"));//评论顶的人数	
								tempMap.put(Constants.COM_REPLY_CNT, commentMap.get("rep"));//对于回复的回复数
								//Map userinfo = (HashMap)commentMap.get("userinfo");
								//tempMap.put(Constants.CITY, userinfo.get("region"));//评论人所在城市	
								
								commentDataList.add(tempMap);
							}
							parsedata.put(Constants.COMMENTS, commentDataList);
							getNextpageUrl(parsedata, data);
						}
					}	
				}else{
					parsedata.put(Constants.TASKS, new ArrayList());
				}							
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error("json parse error or json is null");
		}
	}
	
	/**
	 * 拼接下一页的URL的生成任务
	 * 评论页第一页URL：http://coral.qq.com/article/1164663232/comment?commentid=0&reqnum=10
	 * 第二页以后的URL：http://coral.qq.com/article/1164663232/comment?commentid=6015035776291119011&reqnum=20
	 * @param parsedata
	 * @param dataMap(json数据的集合)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(Map<String, Object> parsedata, Map dataMap) {
		
		String last = (String) dataMap.get("last");
		int targetid = (int) dataMap.get("targetid");// "targetid": 1164663232
		boolean hasnext = (boolean) dataMap.get("hasnext");
		StringBuffer sb = new StringBuffer();
		
		List taskList = new ArrayList();
		if(hasnext){
			Map nexpageTask = new HashMap();
			
			//拼接下一页链接   http://coral.qq.com/article/1164663232/comment?commentid=6015035776291119011&reqnum=20
			sb.append("http://coral.qq.com/article/"+targetid);
			sb.append("/comment?commentid="+last);
			sb.append("&reqnum=20");
			
			String nextpageUrl = sb.toString();
			nexpageTask.put(Constants.LINK, nextpageUrl);
			nexpageTask.put(Constants.RAWLINK, nextpageUrl);
			nexpageTask.put(Constants.LINKTYPE, "newscomment");		
			taskList.add(nexpageTask);
			
			parsedata.put(Constants.NEXTPAGE, nexpageTask);
		}
		parsedata.put(Constants.TASKS, taskList);
	}

}
