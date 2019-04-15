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

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * @site 界面网
 * @author BFD_01
 *
 */
public class NjiemianCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NjiemianCommentJson.class);
	private static final Pattern AUTHOR_NAME = Pattern.compile("class=\\\"author-name\\\">(\\S+\\s?)<\\/a>");
	private static final Pattern COMMENT_MAIN = Pattern.compile("class=\\\"comment-main\\\"><p>(\\S+\\s?)<\\/p>");
	private static final Pattern DATE = Pattern.compile("class=\\\"date\\\">(\\d+\\/\\d+\\s\\d+:\\d+)<\\/span>");
	private static final Pattern URL = Pattern.compile("page=(\\d+)");
	private static final int PAGESIZE = 10;
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
//			LOG.info("url:"+data.getUrl()+".json is "+json);
			// json = TextUtil.removeAllHtmlTags(json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				} 
//				LOG.info("url:"+data.getUrl()+".correct json is "+json);
				
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
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
			LOG.info("url:"+taskdata.get("url")+"after jsonparser parsedata is "+parsedata);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata,
 String json,
			String url, ParseUnit unit) {
		try {
			Map<String, Object> originalMap = (Map<String, Object>) JsonUtils
					.parseObject(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);
			List<Map<String, Object>> commentList = new ArrayList<Map<String, Object>>();

			List<String> authorlist = new ArrayList<String>();
			List<String> commentlist = new ArrayList<String>();
			List<String> datelist = new ArrayList<String>();

			if (originalMap != null && !originalMap.isEmpty()) {
				if (originalMap.containsKey("rs")) {
					String rs = originalMap.get("rs").toString();
					Matcher mauthor = AUTHOR_NAME.matcher(rs);
					Matcher mcomment = COMMENT_MAIN.matcher(rs);
					Matcher mdate = DATE.matcher(rs);

					while (mauthor.find()) {
						authorlist.add(mauthor.group(1));
					}
					while (mcomment.find()) {
						commentlist.add(mcomment.group(1));
					}
					while (mdate.find()) {
						datelist.add(mdate.group(1));
					}

					if (authorlist.size() == commentlist.size()
							&& authorlist.size() == datelist.size()) {
						for (int i = 0; i < authorlist.size(); i++) {
							Map<String, Object> commentMap = new HashMap<String, Object>();
							commentMap.put(Constants.COMMENTER_NAME,
									authorlist.get(i));
							commentMap.put(Constants.COMMENT_CONTENT,
									commentlist.get(i));
							commentMap.put(Constants.COMMENT_TIME,
									formatDate(datelist.get(i)));
							commentList.add(commentMap);
						}

					}

				}

				int replyCnt = 0;
				int pageNo = 0;
				if (originalMap.containsKey("count")) {
					replyCnt = Integer.valueOf(originalMap.get("count")
							.toString());
				}
				if (!url.contains("page=")) {
					pageNo = 1;
				} else {
					Matcher murl = URL.matcher(url);
					while (murl.find()) {
						pageNo = Integer.valueOf(murl.group(1));
					}
				}
				String nextpage = null;

				if (replyCnt > PAGESIZE * pageNo && pageNo == 1) {
					nextpage = url + "&page=2";
				} else if (replyCnt > PAGESIZE * pageNo && pageNo > 1) {
					nextpage = url.split("page")[0] + "page=" + (pageNo + 1);
				}
				parsedata.put("comments", commentList);
				// 获取下一页
				if (nextpage != null) {
					Map<String, Object> nextPageTask = new HashMap<String, Object>();
					nextPageTask.put("link", nextpage);
					nextPageTask.put("rawlink", nextpage);
					nextPageTask.put("linktype", "newscomment");
					taskList.add(nextPageTask);
					// 下一页在json中放入的是个map
					parsedata.put("nextpage", nextPageTask);
				}
			}

		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
	}
	
	public String formatDate(String time) {
		Pattern pattern = Pattern
				.compile("(\\d+?)/(\\d+?) (\\d+):(\\d+)");
		Matcher matcher = pattern.matcher(time);
		if (matcher.find()) {
			if (time.contains("201")) {
			} else {
				Date date = new Date();
				SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
				String times = sFormat.format(date);
				String[] mm = times.split("-");
				if (Integer.parseInt(mm[1]) < Integer
						.parseInt(matcher.group(1))) {
					int year = Integer.parseInt(mm[0]) - 1;
					time = year + "-" + time;
				} else if (Integer.parseInt(mm[1]) == Integer.parseInt(matcher
						.group(1))) {
					if (Integer.parseInt(mm[2]) >= Integer.parseInt(matcher
							.group(2))) {
						int year = Integer.parseInt(mm[0]);
						time = year + "-" + time;
					} else {
						int year = Integer.parseInt(mm[0]) - 1;
						time = year + "-" + time;
					}
				} else {
					int year = Integer.parseInt(mm[0]);
					time = year + "-" + time;
				}
			}
		} else {
			time = ConstantFunc.convertTime(time);
			time = time.trim();
		}
		return ConstantFunc.convertTime(time.replace("/", "-"));
	}
}
