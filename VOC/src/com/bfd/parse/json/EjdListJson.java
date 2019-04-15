package com.bfd.parse.json;

import java.util.ArrayList;
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
 * 站点名：京东 
 * 主要功能： 获取热销商品
 * @author bfd_03
 *
 */
public class EjdListJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EjdListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

		// 遍历dataList
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn(
						"AMJsonParse exception,taskdat url="
								+ taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.warn("json parse error or json is null");
		}

		if(obj instanceof Map){
			Map map = (Map)obj;
			//热销商品（推广商品）
			//搜索关键词，Map中的key为:291，292
			//按价格排序，Map中的Key为：47
			List hotSaleList = new ArrayList();
			if (map.containsKey("47")) {
				hotSaleList = (ArrayList) map.get("47");
			} else if (map.containsKey("291")) {
				hotSaleList = (ArrayList) map.get("291");
			}
			if(!hotSaleList.isEmpty()){
				List hotSaleDataList = new ArrayList();
				for(int i=0; i<hotSaleList.size(); i++){
					Map hotSaleMap = (HashMap)hotSaleList.get(i);
					Map temp = new HashMap();
					String itemname = hotSaleMap.get("ad_title").toString();
					itemname = itemname.replace("<font class=\"skcolor_ljg\">", "").replace("</font>", "");
					
					temp.put(Constants.ITEMLINK, hotSaleMap.get("click_url").toString());
					temp.put(Constants.ITEMNAME, itemname);
					temp.put(Constants.REPLY_CNT, hotSaleMap.get("comment_num").toString());
					
					hotSaleDataList.add(temp);
				}
				parsedata.put(Constants.HOT_SALE, hotSaleDataList);
			}
			
		}
		

	}

}
