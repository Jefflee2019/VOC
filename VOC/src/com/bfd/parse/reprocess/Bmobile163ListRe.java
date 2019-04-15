package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:网易手机/数码-论坛
 * @function 论坛列表页后处理插件 格式化回复数和发表时间 
 * 
 * @author bfd_02
 *
 */

public class Bmobile163ListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(Bmobile163ListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}
	
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			if (items != null && !items.isEmpty()) {
				for (Map<String,Object> item : items) {
					//deal reply_cnt
					String oldReplyCnt = item.get(Constants.REPLY_CNT).toString();
					if(oldReplyCnt.contains("/")) {
					String[] cntArr = oldReplyCnt.split("/");
					int replyCnt = Integer.parseInt(cntArr[1].trim());
					item.put(Constants.REPLY_CNT, replyCnt);
					}else {
						item.put(Constants.REPLY_CNT, 54);
					}
					//deal posttime
					String oldPosttime = item.get(Constants.POSTTIME).toString();
					String posttime = ConstantFunc.convertTime(oldPosttime);
					item.put(Constants.POSTTIME, posttime);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
