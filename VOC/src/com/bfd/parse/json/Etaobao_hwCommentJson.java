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
 * 解析评论页内容
 * @author BFD_499
 *
 */
public class Etaobao_hwCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(Etaobao_hwCommentJson.class);
	private static final int PAGESIZE = 20;
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				} 
				
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = Constants.JSONPROCESS_FAILED;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		LOG.info("after json is " + parsedata);
		return result;
	}
	
	public void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("json format conversion error in parseObject(json) method", e);
		}
		if (obj instanceof Map) {
			Map map = (HashMap) obj;
			// 处理评论详细内容
			if (map != null && map.containsKey("comments")) {
				ArrayList<Map<String, Object>> comments = (ArrayList<Map<String, Object>>) map
						.get("comments");
				ArrayList<String> commentImgList = new ArrayList<String>();
				ArrayList<String> replyCommentImgList = new ArrayList<String>();
				ArrayList<Map<String, Object>> photos = new ArrayList<Map<String, Object>>();
				ArrayList<Map<String, Object>> appendPhotos = new ArrayList<Map<String, Object>>();
				ArrayList<Map<String, Object>> temp = new ArrayList<Map<String, Object>>();
				if (comments != null && !comments.isEmpty()) {
					for (Map<String, Object> comment : comments) {
						Map<String, Object> item = new HashMap<String, Object>();
						String commentTime = (String) comment.get("date");
						commentTime = commentTime.replace("年", "-")
								.replace("月", "-").replace("日", "");
						String commentContent = (String) comment.get("content");
						item.put(Constants.COMMENT_CONTENT, commentContent);
						String useful = String.valueOf(comment.get("useful")
								.toString());
						item.put(Constants.UP_CNT, useful);
						Map<String, Object> userInfo = (Map<String, Object>) comment
								.get("user");
						String commenterName = (String) userInfo.get("nick");
						item.put(Constants.COMMENTER_NAME, commenterName);
						// 评论图片
						photos = (ArrayList<Map<String, Object>>) comment
								.get("photos");
						for (Map<String, Object> photo : photos) {
							String photoUrl = (String) photo.get("url");
							commentImgList.add("http:" + photoUrl);
						}
						item.put(Constants.COMMENT_IMG, commentImgList);
						// 追评里的信息
						Map<String, Object> append = (Map<String, Object>) comment
								.get("append");
						if (append != null && append.containsKey("content")) {
							String appendCommentContent = (String) append
									.get("content");
							item.put(Constants.APPEND_COMMENT_CONTENT,
									appendCommentContent);
							appendPhotos = (ArrayList<Map<String, Object>>) append
									.get("photos");
							for (Map<String, Object> photo : appendPhotos) {
								String photoUrl = (String) photo.get("url");
								replyCommentImgList.add(photoUrl);
							}
							item.put(Constants.APPEND_COMMENT_IMG,
									replyCommentImgList);
						}
						// 回复信息
						Map<String, Object> reply = (Map<String, Object>) comment
								.get("reply");
						if (reply != null && reply.containsKey("content")) {
							String commentReply = (String) reply.get("content");
							item.put(Constants.COMMENT_REPLY_CONTENT,
									commentReply);
						}
						// 获取购买机型信息
						Map<String, Object> action = (Map<String, Object>) comment
								.get("auction");
						if (action != null && action.containsKey("sku")) {
							String phoneInfo = (String) action.get("sku");
							String[] phoneInfoS = phoneInfo.split(" ");
							for (String tmp : phoneInfoS) {
								if (tmp.contains("机身颜色")) {
									item.put(Constants.COLOR, tmp.split(":")[1]);
								}
								if (tmp.contains("套餐类型")) {
									item.put(Constants.BUY_TYPE,
											tmp.split(":")[1]);
								}
								if (tmp.contains("机身内存")) {
									item.put(Constants.BUY_RAM,
											tmp.split(":")[1]);
								}
							}
						}
						temp.add(item);
					}
				}
				parsedata.put(Constants.COMMENTS, temp);
			}
			// nextpage
			if (map.containsKey("total")) {
				int count = Integer.valueOf(map.get("total").toString());
				int pageNum = 1;
				int pageCount = count % PAGESIZE == 0 ? (count / PAGESIZE)
						: count / (PAGESIZE + 1);
				Pattern p = Pattern.compile("PageNum=(\\d+)");
				Matcher m = p.matcher(url);
				while (m.find()) {
					pageNum = Integer.valueOf(m.group(1));
				}
				String nextpage = null;
				if (pageNum < pageCount) {
					nextpage = url.split("PageNum=")[0]
							+ "PageNum="
							+ (pageNum + 1)
							+ "&pageSize=20&rateType=&orderType=sort_weight&showContent=1&attribute=&folded=0&callback=jsonp_tbcrate_reviews_list";
					Map<String, Object> commentTask = new HashMap<String, Object>();
					commentTask.put(Constants.LINK, nextpage);
					commentTask.put(Constants.RAWLINK, nextpage);
					commentTask.put(Constants.LINKTYPE, "eccomment");
					taskList.add(commentTask);
					parsedata.put(Constants.NEXTPAGE, commentTask);
				}
			}
		}
	}
}
