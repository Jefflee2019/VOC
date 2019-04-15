package com.bfd.parse.json;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点：天河购
 * 功能：列表页json
 * @author dph 2017年11月8日
 *
 */
public class EthmallListJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(EthmallListJson.class);
	private static final Pattern PATTERN_ITEMS = Pattern.compile("<a href=\"goods-\\d+-\\d+.html\" target=\"_blank\" class=\"hei3\">(.|\n)*</a>");
	private static final Pattern PATTERN_LINK = Pattern.compile("goods-\\d+-\\d+.html");
	private static final Pattern PATTERN_ITEMNAME = Pattern.compile(">(.|\n)*<");
	private static final Pattern PATTERN_CURRENTPAGE = Pattern.compile("<span class=\"currentpage\">\\d+</span>");
	private static final Pattern PATTERN_PAGE = Pattern.compile("\\d+");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json);
			}catch(Exception e){
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	/**
	 * 从json中提取信息
	 * @param parsedata
	 * @param json
	 */
	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json) {
		
		String htmlData = null;
		String currentpageStr = null;
		String pageStr = null;
		Matcher pageM = null;
		try {
			htmlData = new String(json.getBytes("gbk"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Matcher matcher = PATTERN_ITEMS.matcher(htmlData);
		String str = null;
		String link = null;
		String rawlink = null;
		String itemname = null;
		int count = 0;
		List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		List<Map<String, Object>> itemList =null;
		if(parsedata.get(Constants.ITEMS) != null){
			itemList = (List<Map<String,Object>>) parsedata.get(Constants.ITEMS);					
		}else{
			itemList = new ArrayList<Map<String,Object>>();
		}
		while(matcher.find()){
			count++;
			Map<String,Object> task = new HashMap<String,Object>(4);
			Map<String,Object> item =new HashMap<String,Object>(4);
			str = matcher.group(0);
			Matcher matcherLink = PATTERN_LINK.matcher(str);
			while(matcherLink.find()){
				link = "http://www.thmall.com/" + matcherLink.group(0);
				rawlink = "//www.thmall.com/" + matcherLink.group(0);
			}
			Matcher matcherItemname = PATTERN_ITEMNAME.matcher(str);
			while(matcherItemname.find()){
				itemname = matcherItemname.group(0);
				itemname = itemname.replace(">", "").replace("<", "").trim();
			}
			task.put(Constants.LINK, link);
			task.put(Constants.RAWLINK, rawlink);
			task.put(Constants.LINKTYPE, "eccontent");		
			taskList.add(task);
			parsedata.put(Constants.TASKS, taskList);
			
			Map<String,String> itemLink = new HashMap<String,String>(4);
			itemLink.put(Constants.LINK, link);
			itemLink.put(Constants.RAWLINK, rawlink);
			itemLink.put(Constants.LINKTYPE, "eccontent");		
			item.put(Constants.ITEMLINK, itemLink);
			item.put(Constants.ITEMNAME, itemname);
			itemList.add(item);
			parsedata.put(Constants.ITEMS, itemList);
			
		}
		//拼接下一页链接
		//当count为0时说明上一页为最后一页但商品数恰好为40
		Map<String, Object> nextpage= new HashMap<String,Object>(4);
		if(0 != count){
			Matcher currentpageM = PATTERN_CURRENTPAGE.matcher(htmlData);
			while(currentpageM.find()){
				currentpageStr = currentpageM.group(0);
				pageM = PATTERN_PAGE.matcher(currentpageStr);
			}
			while(pageM.find()){
				pageStr = pageM.group(0);
			}
			int page = 0;
			//endpageStr为空时证明列表页到最后一页 此时循环从第一页开始
			page = Integer.parseInt(pageStr);
			page = page + 1;
			//每一页的商品数为40 当小于时证明为最后一页
			if(count < 40){
				page = 1;
			}
			link = "http://www.thmall.com/index.php?act=goods_class&keyword=%E5%8D%8E%E4%B8%BA&curpage=" + page;
			rawlink = "//www.thmall.com/index.php?act=goods_class&keyword=%E5%8D%8E%E4%B8%BA&curpage=" + page;
			nextpage.put(Constants.LINK, link);
			nextpage.put(Constants.RAWLINK, rawlink);
			nextpage.put(Constants.LINKTYPE, "eclist");
			taskList.add(nextpage);
			parsedata.put(Constants.NEXTPAGE, nextpage);
			parsedata.put(Constants.TASKS, taskList);
		}else{
			link = "http://www.thmall.com/index.php?act=goods_class&keyword=%E5%8D%8E%E4%B8%BA&curpage=1";
			rawlink = "//www.thmall.com/index.php?act=goods_class&keyword=%E5%8D%8E%E4%B8%BA&curpage=1";
			nextpage.put(Constants.LINK, link);
			nextpage.put(Constants.RAWLINK, rawlink);
			nextpage.put(Constants.LINKTYPE, "eclist");
			taskList.add(nextpage);
			parsedata.put(Constants.NEXTPAGE, nextpage);
			parsedata.put(Constants.TASKS, taskList);
		}
		
	}
}
