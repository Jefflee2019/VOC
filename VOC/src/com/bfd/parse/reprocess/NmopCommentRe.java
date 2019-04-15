package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nmop
 * 
 * 功能：获取评论
 * 
 * @author bfd_06
 */
public class NmopCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		String json = unit.getPageData();

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> replysList = null;
		if(null != resultData.get(Constants.REPLYS)){
			replysList = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
		}else{
			replysList = new ArrayList<Map<String,Object>>();
		}
		Map<String, Object> jsonMap = null;
		try {
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
			if(jsonMap.containsKey("data") && null != jsonMap.get("data")){
				List<Map<String,Object>> dataList = (List<Map<String,Object>>) jsonMap.get("data");
				for(Map<String,Object> data : dataList){
					Map<String,Object> map = new HashMap<String,Object>();
					String replytime = (String) data.get("replytime");
					Long time = Long.parseLong(replytime);
					String date = ConstantFunc.transferLongToDate("yyyy-MM-dd HH:mm:ss",time);
					map.put(Constants.REPLYDATE, date);
					map.put(Constants.REPLYCONTENT, data.get("body"));
					map.put(Constants.REPLYFLOOR, data.get("floor"));
					replysList.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resultData.put("comments", replysList);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
		
	}

}
