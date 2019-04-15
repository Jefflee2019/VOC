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
import com.bfd.parse.util.ParseUtils;

public class EzolContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit,
			ParseResult result, ParserFace face) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.REPLY_CNT)){
			Pattern pNum = Pattern.compile("\\d+");
			Matcher m = pNum.matcher(resultData.get(Constants.REPLY_CNT).toString());
			if(m.find()){
				int replycount = Integer.valueOf(m.group());
				resultData.put(Constants.REPLY_CNT, replycount);
				if(replycount > 0){
//					String url = ((Map<String, Object>)resultData.get("comment_url")).get("link").toString();
					String url = unit.getUrl();
//					http://detail.zol.com.cn/cell_phone/index302442.shtml
					Pattern p = Pattern.compile("index(\\d+).shtml");
					Matcher mch = p.matcher(url);
					if(mch.find()){
						Map<String, Object> commMap = new HashMap<>();
						String commUrl = "http://detail.zol.com.cn/xhr4_Review_GetList_&proId=%s&page=1.html";
						commUrl = String.format(commUrl, mch.group(1));
						commMap.put("link", commUrl);
						commMap.put("rawlink", commUrl);
						commMap.put("linktype", "eccomment");
						resultData.put(Constants.COMMENT_URL, commUrl);
						List<Map<String, Object>> tasks = new ArrayList<>();
						tasks.add(commMap);
						resultData.put(Constants.TASKS, tasks);
					}
					
				}
			}
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
