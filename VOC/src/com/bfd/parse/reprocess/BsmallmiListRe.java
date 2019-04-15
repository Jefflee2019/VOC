package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.Date;
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
 * techweb列表页 后处理插件
 * 
 * @author bfd_05
 * 
 */
public class BsmallmiListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BsmallmiListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if (item.containsKey(Constants.POSTTIME)) {
					String posttime = item.get(Constants.POSTTIME).toString().trim();
					if (posttime.matches("\\d+-\\d+ \\d+:\\d+:\\d+")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
						posttime = sdf.format(new Date()) + "-" + posttime;
					}else{
						posttime = ConstantFunc.convertTime(posttime);
					}
					item.put(Constants.POSTTIME, posttime);
				}
				//转换帖子url为正序
				if (item.containsKey(Constants.ITEMLINK)) {
					Map itemlink = (Map) item.get(Constants.ITEMLINK);
					String link = itemlink.get(Constants.LINK).toString() + "-1-o0";
					itemlink.put("link", link);
					itemlink.put("rawlink", link);
					item.put(Constants.ITEMLINK, itemlink);
				}
			}
		}
		
		if (resultData.containsKey(Constants.TASKS)) {
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			for (Map<String, Object> task : tasks) {
				//转换帖子url为正序
				if ("bbspost".equals(task.get(Constants.LINKTYPE).toString())) {
					String link = task.get(Constants.LINK).toString() + "-1-o0";
					task.put("link", link);
					task.put("rawlink", link);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
