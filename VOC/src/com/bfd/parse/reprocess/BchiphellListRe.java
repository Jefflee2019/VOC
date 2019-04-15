package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点:Bchiphell
 * 功能：处理回复数、发布时间等字段
 * @author dph 2017年12月15日
 *
 */
public class BchiphellListRe implements ReProcessor{

	@SuppressWarnings({ "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>(5);
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String,Object>> itemList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String,Object> item : itemList){
				//"posttime": "发布：2015-05-23"
				if(item.containsKey(Constants.POSTTIME)){
					String posttime = (String) item.get(Constants.POSTTIME);
					posttime = posttime.replace("发布：", "").trim();
					item.put(Constants.POSTTIME,posttime);
				}
				//"reply_cnt": "回复：14", 
				// "reply_cnt": "",
				if(item.containsKey(Constants.REPLY_CNT)){
					String replyCnt = (String) item.get(Constants.REPLY_CNT);
					if(null != replyCnt && replyCnt != ""){
						replyCnt = replyCnt.replace("回复：", "");
					}else{
						replyCnt = "0";
					}
					item.put(Constants.REPLY_CNT, replyCnt);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
