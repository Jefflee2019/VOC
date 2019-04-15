package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：Bchinamac
 * 功能：帖子页后处理插件
 * @author dph 2017年11月14日
 *
 */
public class BchinamacPostRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		
		//"newstime": "发表于 2007-7-6 00:46:23", 
		if(resultData.containsKey(Constants.NEWSTIME)){
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			newstime = newstime.replace("发表于", "").trim();
			resultData.put(Constants.NEWSTIME, newstime);
		}
		List<Map<String,Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
		Iterator<?> iterator = replys.iterator();
		while(iterator.hasNext()){
			Map<String,Object> reply = (Map<String, Object>) iterator.next();
			String replyfloor = (String) reply.get(Constants.REPLYFLOOR);
			//回复的一楼为帖子内容，删除
			String firstfloor = "1";
			if(replyfloor.equals(firstfloor)){
				iterator.remove();
				continue;
			}
			String replydate = (String) reply.get(Constants.REPLYDATE);
			replydate = replydate.replace("发表于", "").trim();
			reply.put(Constants.REPLYDATE,replydate);
			int floor = Integer.parseInt(replyfloor);
			if(floor > 10 && resultData.containsKey(Constants.AUTHOR)){
				resultData.remove(Constants.AUTHOR);
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.AUTHORNAME);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
