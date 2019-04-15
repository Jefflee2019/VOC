package com.bfd.parse.json;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.MyDateUtil;
import com.bfd.parse.util.TextUtil;
import com.mysql.jdbc.StringUtils;

public abstract class AMJsonParser implements JsonParser {
	private static final Log LOG = LogFactory.getLog(AMJsonParser.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
//		LOG.info("url:"+unit.getUrl()+" begin execute AMJsonParser parse");
//		LOG.info("url:"+unit.getUrl()+".execute amjsonparser parse . taskdata is "+JsonUtil.toJSONString(taskdata));
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit.getPageEncode());
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
//				LOG.info("url:"+unit.getUrl()+".before execute parse.classname : "+this.getClass().getName());
				
				executeParse(parsedata, json, data.getUrl(), unit);
//				LOG.info("after execute parse");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = ParseResult.jsonprocess_FAILED;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
//		LOG.info("line ");
		JsonParserResult result = new JsonParserResult();
		try {
			reprocess(parsedata, unit);
//			LOG.info("line 64");
//			LOG.info("after execute reprocess");
			
			result.setData(parsedata);
			result.setParsecode(parsecode);
//			LOG.info("after execute JsonParserResult");
//			LOG.info("line 70");
//			LOG.debug("url:"+unit.getUrl()+".jsonparser rs is "+JsonUtil.toJSONString(parsedata));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
//		LOG.info("line 74");
		return result;
	}

	public void reprocess(Map<String, Object> parsedata,ParseUnit unit){
		
	}
	

	public abstract void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit);

	// 对于一般item任务增加buyerpageInfo
	public void createBuyerTaskForCommonItem(
			Map<String, Object> parsedata, ParseUnit unit,String monthSold) {
		LOG.info("url:"+unit.getUrl()+".begin execute createBuyerTaskForCommonItem");
		// 这次解析出的销量为空或者为0，不增加销量任务
		if (monthSold == null|| monthSold.equals("0")) {
			LOG.info("url:"+unit.getUrl()+".monthsold is null or is 0 not execute createBuyerTaskForCommonItem");
			return;
		}
		// 如果这次解析出的销量和taskdata里面的销量相等，并且当前世界与上次抓取时间小于2小时，不生成销量任务
		
		try {
			if (unit.getTaskdata().get(Constants.month_sold)!=null
					&&unit.getTaskdata().get(Constants.last_soldlist_time)!=null
					&&monthSold.equals(unit.getTaskdata().get(Constants.month_sold).toString())
					&& ((long)Double.parseDouble(unit.getTaskdata().get(Constants.last_soldlist_time).toString())-new Date().getTime()/1000)<2*60*60
					) {
				LOG.info("url:"+unit.getUrl()+".monthsold not change and interval in 2 hours not execute createBuyerTaskForCommonItem");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("error:"+unit.getTaskdata().get(Constants.last_soldlist_time));
		}

		boolean firstTimes = !unit.getTaskdata().containsKey(
				Constants.month_sold);
		Map<String, Object> buyer_page_info = new HashMap<String, Object>();
		if (firstTimes) {
			// buyer_page_info里的total_old为0，
			buyer_page_info.put(Constants.total_old, 0);
			
		} else {
			buyer_page_info.put(Constants.total_old,
					unit.getTaskdata().get(Constants.month_sold));

		}
		buyer_page_info.put(Constants.total_new,Integer.parseInt(monthSold));
		buyer_page_info.put(Constants.crawled_pages, 0);
		buyer_page_info.put(Constants.task_create_time, unit
				.getSpiderdata().get(Constants.dendtime));
		if(unit.getTaskdata().containsKey(Constants.last_soldlist_time)){
			buyer_page_info.put(Constants.last_crawl_time, unit.getTaskdata().get(Constants.last_soldlist_time));
		}else{
			buyer_page_info.put(Constants.last_crawl_time, "");
		}
		
		parsedata.put(Constants.buyer_page_info, buyer_page_info);
	}

	// 对于销量任务增加buyerpageInfo信息
	public void createBuyerTaskForBuyerItem(Map<String, Object> parsedata, ParseUnit unit) {
		
		LOG.info("url:"+unit.getUrl()+".begin execute createBuyerTaskForBuyerItem");
		// TODO从下载传过来的参数表示是否终止本轮抓取
		boolean isTerminalFromDownload = unit.getSpiderdata().containsKey(
				Constants.noNext);

		if(unit.getTaskdata().get(Constants.last_soldlist_time)==null
				||parsedata.get(Constants.oldest_sold_time)==null){
			return;
		}
		// 本次抓取最老的时间比上轮抓取的时间要老，就终止抓取
		if ((unit.getTaskdata().get(Constants.last_soldlist_time) != null 
				&& unit.getTaskdata().get(Constants.last_soldlist_time).toString()
				.compareTo(parsedata.get(Constants.oldest_sold_time).toString())>0)
				|| isTerminalFromDownload) {
			LOG.info("url:"+unit.getUrl()+".last_soldlist_time>oldest_sold_time or isTerminalFromDownload is true not execute createBuyerTaskForBuyerItem");
			return;
		}
		// 解析出总销量，得到下载时间，分别写到buyer_page_info里面的total_new， task_create_time
		// buyer_page_info里面的crawled_pages从taskdata里面得到；
		// last_crawl_time从taskdata里面 last_soldlist_time取得；
		// TODO:判断是否是以第一次抓这个item，
		Map<String, Object> buyer_page_info = new HashMap<String, Object>();

		// buyer_page_info里的total_old为taskdata里的month_sold，
		buyer_page_info = (Map<String, Object>) unit.getTaskdata().get(
				Constants.ajaxext);
//		buyer_page_info = new HashMap<String, Object>();
//		buyer_page_info.put(Constants.crawled_pages, 2);
//		buyer_page_info.put(Constants.last_crawl_time, "");
//		buyer_page_info.put(Constants.task_create_time, new Date().getTime()/1000);
//		buyer_page_info.put(Constants.total_old, 10);
//		buyer_page_info.put(Constants.total_new, 100);
		if(buyer_page_info==null){
			return;
		}
		buyer_page_info.put(Constants.crawled_pages,
				(Integer) buyer_page_info.get(Constants.crawled_pages)
						+ (Integer)parsedata.get(Constants.pageNumThisTimes));

//		buyer_page_info.put(Constants.last_crawl_time,
//				buyer_page_info.get(Constants.task_create_time));
//		buyer_page_info.put(Constants.task_create_time, unit.getSpiderdata()
//				.get(Constants.dendtime));
		parsedata.put(Constants.buyer_page_info, buyer_page_info);

	}

}
