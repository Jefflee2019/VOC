package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：Eyhd_hw
 * 功能：后处理获取处理评论
 * @author dph 2018年3月14日
 *
 */
public class Egome_sListRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		
		String htmlcontent = gethtml(unit.getUrl());
		Map<String, Object> parsedata = result.getParsedata().getData();
		List items = new ArrayList();
		List tasks = new ArrayList();
		JSONObject json = JSONObject.parseObject(htmlcontent);  
		Map content = (Map) json.get("content");
		Map pageBar = (Map) content.get("pageBar");
		Map prodInfo = (Map) content.get("prodInfo");
		List products = (List) prodInfo.get("products");
		for (Object object : products) {
			Map<String, Object> reMap = new HashMap<String, Object>();
			Map<String, Object> tempTask = new HashMap<String, Object>();
			Map<String, Object> item = new HashMap<String, Object>();
			Map obj = (Map) object;
			String link = (String) obj.get("sUrl");
			if (!link.contains("http")) {
				link = "http:" + link;
			}
			String itemname = (String) obj.get("alt");
			item.put("link", link);
			item.put("rawlink", link);
			item.put("linktype", "eccontent");
			reMap.put(Constants.ITEMLINK, item);
			reMap.put(Constants.ITEMNAME, itemname);
			tempTask.put("iid", DataUtil.calcMD5(link));
			tempTask.put("link", link);
			tempTask.put("rawlink", link);
			tempTask.put("linktype", "eccontent");
			tasks.add(tempTask);
			items.add(reMap);
		}
//		下一页
		Integer pageNumber = (Integer) pageBar.get("pageNumber");
		Integer totalPage = (Integer) pageBar.get("totalPage");
		if (pageNumber < totalPage) {
			String nextpage = unit.getUrl().replaceAll("page=" + pageNumber, "page=" + (pageNumber + 1));
			parsedata.put(Constants.NEXTPAGE, nextpage);
			Map<String,Object> nextpageMap = new HashMap<String,Object>();
			nextpageMap.put(Constants.LINK, nextpage);
			nextpageMap.put(Constants.RAWLINK, nextpage);
			nextpageMap.put(Constants.LINKTYPE, "eclist");
			nextpageMap.put("iid", DataUtil.calcMD5(nextpage.toString()));
			tasks.add(nextpageMap);
		}
		parsedata.put("items", items);
		parsedata.put("tasks", tasks);
		return new ReProcessResult(SUCCESS, parsedata);
	}
	
	public static String gethtml (String url) {
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
//		https://search.gome.com.cn/search?question=%E5%8D%8E%E4%B8%BA&searchType=goods&&page=1&type=json&aCnt=0
		String regex = "question=(.+)&searchType";
		Matcher m = Pattern.compile(regex).matcher(url);
		if(m.find()){
			String productId = m.group(1);
			request.setHeader("Referer", "https://search.gome.com.cn/search?question="+ productId +"&searchType=goods");
		}
		try {
			HttpResponse response = client.execute(request);
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	

}
