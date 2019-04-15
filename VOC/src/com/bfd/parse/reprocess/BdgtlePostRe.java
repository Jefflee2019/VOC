package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：数字尾巴(论坛)
 * 
 * 主要功能：
 * 		格式化作者的关注数，帖子数，评论数。
 * 		格式化回复的回复时间和回复楼层
 * 
 * @author bfd_03
 *
 */
public class BdgtlePostRe implements ReProcessor {

	private static final Pattern PATTERN_ID = Pattern.compile("\\d+");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		List<Map<String, Object>> taskList =null;
		if(resultData.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) resultData.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		Matcher idM = PATTERN_ID.matcher(unit.getUrl());
		String id =null;
		while(idM.find()){
			id = idM.group(0);
		}
		String link = "https://api.yii.dgtle.com/v2/comment?token=&tid=" + id + "&page=1";
		Map<String,Object> commentMap = new HashMap<>(4);
		commentMap.put(Constants.LINK, link);
		commentMap.put(Constants.RAWLINK, link);
		commentMap.put(Constants.LINKTYPE, Constants.COMMENT_URL);
		taskList.add(commentMap);
		resultData.put(Constants.COMMENT_URL, link);
		return new ReProcessResult(SUCCESS, processdata);
	}

	
}
