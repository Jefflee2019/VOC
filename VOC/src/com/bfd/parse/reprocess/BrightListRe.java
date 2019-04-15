package com.bfd.parse.reprocess;

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

/**
 * 恩山
 * 帖子列表页
 * 后处理插件
 * 
 * @author bfd_01
 *
 */
public class BrightListRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(BrightListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String, Object> item : items){
				if(item.containsKey(Constants.REPLY_CNT)){
					String replycnt = (String) item.get(Constants.REPLY_CNT);
					Pattern p = Pattern.compile("(\\d+) 个回复");
					Matcher m = p.matcher(replycnt);
					int cnt = 0;
					while (m.find()){
						cnt = Integer.valueOf(m.group(1));
					}
					item.put(Constants.REPLY_CNT, cnt);
				}
			}
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
