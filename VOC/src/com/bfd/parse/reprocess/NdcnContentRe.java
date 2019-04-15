package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点：当乐网
 * 功能：内容页后处理
 * @author dph 2017年11月6日
 *
 */
public class NdcnContentRe implements ReProcessor{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace face) {
		
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<String, Object>();
		//"view_cnt": "1592人浏览",
		if(resultData.containsKey(Constants.VIEW_CNT)){
			String viewCnt = (String) resultData.get(Constants.VIEW_CNT);
			viewCnt = viewCnt.replace("人浏览", "");
			resultData.put(Constants.VIEW_CNT,viewCnt);
		}
		Map<String, Object> task = new HashMap<String, Object>();
		String commentUrl =unit.getUrl() + "#comment";
		task.put("link", commentUrl);
		task.put("rawlink", commentUrl);
		task.put("linktype", "newscomment");
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		tasks.add(task);
		resultData.put(Constants.COMMENT_URL, commentUrl);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
