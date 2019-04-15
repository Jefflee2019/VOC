package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 中关村论坛 列表页 后处理插件
 * 
 * @author bfd_05
 * 
 */
public class BzolListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BzolListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String, Object> item : items){
				if(item.containsKey(Constants.POSTTIME)){
					String posttime = (String) item.get(Constants.POSTTIME);
					posttime = ConstantFunc.convertTime(posttime);
					item.put(Constants.POSTTIME, posttime);
				}
				if(item.containsKey(Constants.REPLY_CNT)){
					String cnt = item.get(Constants.REPLY_CNT).toString();
					Pattern p = Pattern.compile("(\\d+)");
					Matcher mch = p.matcher(cnt);
					if(mch.find()){
						int replyCnt = Integer.valueOf(mch.group(1));
						item.put(Constants.REPLY_CNT, replyCnt);
					}
				}
			}
		}
		// LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
		// + JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
