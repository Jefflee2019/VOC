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

import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：优酷
 * 
 * 从JSON中解析评论内容
 * 
 * @author bfd_06
 */
public class NyoukuCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NyoukuCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
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
				LOG.error("JsonParse reprocess exception, taskdat url="
						+ taskdata.get("url") + ".jsonUrl:" + data.getUrl(), e);
			}
		}

		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			LOG.error("JsonParse reprocess error, taskdat url=" + taskdata.get("url"), e);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		// 开头加上tasks
		List<Map<String, Object>> rtasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, rtasks);
		try {
			Map<Object, Object> jsonMap = (Map<Object, Object>) JsonUtil
					.parseObject(json);
			
			if (jsonMap.containsKey("data")) {
				Map data = (Map)jsonMap.get("data");
				int currentPage = 0;
				int totalPage = 0;
				if (data.containsKey("currentPage")) {					
					currentPage = Integer.valueOf(data.get("currentPage").toString());
				}
				
				if (data.containsKey("totalPage")) {					
					totalPage = Integer.valueOf(data.get("totalPage").toString());
				}
				
				List<Map<String,Object>> comments = new ArrayList<Map<String,Object>>();
				if (data.containsKey("comment")) {
					List<Map<String, Object>> list = (List<Map<String, Object>>) data
							.get("comment");
					for (int i=0;i<list.size();i++) {
						Map<String,Object> comm = new HashMap<String,Object>();
						Map comment = list.get(i);
						comm.put(Constants.COMMENT_CONTENT, comment.get("content").toString());
						comm.put(Constants.COMMENT_TIME, normalTime(comment.get("createTime").toString()));
						comm.put(Constants.UP_CNT, comment.get("upCount").toString());
						comm.put(Constants.DOWN_CNT, comment.get("downCount").toString());
						String username = ((Map)comment.get("user")).get("userName").toString();
						comm.put(Constants.COMMENTER_NAME, username);
						comments.add(comm);
					}
				}
				parsedata.put(Constants.COMMENTS, comments);
				
//				http://p.comments.youku.com/ycp/comment/pc/commentList?jsoncallback=n_commentList&objectId=384903625
//				&app=100-DDwODVkv&currentPage=1&pageSize=30&listType=0&sign=8ac5549d7542e61cbdc7967c62705846&time=1479884315678
				if (currentPage < totalPage) {
					String nextpageUrl = url.split("currentPage=")[0] + "currentPage="
							+ (currentPage+1) + "&pageSize=30" + url.split("&pageSize=30")[1];
					Map<String, Object> commentTask = new HashMap<String, Object>();
					commentTask.put(Constants.LINK, nextpageUrl);
					commentTask.put(Constants.RAWLINK, nextpageUrl);
					commentTask.put(Constants.LINKTYPE, "newscomment");
					rtasks.add(commentTask);
					parsedata.put(Constants.NEXTPAGE, commentTask);
				}
			}
			
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
	}

	public int match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		matcher.find();
		return Integer.parseInt(matcher.group(1));
	}
		
	/**
	 * @function:将时间戳转换为标准时间格式，单位是秒
	 * @param time
	 * @return
	 */
	public static String normalTime(String time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        return sdf.format(new Date(Long.valueOf(time)));
	}
	
}