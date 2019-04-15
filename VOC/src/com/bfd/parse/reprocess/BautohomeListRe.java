package com.bfd.parse.reprocess;

import java.util.HashMap;
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
 * @site Bautohome
 * @function 帖子列表页后处理
 * @author bfd_04
 * 
 */
public class BautohomeListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BautohomeListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = (String) item.get(Constants.POSTTIME);
					posttime = posttime.replace("发表于：", "").trim();
					item.put(Constants.POSTTIME, posttime);
				}
				if (item.containsKey(Constants.REPLY_CNT)) {
//					String replyCnt = (String) item.get(Constants.REPLY_CNT);
//					replyCnt = replyCnt.replace("个回复", "").trim();
//					item.put(Constants.REPLY_CNT, replyCnt);
					item.put(Constants.REPLY_CNT, -1024);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
}
