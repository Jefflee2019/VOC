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
 * @ClassName: BfengListRe
 * @author: taihua.li
 * @date: 2019年3月20日 下午3:13:51
 * @Description:TODO(处理威锋论坛列表页中部分模糊的发帖时间，如“昨天 17:35”，“3天前”等)
 */
public class BfengListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BfengListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			if (items != null && !items.isEmpty()) {
				for (Map<String, Object> item : items) {
					if (item.containsKey(Constants.POSTTIME)) {
						String posttime = item.get(Constants.POSTTIME).toString();
						posttime = ConstantFunc.convertTime(posttime);
						item.put(Constants.POSTTIME, posttime);
					}
				}
			} else {
				LOG.info("items is null " + unit.getUrl());
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
