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
 * @site:C114 (B114)
 * @function 帖子列表页增加nextpage
 * 
 * @author bfd_04
 *
 */

public class BwfunListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BwfunListRe.class);
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.ITEMS)) {
				List items = (List)resultData.get(Constants.ITEMS);
				for (int i=0;i<items.size();i++) {
					Map map = (Map)items.get(i);
					//修改时间
					if(map.containsKey(Constants.POSTTIME)) {
						String posttime = map.get(Constants.POSTTIME).toString();
						Matcher match = DATE_PATTERN.matcher(posttime);
						if(match.find()) {
							posttime = match.group(1);
							map.put(Constants.POSTTIME, posttime);
						} else {
							map.put(Constants.POSTTIME, "");
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
