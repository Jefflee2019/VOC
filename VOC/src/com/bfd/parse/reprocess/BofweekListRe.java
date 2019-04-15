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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site:光电社区(Bofweek)
 * @function 帖子列表页
 * 
 * @author bfd_04
 *
 */

public class BofweekListRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BofweekListRe.class);
	private static final Pattern PATTERN = Pattern.compile("(\\d+) 个回复");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		if(resultData.containsKey("items")) {
			List itemList = (List)resultData.get("items");
			if(itemList !=null && !itemList.isEmpty()) {
				for(Object obj: itemList) {
					Map tempMap = (Map)obj;
					if(tempMap.containsKey(Constants.REPLY_CNT)) {
						String replyCnt = tempMap.get(Constants.REPLY_CNT).toString();
						Matcher match = PATTERN.matcher(replyCnt);
						if(match.find()) {
							replyCnt = match.group(1);
						} else {
							replyCnt = "-1024";
						}
						tempMap.put(Constants.REPLY_CNT, replyCnt);
					}
				}
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
