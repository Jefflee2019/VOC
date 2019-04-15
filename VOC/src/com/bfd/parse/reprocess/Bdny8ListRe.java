package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 南阳社区论坛帖子列表页
 * 后处理插件
 * @author bfd_05
 *
 */
public class Bdny8ListRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String, Object> item :items){
				if(item.containsKey(Constants.REPLY_CNT)){
					String reply_cnt = item.get(Constants.REPLY_CNT).toString();
					item.put(Constants.REPLY_CNT, reply_cnt.substring(0, reply_cnt.indexOf("个回复")).trim());
					item.put(Constants.VIEW_CNT, reply_cnt.substring(reply_cnt.indexOf("-")+1, reply_cnt.indexOf("次查看")).trim());
				}
				if(item.containsKey(Constants.POSTTIME)){
					String posttime = ConstantFunc.convertTime(item.get(Constants.POSTTIME).toString().trim());
					item.put(Constants.POSTTIME, posttime);
				}
			}
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
