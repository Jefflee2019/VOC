package com.bfd.parse.json;

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
 * 站点名：和讯网
 * 主要功能：获取点击数，等级，积分
 * @author bfd_03
 *
 */
public class BhexunPostJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(BhexunPostJson.class);
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		
		// 遍历dataList
		for(Object obj:dataList){
			JsonData data = (JsonData)obj;
			// 判断该ajax数据是否下载成功
			if(!data.downloadSuccess()){
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				// 将ajax数据转化为json数据格式
				/*
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				*/
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,data.getUrl(),unit);
			}catch(Exception e){
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
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
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String,String>> authorList = new ArrayList<Map<String,String>>();
		Map<String,String> author = new HashMap<String,String>();
		if(parsedata.containsKey(Constants.AUTHOR)){
			authorList = (List<Map<String,String>>) parsedata.get(Constants.AUTHOR);
		}
		if(!authorList.isEmpty()){
			author = authorList.get(0);
		}else{
			authorList.add(author);		
			parsedata.put(Constants.AUTHOR, authorList);
		}
		
		Pattern pattern = Pattern.compile("updatePostPv\\((\\d+)\\)");
		Matcher matcher = pattern.matcher(json);
		if(matcher.find()){
			//点击数
			parsedata.put(Constants.VIEW_CNT, matcher.group(1));
		}
		
		pattern = Pattern.compile("\\(\"dvGradeName\"\\).innerHTML\\s*=\\s*\"(\\S+)\";", Pattern.DOTALL);
		matcher = pattern.matcher(json);
		if(matcher.find()){
			//等级
			author.put(Constants.AUTHOR_LEVEL, matcher.group(1));
		}
		
		pattern = Pattern.compile("\\(\"dvuserpoint\"\\).innerHTML\\s*=\\s*'(\\S+)';", Pattern.DOTALL);
		matcher = pattern.matcher(json);
		if(matcher.find()){
			//积分
			author.put(Constants.FORUM_SCORE, matcher.group(1));
		}
		
	}
	
}
