package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：21cn
 * <p>
 * 主要功能：处理评论信息
 * @author bfd_01
 *
 */
public class N21cnCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(N21cnCommentJson.class);
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
			LOG.info("url:" + data.getUrl() + ".json is " + json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				LOG.info("url:" + data.getUrl() + ".correct json is " + json);

				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
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
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, taskList);
		try {
			Map<String, Object> map = (Map<String, Object>) JsonUtils
					.parseObject(json);
			if (map.containsKey("list")) {
				List<Map<String,Object>> comm = new ArrayList<Map<String,Object>>();
				List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("list");
				for (int i = 0; i < list.size(); i++) {
					Map<String,Object> commmap = new HashMap<String,Object>();
					Map<String,Object> temp = (Map<String,Object>) list.get(i);
					commmap.put(Constants.DOWN_CNT, temp.get("againstNum"));
					commmap.put(Constants.COMMENT_TIME, temp.get("createTime"));
					commmap.put(Constants.UP_CNT, temp.get("supportNum"));
					// 评论分享数
					commmap.put(Constants.SHARE_CNT, temp.get("shareNum"));
					commmap.put(Constants.COMMENT_CONTENT,
							temp.get("reviewContent"));
					commmap.put(Constants.USERNAME, temp.get("userNickName"));
					commmap.put(Constants.COMMENTER_IP, temp.get("userIp"));
					comm.add(commmap);
				}
				parsedata.put(Constants.COMMENTS, comm);
			}
			if (map.containsKey("summary")) {
				Map<String,Object> temp = (Map<String,Object>)map.get("summary");
				int replyCnt = Integer.valueOf(temp.get("reviewNum").toString());
				parsedata.put(Constants.REPLY_CNT, replyCnt);
			}
			if (map.containsKey("pageTurn")) {
				int page = Integer.valueOf(((Map<String,Object>)map.get("pageTurn")).get("page").toString());
				int nextpage = Integer.valueOf(((Map<String,Object>)map.get("pageTurn")).get("nextPage").toString());
				if (page < nextpage) {
					String nextpageUrl = url.split("pageNo=")[0] + "pageNo=" + nextpage
							+ "&pageSize=10&sys=cms";
					Map<String, Object> commentTask = new HashMap<String, Object>();
					commentTask.put(Constants.LINK, nextpageUrl);
					commentTask.put(Constants.RAWLINK, nextpageUrl);
					commentTask.put(Constants.LINKTYPE, "newscomment");
					taskList.add(commentTask);
					parsedata.put(Constants.NEXTPAGE, commentTask);
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}
}
