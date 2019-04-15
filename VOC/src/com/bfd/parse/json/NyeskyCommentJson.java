package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
 * 站点名：天极网
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class NyeskyCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NyeskyCommentJson.class);
	private static boolean isFirst = false;
	private static final int PAGESIZE = 30;
	
	@SuppressWarnings("unchecked")
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
		for (JsonData item : dataList) {
			if (!item.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(item, unit);
			Object obj = null;
			try {
				obj = JsonUtil.parseObject(json);
			} catch (Exception e) {
				LOG.error(e);
			}
			// 评论
			if (obj instanceof Map) {
				Map<String,Object> data = (Map<String,Object>) obj;
				String topicId = getTopicId(data);
				int cmtsum = 0;
				if (data.containsKey("listData")) {
					Map<String,Object> temp = (Map<String,Object>) data.get("listData");
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					if (temp != null && temp.containsKey("cmt_sum")) {
						cmtsum = (int) temp.get("cmt_sum");
					}
					parsedata.put(Constants.REPLY_CNT, cmtsum);
					if (temp != null && temp.containsKey("comments")) {
						List<Map<String,Object>> comments = (List<Map<String,Object>>) temp.get("comments");
						for (int i = 0; i < comments.size(); i++) {
							Map<String,Object> comm = new HashMap<String,Object>();
							comm.put(Constants.UP_CNT, ((Map<String,Object>) comments.get(i))
									.get("support_count"));
							comm.put(Constants.DOWN_CNT,
									((Map<String,Object>) comments.get(i)).get("oppose_count"));
							comm.put(Constants.CITY,
									((Map<String,Object>) comments.get(i)).get("ip_location"));
							comm.put(Constants.COMMENTER_IP,
									((Map<String,Object>) comments.get(i)).get("ip"));
							comm.put(Constants.COMMENT_CONTENT,
									((Map<String,Object>) comments.get(i)).get("content"));
							comm.put(Constants.COMMENT_TIME, timeformat(Long
									.parseLong(((Map<String,Object>) comments.get(i)).get(
											"create_time").toString())));
							// 评论人名称
							Map<String,Object> passport = (Map<String,Object>) ((Map<String,Object>) comments.get(i))
									.get("passport");
							comm.put(Constants.USERNAME,
									passport.get("nickname").toString());
							// 引用的回复内容
							Map<String,Object> referComm = new HashMap<String,Object>();
							List<Map<String,Object>> listRefer = (List<Map<String,Object>>) ((Map<String,Object>) comments.get(i))
									.get("comments");
							if (!listRefer.isEmpty()) {
								Map<String,Object> refer = (Map<String,Object>) listRefer.get(listRefer
										.size() - 1);
								// 引用的评论人名称
								Map<String,Object> referPassport = (Map<String,Object>) refer
										.get("passport");
								referComm.put(
										Constants.REFER_COMM_USERNAME,
										referPassport.get("nickname")
												.toString());
								referComm.put(Constants.REFER_COMM_CONTENT,
										refer.get("content"));
								referComm.put(
										Constants.REFER_COMM_TIME,
										timeformat(Long.parseLong(refer.get(
												"create_time").toString())));

							}
							comm.put(Constants.REFER_COMMENTS, referComm);
							list.add(comm);
						}
					}
					parsedata.put(Constants.COMMENTS, list);
				} else if (data.containsKey("comments")) {
					if (data.containsKey("cmt_sum")) {
						cmtsum = (int) data.get("cmt_sum");
					}
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					List<Map<String,Object>> comments = (List<Map<String,Object>>) data.get("comments");
					for (int i = 0; i < comments.size(); i++) {
						Map<String,Object> comm = new HashMap<String,Object>();
						comm.put(Constants.UP_CNT,
								((Map<String,Object>) comments.get(i)).get("support_count"));
						comm.put(Constants.DOWN_CNT,
								((Map<String,Object>) comments.get(i)).get("oppose_count"));
						comm.put(Constants.CITY,
								((Map<String,Object>) comments.get(i)).get("ip_location"));
						comm.put(Constants.COMMENTER_IP,
								((Map<String,Object>) comments.get(i)).get("ip"));
						comm.put(Constants.COMMENT_CONTENT,
								((Map<String,Object>) comments.get(i)).get("content"));
						comm.put(Constants.COMMENT_TIME, timeformat(Long
								.parseLong(((Map<String,Object>) comments.get(i)).get(
										"create_time").toString())));
						// 评论人名称
						Map<String,Object> passport = (Map<String,Object>) ((Map<String,Object>) comments.get(i))
								.get("passport");
						comm.put(Constants.USERNAME, passport.get("nickname")
								.toString());
						// 引用的回复内容
						Map<String,Object> referComm = new HashMap<String,Object>();
						List<Map<String,Object>> listRefer = (List<Map<String,Object>>) ((Map<String,Object>) comments.get(i))
								.get("comments");
						if (!listRefer.isEmpty()) {
							Map<String,Object> refer = (Map<String,Object>) listRefer
									.get(listRefer.size() - 1);
							// 引用的评论人名称
							Map<String,Object> referPassport = (Map<String,Object>) refer.get("passport");
							referComm.put(Constants.REFER_COMM_USERNAME,
									referPassport.get("nickname").toString());
							referComm.put(Constants.REFER_COMM_CONTENT,
									refer.get("content"));
							referComm.put(
									Constants.REFER_COMM_TIME,
									timeformat(Long.parseLong(refer.get(
											"create_time").toString())));

						}
						comm.put(Constants.REFER_COMMENTS, referComm);
						list.add(comm);
					}
					parsedata.put(Constants.COMMENTS, list);
				}

				String nextpage = getNextpageUrl(unit.getUrl(), topicId);
				if (isFirst) {
					if (cmtsum > PAGESIZE) {
						Map<String, Object> commentTask = new HashMap<String, Object>();
						commentTask.put(Constants.LINK, nextpage);
						commentTask.put(Constants.RAWLINK, nextpage);
						commentTask.put(Constants.LINKTYPE, "newscomment");
						taskList.add(commentTask);
						parsedata.put(Constants.NEXTPAGE, commentTask);
					}
				} else {
					int page = Integer.valueOf(getPage(unit.getUrl()));
					if (cmtsum - page * PAGESIZE > 0) {
						Map<String, Object> commenTask = new HashMap<String, Object>();
						commenTask.put(Constants.LINK, nextpage);
						commenTask.put(Constants.RAWLINK, nextpage);
						commenTask.put(Constants.LINKTYPE, "newscomment");
						taskList.add(commenTask);
						parsedata.put(Constants.NEXTPAGE, commenTask);
					}
				}

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

	private String getNextpageUrl(String url, String topicId) {
		String nextpage = null;
		// 判断是评论的第一页还是其他页
		if ((!url.contains("topic_id")) && (!url.contains("page_no"))) {
			isFirst = true;
			nextpage = "http://changyan.sohu.com/api/2/topic/comments?client_id=cyrBg7Hbp&topic_id="
					+ topicId + "&page_size=30&page_no=2";
		} else {
			nextpage = url.split("page_no")[0] + "page_no="
					+ (Integer.valueOf(getPage(url)) + 1);
		}
		return nextpage;
	}
	
	@SuppressWarnings("unchecked")
	private String getTopicId(Map<String,Object> map) {
		String topicId = null;
		if (map.containsKey("topic_id")) {
			topicId = map.get("topic_id").toString();
		} else if (map.containsKey("listData")
				&& ((Map<String,Object>) map.get("listData")).containsKey("topic_id")) {
			topicId = ((Map<String,Object>) map.get("listData")).get("topic_id").toString();
		}
		return topicId;
	}
	
	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("page_no=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	
	private String timeformat(long time) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(gc.getTime());
	}
}
