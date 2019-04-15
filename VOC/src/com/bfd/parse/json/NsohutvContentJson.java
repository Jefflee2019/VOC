package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class NsohutvContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NsohutvContentJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) { // 遍历dataList
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				json = new String(data.getData(), "UTF-8");
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("taskdat url=" + taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, taskList); // tasks 字段是必须的
		if(parsedata.containsKey(Constants.COMMENT_URL)) { // 处理评论任务
			String url = (String) parsedata.get(Constants.COMMENT_URL);
			Map<String, Object> task = new HashMap<>();
			task.put(Constants.LINK, url);
			task.put(Constants.RAWLINK, url);
			task.put(Constants.LINKTYPE, "newscomment");
			taskList.add(task);
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) throws Exception {
		if(json.contains("vids:")) {
			json = json.substring(json.indexOf("vids:") + 5, json.length() - 1);
		}
		Object obj = JsonUtil.parseObject(json);
		if(obj == null) return;
		if(url.contains("://score.")) { // 获取点赞数
			//http://score.my.tv.sohu.com/digg/get.do?vid=84667752&type=9001
			if (obj instanceof Map) {
				Map<String,Object> map = (Map<String,Object>) obj;
				//"upCount":32,"downCount":11
				if (map.containsKey("upCount")) { // 视频支持数
					parsedata.put(Constants.NEW_FLOWERNUM, map.get("upCount"));
				}
				if (map.containsKey("downCount")) { // 视频反对数
					parsedata.put(Constants.NEW_EGGNUM, map.get("downCount"));
				}
			}
		} else if(url.contains("://vstat.") || url.contains("://count.vrs")) { // 获取播放量
			/* 
			 * http://count.vrs.sohu.com/count/queryext.action?vids=3527463&plids=9272357
			 * {vids:{"3527463":{"total":249992,"today":10}}}
			 * 
			 * http://vstat.my.tv.sohu.com/dostat.do?method=getVideoPlayCount&v=84667752&n=c
			 * [{"id":"84667752","count":1768631,"refers":"0"}]
			 */
			Map<String,Object> map = null;
			if (obj instanceof List) {
				map =  ((List<Map<String,Object>>) obj).get(0);
			} else if(obj instanceof Map) {
				map = (Map<String,Object>) obj;
			}
			if(map != null) {
				if(map.containsKey("count")) {
					parsedata.put(Constants.PLAY_CNT, map.get("count"));
				} else if(map.size() == 1) {
					for(Object item : map.values()) {
						map = (Map<String, Object>) item;
						parsedata.put(Constants.PLAY_CNT, map.get("total"));
						break;
					}
				}
			}
		} else if(url.contains("://my.tv.")) { // 获取视频上传时间
			//http://my.tv.sohu.com/interaction/get/getListByVid.do?vid=84667752
			//{"status":1,"data":[{"basicId":9,"type":3,"addTime":"Thu Jul 28 09:31:02 CST 2016","isSys":0}]}
			if (obj instanceof Map) {
				Map<String,Object> map = (Map<String,Object>) obj;
				if (map.containsKey("data")) {
					obj = map.get("data");
					map = null;
					if(obj instanceof List) {
						map =  ((List<Map<String,Object>>) obj).get(0);
					} else if(obj instanceof Map) {
						map = (Map<String,Object>) obj;
					}
					if(map != null && map.containsKey("addTime")) {
						String addTime = (String) map.get("addTime");
						if(addTime != null) {
							SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
							addTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(addTime));
							parsedata.put(Constants.POST_TIME, addTime);
						}
					}
				}
			}
		} else if(url.contains("changyan.sohu.com")) {
			if (obj instanceof Map) {
				Map<String,Object> map = (Map<String,Object>) obj;
				/*
				 * 在视频页的源码里有两个变量需要用到：var vid="3527463"; var cid="7";
				 * 在jsEngine里得到topic_source_id的值：
				 * var topic_source_id = "9001" == cid ? "bk" + vid : "vp" + vid;
				 * 最后组成第一页的评论链接，最重要的是要获取到topic_id，这样才能拿到下一页的评论
				 * 
				 * 在插件里得到评论页的json，拿到下面这些值
				 * {"cmt_sum":2, "participation_sum": 8, "topic_id": 2381973652, "total_page_no": 1}
				 * 最终判断是否需要抓取评论，然后使用这些值组成评论抓取链接，抓取剩余页的评论内容
				 */
				Integer cmtSum = (Integer) map.get("cmt_sum");
				if(cmtSum != null && cmtSum > 0) {
					parsedata.put(Constants.MSG_CNT, cmtSum); // 留言数
					parsedata.put(Constants.JREPLY_CNT, map.get("participation_sum")); // 参与评论的总人数
					/*
					 * 评论url转换
					 * http://changyan.sohu.com/api/2/topic/load?client_id=cyqyBluaj&topic_url=http%3A%2F%2Ftv.sohu.com%2F20170112%2Fn478483386.shtml&topic_source_id=vp3527463&page_size=10
					 * http://changyan.sohu.com/api/2/topic/comments?client_id=cyqyBluaj&page_no=1&page_size=10&topic_id=2381973652&order_by=time
					 */
					String commUrl = url.substring(0, url.indexOf("&")).replace("load", "comments")
							+ "&topic_id=" + map.get("topic_id")
							+ "&page_size=10&page_no=1&total_page_no=" + map.get("total_page_no");
					parsedata.put(Constants.COMMENT_URL, commUrl);
				} else {
					parsedata.put(Constants.MSG_CNT, 0);
				}
			}
		}
	}
}