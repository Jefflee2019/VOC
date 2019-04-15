package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * @site：中关村-新闻(Nzol)
 * @function：新闻内容页 获取内容点赞数(因为必须要带cookie才能正常下载，所以改在后处理插件处理，不再走JsEngine)
 * 
 * @author bfd_02
 */
public class NzolContentJson implements JsonParser {
	private final static Log LOG = LogFactory.getLog(NzolContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
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
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		//因为必须要带cookie才能正常下载，所以改在后处理插件处理，不再走JsEngine
		if (obj instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) obj;
			 //内容点赞数 support_cnt
			if (data.containsKey("like_hits")) {
				String supportCnt = data.get("like_hits").toString();
				parsedata.put(Constants.SUPPORT_CNT, Integer.parseInt(supportCnt));
			}
			
			//点踩数 oppose_cnt
			if (data.containsKey("dislike_hits")) {
				String opposeCnt = data.get("dislike_hits").toString();
				parsedata.put(Constants.OPPOSE_CNT, Integer.parseInt(opposeCnt));
			}

		}
	}
}