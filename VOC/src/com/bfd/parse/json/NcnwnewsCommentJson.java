package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * 站点名：中网资讯
 * 
 * 功能：分离评论
 * 
 * @author bfd_06
 * 
 */
public class NcnwnewsCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NcnwnewsCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			int indexA = json.indexOf("(");
			int indexB = json.lastIndexOf(")");
			if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
				json = json.substring(indexA + 1, indexB);
			}
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil
					.parseObject(json);
			Map<String, Object> listData = null;
			if (!unit.getUrl().contains("comments")) {
				listData = (Map<String, Object>) result.get("listData"); // 第一页评论数据结构
			} else {
				listData = result; // 第二页评论数据结构
			}
			List<Map<String, Object>> comments = (List<Map<String, Object>>) listData
					.get("comments");
			List<Map<String, Object>> newComments = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> comment : comments) {
				Map<String, Object> newComment = new HashMap<String, Object>();
				String username = ((Map<String, String>) comment
						.get("passport")).get("nickname");
				String commenter_level = ((Map<String, String>) comment
						.get("userScore")).get("title");
				String city = (String) comment.get("ip_location");
				long create_time = (long) comment.get("create_time");
				Date date = new Date(create_time);
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日 HH:mm");
				String comment_time = format.format(date);
				String comment_content = (String) comment.get("content");
				int up_cnt = (int) comment.get("support_count");
				int down_cnt = (int) comment.get("oppose_count");

				newComment.put(Constants.USERNAME, username);
				newComment.put(Constants.COMMENTER_LEVEL, commenter_level);
				newComment.put(Constants.CITY, city);
				newComment.put(Constants.COMMENT_TIME, comment_time);
				newComment.put(Constants.COMMENT_CONTENT, comment_content);
				newComment.put(Constants.UP_CNT, up_cnt);
				newComment.put(Constants.DOWN_CNT, down_cnt);
				newComments.add(newComment);
			}
			parsedata.put("comments", newComments);

			/**
			 * 判断是否含有下一页 有则给出
			 */
			int cmt_sum = (int) listData.get("cmt_sum"); // 总评论数
			int pageNum = matchInt("page_no=(\\d+)", unit.getUrl()); // 当前页码
			if (cmt_sum > pageNum * 15) {
				int topicId = (int) listData.get("topic_id");
				String nextCommentUrl = "http://changyan.sohu.com/api/2/topic/comments?callback=fn&client_id=cyqQwySAs&topic_id="
						+ topicId
						+ "&page_size=15&page_no="
						+ (pageNum + 1)
						+ "&style=floor&inside_floor=3&outside_floor=2";
				Map<String, Object> task = new HashMap<String, Object>();
				// task.put("iid", DataUtil.calcMD5(nextCommentUrl));
				task.put("link", nextCommentUrl);
				task.put("rawlink", nextCommentUrl);
				task.put("linktype", "newscomment");
				tasks.add(task);
				parsedata.put(Constants.NEXTPAGE, task);
			}

			// System.out.println(parsedata.toString());
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
	}

	public int matchInt(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 1;
	}

	public Matcher matchTopicId(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);

		return matcher;
	}

}
