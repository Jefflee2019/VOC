package com.bfd.parse.json;

import java.util.ArrayList;
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
 *	@site：网易手机/数码
 *  @function：处理商品评论
 * @author bfd_04
 *
 */
public class Emobile163CommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(Emobile163CommentJson.class);
	private static final Pattern PAGEPATTERN_COMMENT = Pattern.compile("pagenum=(\\d+)");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
//			String url = unit.getUrl();
			try {
				executeCommentParse(parsedata, json, data.getUrl(), unit);      //商品评论
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}
	
	/**
	 * 处理商品评论
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeCommentParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
		parsedata.put("tasks", taskList);
		try {
			Map<String,Object> jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			List<Map<String, Object>> remarkList =(List<Map<String, Object>>)jsonMap
					.get("clist");
			if(remarkList !=null && !remarkList.isEmpty())
			{
				List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
				Matcher match = PAGEPATTERN_COMMENT.matcher(url);
				for(Map<String,Object> commItem : remarkList){
					Map<String, Object> reMap = new HashMap<String, Object>();
					reMap.put(Constants.COMMENTER_NAME, commItem.get("nickname"));
					reMap.put(Constants.COMMENT_TIME, ConstantFunc.transferLongToDate(
							"yyyy-MM-dd HH:mm:ss",Long.parseLong(commItem.
									get("createtime").toString())));
					reMap.put(Constants.SCORE, commItem.get("grade"));
					reMap.put(Constants.COMMENT_TITLE, commItem.get("title"));
					reMap.put(Constants.COMMENT_CONTENT, commItem.get("comment"));
					itemList.add(reMap);
				}
				parsedata.put("comments",itemList);       //parseResult body
				parsedata.put("msg_type", "comment");
				if(remarkList.size() >= 5 && match.find())
				{
					int page = Integer.parseInt(match.group(1)) + 1;
					String nextPage = url.replaceAll("pagenum" + match.group(1), "pagenum" + page);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextPage);
					nextpageTask.put("rawlink", nextPage);
					nextpageTask.put("linktype", "eccomment");
					taskList.add(nextpageTask);
					parsedata.put("nextpage", nextpageTask);
					parsedata.put("tasks", taskList);
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("executeParse error "+url);
		}
	}
}
