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
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Neastmoney
 * 
 * 功能：分离评论
 * 
 * @author bfd_06
 * 
 */
public class Nali213CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Nali213CommentJson.class);

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
			String url = data.getUrl();
			if(url.contains("ali-comment-ajx")){
				int indexA = json.indexOf("[");
				int indexB = json.lastIndexOf("]");
				if (indexA >= 0 && indexB >= 0 && indexA < indexB)
					json = json.substring(indexA + 1, indexB);
			}
			executeParse(parsedata, json, url, unit);
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
			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			/**
			 * 第一类常用评论链接数据结构解析
			 */
			if(url.contains("ali-comment-ajx")){
				String htmllist = (String) result.get("htmllist");
				HtmlCleaner cleaner = new HtmlCleaner();
				TagNode root = cleaner.clean(htmllist);
				TagNode body = (TagNode) root.evaluateXPath("//body")[0];
				for (TagNode childTag : body.getChildTags()) {
					if (!childTag.getAttributeByName("class").equals(
							"ali-c-block-box"))
						continue;
					Map<String, Object> comment = new HashMap<String, Object>();
					TagNode utagNode = childTag.getChildTags()[0];
					TagNode uutagNode = utagNode.getChildTags()[0];
					TagNode nameSpan = uutagNode.getChildTags()[0];
					TagNode citySpan = uutagNode.getChildTags()[1];
					TagNode timeSpan = uutagNode.getChildTags()[2];
					TagNode contentDiv = utagNode.getChildTags()[1].getChildTags()[0];
					TagNode upCntEm = utagNode.getChildTags()[2].getChildTags()[0]
							.getChildTags()[0].getChildTags()[0].getChildTags()[1];
					TagNode downCntEm = utagNode.getChildTags()[2].getChildTags()[0]
							.getChildTags()[1].getChildTags()[0].getChildTags()[1];

					String username = nameSpan.getText().toString();
					String city = citySpan.getText().toString();
					String comment_time = timeSpan.getText().toString();
					String comment_content = contentDiv.getText().toString();
					String up_cnt = upCntEm.getText().toString();
					if (up_cnt.equals("") || up_cnt == null)
						up_cnt = "0";
					String down_cnt = downCntEm.getText().toString();
					if (down_cnt.equals("") || down_cnt == null)
						down_cnt = "0";

					comment.put(Constants.USERNAME, username);
					comment.put(Constants.CITY, city);
					comment.put(Constants.COMMENT_TIME, comment_time);
					comment.put(Constants.COMMENT_CONTENT, comment_content);
					comment.put(Constants.UP_CNT, up_cnt);
					comment.put(Constants.DOWN_CNT, down_cnt);
					comments.add(comment);
				}
				/**
				 * 搜狐畅言评论链接数据结构解析
				 */
			} else if(url.contains("changyan")){
				// 根据listHtml字段是否为空判断评论链接是否正确 否则进行下一种链接格式的尝试
				String listHtml = (String) result.get("listHtml");
				if(listHtml.equals("")){
					String reg = "(\\d+).html";
					Pattern ptn = Pattern.compile(reg);
					Matcher m = ptn.matcher(url);
					if(m.find()){
						Map<String, Object> commentTask = new HashMap<String, Object>();
						String commentUrl = "http://comment.ali213.net/ali-comment-ajx.php?callback=success_jsonpCallback&action=display&appid=1&conid=" + m.group(1);
						commentTask.put("link", commentUrl);
						commentTask.put("rawlink", commentUrl);
						commentTask.put("linktype", "newscomment");
						parsedata.put("redirect_comment_url", commentUrl);
						tasks.add(commentTask);
					}
				} else {
					// 链接正确
					Map<String, Object> listData = (Map<String, Object>) result.get("listData");
					List<Map<String, Object>> jsonComments = (List<Map<String, Object>>) listData
							.get("comments");
					for (Map<String, Object> jsonComment : jsonComments) {
						Map<String, Object> comment = new HashMap<String, Object>();
						String username = ((Map<String, String>) jsonComment
								.get("passport")).get("nickname");
//						String commenter_level = ((Map<String, String>) jsonComment
//								.get("userScore")).get("title");
//						String city = (String) jsonComment.get("ip_location");
						long create_time = (long) jsonComment.get("create_time");
						Date date = new Date(create_time);
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy年MM月dd日 HH:mm");
						String comment_time = format.format(date);
						String comment_content = (String) jsonComment.get("content");
						int up_cnt = (int) jsonComment.get("support_count");
						int down_cnt = (int) jsonComment.get("oppose_count");

						comment.put(Constants.USERNAME, username);
//						comment.put(Constants.COMMENTER_LEVEL, commenter_level);
//						comment.put(Constants.CITY, city);
						comment.put(Constants.COMMENT_TIME, comment_time);
						comment.put(Constants.COMMENT_CONTENT, comment_content);
						comment.put(Constants.UP_CNT, up_cnt);
						comment.put(Constants.DOWN_CNT, down_cnt);
						comments.add(comment);
					}
				}
			}
			parsedata.put("comments", comments);
//			System.out.println(parsedata.toString());
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
	}

	/*
	 * List<Map<String, Object>> reL = (List<Map<String, Object>>)
	 * result.get("re"); List<Map<String, Object>> comments = new
	 * ArrayList<Map<String, Object>>(); for(Map<String, Object> reM:reL){
	 * Map<String, Object> comment = new HashMap<String, Object>(); String
	 * userName = (String) reM.get("reply_ip"); if(userName.equals("")){
	 * Map<String, Object> reply_userM = (Map<String, Object>)
	 * reM.get("reply_user"); userName = (String)
	 * reply_userM.get("user_nickname"); } comment.put(Constants.USERNAME,
	 * userName); comment.put(Constants.COMMENT_TIME,
	 * reM.get("reply_publish_time")); comment.put(Constants.COMMENT_CONTENT,
	 * reM.get("reply_text")); comment.put(Constants.FAVOR_CNT,
	 * reM.get("reply_like_count")); comments.add(comment); }
	 * parsedata.put("comments", comments);
	 *//**
	 * 判断是否含有下一页 有则给出
	 */
	/*
	 * int pageNum = matchPageNum("thispage=(\\d+)", url); //当前页码 int count =
	 * (int)result.get("count"); //评论总数 if(pageNum*20<count){ String
	 * nextCommentUrl = url.replace("thispage="+pageNum,
	 * "thispage="+(pageNum+1)); Map<String, Object> task = new HashMap<String,
	 * Object>(); task.put("link", nextCommentUrl); task.put("rawlink",
	 * nextCommentUrl); task.put("linktype", "newscomment"); tasks.add(task);
	 * parsedata.put(Constants.NEXTPAGE, task); }
	 * 
	 * System.out.println(parsedata.toString());
	 */

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 1;
	}

}
