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
 * 站点名：it168(新闻)
 * 
 * 主要功能：获取评论信息
 * 
 * @author bfd_03
 *
 */
public class Nit168CommentJson_bak implements JsonParser{

	private static final Log LOG = LogFactory.getLog(Nit168CommentJson_bak.class);
	
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
				e.printStackTrace();
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
			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put(Constants.TASKS, taskList);
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			System.out.println(map);
			// 回复信息
			if (map.containsKey("parentPosts")) {
				Map<String, Object> myObj = (Map<String, Object>) map.get("parentPosts");
				Map<String, Object> dataMap = (Map<String, Object>) myObj;

				Object[] objArray = dataMap.keySet().toArray();

				if (objArray.length > 0) {
					List<Map<String,Object>> dataDataList = new ArrayList<Map<String,Object>>();
					for (int i = 0; i < objArray.length; i++) {
						Map<String, Object> commentMap = (Map<String, Object>) dataMap
								.get((String) objArray[i]);
						Map<String, Object> temp = new HashMap<String, Object>();
						
						//剔除掉回复和转发的数据
						String source = (String)commentMap.get("source");
						if(!source.equals("duoshuo")){ 
							continue;  //对于不是来自该站点的评论剔除，即排除掉重复的转发微博的评论数据
						}

						temp.put(Constants.COMMENT_CONTENT,commentMap.get("message").toString().trim());// 回复内容
						// 回复时间
						String time = (String) commentMap.get("created_at");
						time = time.substring(0, 10) + " " + time.substring(11, 19);
						temp.put(Constants.UP_CNT, commentMap.get("likes").toString().trim());
						temp.put(Constants.COMMENT_TIME, time.trim());
						temp.put(Constants.USERNAME,((Map<String, Object>) commentMap.get("author")).get("name")
								.toString().trim());// 回复人姓名
						dataDataList.add(temp);
					}
					parsedata.put(Constants.COMMENTS, dataDataList);
				}

				//拼接下一页链接
				getNextpageUrl(parsedata, unit, map, taskList);
			}

		}
	}
	
	/**
	 * 拼接下一页的URL的生成任务
	 * 评论页第一页URL：http://it168.duoshuo.com/api/threads/listPosts.json?thread_key=1720547&channel_key=6
	 * 第二页以后的URL：http://it168.duoshuo.com/api/threads/listPosts.json?thread_id=1328796137602985145&limit=10&page=2
	 * @param parsedata
	 * @param dataMap(json数据的集合)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getNextpageUrl(Map<String, Object> parsedata, ParseUnit unit, Map dataMap, List taskList) {
		String url = (String) unit.getTaskdata().get("url");
		String thread_id = "";
		int pageNo = 0;
		int pageSize= 0;
		StringBuffer sb = new StringBuffer();
		
		Pattern pattern = Pattern.compile("http://it168.duoshuo.com/api/threads/listPosts.json\\?thread_id=(\\d+)&limit=(\\d+)&page=(\\d+)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
		
		
		if (matcher.find()) {
			thread_id = matcher.group(1);
			pageSize = Integer.parseInt(matcher.group(2));
			pageNo = Integer.parseInt(matcher.group(3)) + 1;
		}

		if (thread_id.equals("")) {
			Map thread = (HashMap) dataMap.get("thread");
			thread_id = (String) thread.get("thread_id");
		}

		// 若未取到参数，则给予默认值
		if (pageSize == 0) {
			pageSize = 20;
		}
		if (pageNo == 0) {
			pageNo = 2;
		}
		
		Map<String,Object> nexpageTask = new HashMap<String,Object>();
		if (!thread_id.equals("")) {
			sb.append("http://it168.duoshuo.com/api/threads/listPosts.json?thread_id="
					+ thread_id);
			sb.append("&limit=" + pageSize);
			sb.append("&page=" + pageNo);

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
