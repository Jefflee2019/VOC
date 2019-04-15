package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Nsinablog
 * 
 * 获取 关注数、积分获赠金笔 字段
 * 
 * @author bfd_06
 * 
 */
public class NsinablogContentJson implements JsonParser {
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
//			int indexA = json.indexOf("(");
//			int indexB = json.lastIndexOf(")");
//			if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
//				json = json.substring(indexA + 1, indexB);
//			}
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings({"unchecked"})
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		if(url.contains("comment")){
			int replyCnt = matchReplyCnt("\"comment_num\":\"(\\d+)\"",json);
			parsedata.put("reply_cnt", replyCnt);
		} else {
			try {
				json = json.replace("data", "\"data\"");
				Map<String, Object> result = (Map<String, Object>) JsonUtil
						.parseObject(json);
				Map<String, Object> data = (Map<String, Object>) result.get("data");
				Matcher matcher = matchUserPointUid("userpointuid=(\\d+)",url);
				if (matcher.find()) {
					String userPointUid = matcher.group(1);
					Map<String, Object> num = (Map<String, Object>) data.get("num");
					// 关注数
					parsedata.put("concern_cnt", num.get(userPointUid));
				}
				// 积分
				if(data.containsKey("userpoint")){
					parsedata.put("experience_cnt", data.get("userpoint"));
				}
				// 获赠金笔
				if(data.containsKey("goldpen")){
					parsedata.put("goldPen_cnt", data.get("goldpen"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
//		System.out.println(parsedata.toString());
	}
	
	public Matcher matchUserPointUid(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);

		return matcher;
	}
	public int matchReplyCnt(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if(matcher.find()){
			return Integer.parseInt(matcher.group(1));
		}
		
		return 0;
	}
	
}
