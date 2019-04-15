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
 * @site 一生一世网(Nmylove1314)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class Nmylove1314CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Nmylove1314CommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(), e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {

			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Object obj = JsonUtil.parseObject(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList);
			if (obj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) obj;
				// 评论数
				String commentid = null;
				// 判断是否有下一页
				boolean flag = true;
				String targetid = null;
				if (data.containsKey("data")) {
					Map<String, Object> dataMap = (Map<String, Object>) data.get("data");
					if (dataMap.containsKey("last")) {
						commentid = dataMap.get("last").toString();
					}
					if (dataMap.containsKey("hasnext")) {
						flag = (boolean) dataMap.get("hasnext");
					}
					if (dataMap.containsKey("targetid")) {
						targetid = String.valueOf(dataMap.get("targetid")) ;
					}
					// 评论内容
					if (dataMap.containsKey("commentid")) {
						List<Map<String, Object>> commentidList = (List<Map<String, Object>>) dataMap.get("commentid");
						// 用于存放组装数据
						List<Map<String, Object>> commentList = new ArrayList<Map<String, Object>>();
						for (Map<String, Object> comment : commentidList) {
							// 用于存放临时数据的map
							Map<String, Object> tempMap = new HashMap<String, Object>();
							// 评论时间
							if (comment.containsKey("time")) {
								String time = ConstantFunc.normalTime(comment.get("time").toString());
								tempMap.put(Constants.COMMENT_TIME, time);
							}

							// 评论内容
							if (comment.containsKey("content")) {
								String content = comment.get("content").toString();
								tempMap.put(Constants.COMMENT_CONTENT, content);
							}

							if (comment.containsKey("userinfo")) {
								Map<String, Object> userinfo = (Map<String, Object>) comment.get("userinfo");
								// 评论人昵称
								if (userinfo.containsKey("nick")) {
									String nick = userinfo.get("nick").toString();
									tempMap.put(Constants.USERNAME, nick);
								}
								// 评论人所在城市
								if (userinfo.containsKey("region")) {
								}
								String region = userinfo.get("region").toString();
								tempMap.put(Constants.CITY, region);
							}
							commentList.add(tempMap);
						}
						parsedata.put(Constants.COMMENTS, commentList);
					}
				}

				// cal nextpage
				// comm_url:http://coral.qq.com/article/1263577794/comment?commentid=0&reqnum=10
				if (flag) {
					StringBuffer sb = new StringBuffer();
					sb.append("http://coral.qq.com/article/"+ targetid);
					sb.append("/comment?commentid=" + commentid);
					sb.append("&reqnum=20");
					String nextPage = sb.toString();
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put(Constants.LINK, nextPage);
					nextpageTask.put(Constants.RAWLINK, nextPage);
					nextpageTask.put(Constants.LINKTYPE, "newscomment");
					taskList.add(nextpageTask);
					parsedata.put(Constants.NEXTPAGE, nextpageTask);
					parsedata.put(Constants.TASKS, taskList);
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}
}
