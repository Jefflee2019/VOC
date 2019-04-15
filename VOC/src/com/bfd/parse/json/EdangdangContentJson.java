package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：当当网
 * 主要功能： 
 * 		获取价格
 *   
 * @author bfd_03
 *
 */
public class EdangdangContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EdangdangContentJson.class);

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
				json = new String(data.getData(), "GBK");
				
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

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		// 获取价格
		if (obj instanceof Map) {
			Map<String,Object> map = (Map<String,Object>) obj;
			if (map.containsKey("data")) {
				String isCatalog = this.getCresult(url, "isCatalog=(\\d+)");
				Map data = (Map) map.get("data"); 
				Map spu = (Map) data.get("spu");
				if ("0".equals(isCatalog)) {
					Map promotion = (Map) spu.get("promotion");
					Map p102 = (Map) promotion.get("p102");
					Map price = (Map) p102.get("price");
					String directPrice = (String) price.get("directPrice");
					parsedata.put(Constants.PRICE, directPrice);
				} else {
					Map price = (Map) spu.get("price");
					String salePrice = (String) price.get("salePrice");
					parsedata.put(Constants.PRICE, salePrice);
				}
			}
		} 
		//添加评论页链接
		String templateUrl = "http://product.dangdang.com/index.php?r=comment/list&productId=product_id&mainProductId=product_id&pageIndex=1&sortType=1&filterType=1&isSystem=1&tagId=0&tagFilterCount=0&template=mall&long_or_short=short";
//		http://product.dangdang.com/index.php?r=callback%2Fproduct-info&productId=1411554335&isCatalog=0&shopId=18215&productType=0
		String id = this.getCresult(url, "productId=(\\d+)");
		String sCommUrl = templateUrl.replaceAll("product_id", id);
		Map commentTask = new HashMap();
		commentTask.put(Constants.LINK, sCommUrl);
		commentTask.put(Constants.RAWLINK, sCommUrl);
		commentTask.put(Constants.LINKTYPE, "eccomment");
		commentTask.put("iid", DataUtil.calcMD5(sCommUrl + ""));
		parsedata.put(Constants.COMMENT_URL, sCommUrl);
		tasks.add(commentTask);	
		parsedata.put("tasks", tasks);
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}
}
