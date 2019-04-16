package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：E9ji
 * 
 * 动态解析列表页
 * 
 * @author bfd_06
 * 
 */
public class E9jiCommentJson implements JsonParser {

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
			// int indexA = json.indexOf("(");
			// int indexB = json.lastIndexOf(")");
			// if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
			// json = json.substring(indexA + 1, indexB);
			// }
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
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil
					.parseObject(json);
			if (!result.containsKey("review"))
				return;
			List<Map<String, Object>> comments = (List<Map<String, Object>>) result
					.get("review");
			List<Map<String, Object>> rcomments = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> comment : comments) {
				Map<String, Object> rcomment = new HashMap<String, Object>();
				String comment_tag = "";
				List<Map<String, String>> reviewTag = (List<Map<String, String>>) comment
						.get("reviewTag");
				for (int i = 0; i < reviewTag.size(); i++) {
					if (i == reviewTag.size() - 1)
						comment_tag += reviewTag.get(i).get("value_");
					else
						comment_tag = reviewTag.get(i).get("value_") + " ";
				}
				List<Map<String, String>> reviewImg = (List<Map<String, String>>) comment
						.get("reviewImg");
				List<String> comment_img_List = new ArrayList<String>();
				for (Map<String, String> reviewImgMap : reviewImg) {
					comment_img_List
							.add("http://img2.ch999img.com/pic/pingjiaimage/"
									+ reviewImgMap.get("ImageName"));
				}
				String comment_content = (String) comment.get("content_");
				String comment_time = (String) comment.get("time_");

				rcomment.put(Constants.COMMENT_TAG, comment_tag);
				rcomment.put(Constants.COMMENT_IMG, comment_img_List);
				rcomment.put(Constants.COMMENT_CONTENT, comment_content);
				rcomment.put(Constants.COMMENT_TIME, comment_time);

				rcomments.add(rcomment);
			}
			parsedata.put(Constants.COMMENTS, rcomments);
			// 添加下一页
			if (comments.size() >= 8)
				addCommentNextUrl(parsedata, unit);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int matchCstart(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	private void addCommentNextUrl(Map<String, Object> parsedata, ParseUnit unit) {
		Matcher matcher = Pattern.compile("pageIndex=(\\d+)").matcher(
				unit.getUrl());
		if (matcher.find()) {
			int pageNumNow = Integer.parseInt(matcher.group(1));
			String nextUrl = unit.getUrl().replace("pageIndex=" + pageNumNow,
					"pageIndex=" + (pageNumNow + 1));
			List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
			Map<String, Object> task = new HashMap<String, Object>();
			task.put(Constants.LINK, nextUrl);
			task.put(Constants.RAWLINK, nextUrl);
			task.put(Constants.LINKTYPE, "eccomment");
			tasks.add(task);
			parsedata.put(Constants.TASKS, tasks);
			parsedata.put(Constants.NEXTPAGE, task);
		}
	}

}
