package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:C114 (B114)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_04
 *
 */

public class B114PostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(B114PostRe.class);

	@SuppressWarnings({ "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.info("未获取到解析数据");
			return null;
		}
		List<Map<String,Object>> listData = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
		Iterator<Map<String,Object>> it = listData.iterator();
		while(it.hasNext()){
			Map<String,Object> map = it.next();
			//删除一楼楼主层
			if(map.get(Constants.REPLYFLOOR).equals("1")){
				it.remove();
				continue;
			}
			// "replydate": "发表于 2015-10-29 12:33:44",
			if(map.containsKey(Constants.REPLYDATE)){
				String date = (String) map.get(Constants.REPLYDATE);
				date = date.replace("发表于", "").trim();
				map.put(Constants.REPLYDATE, date);
			}
		}
		//"newstime": "发表于 2015-10-26 23:52:58", 
		if(resultData.containsKey(Constants.NEWSTIME)){
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			newstime = newstime.replace("发表于", "").trim();
			resultData.put(Constants.NEWSTIME, newstime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
