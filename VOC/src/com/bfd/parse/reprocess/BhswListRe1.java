package com.bfd.parse.reprocess;

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
 * 华商论坛 列表页 后处理插件
 * 
 * @author bfd_05
 * 
 */
public class BhswListRe1 implements ReProcessor {

	private static final Pattern PATTIME = Pattern
			.compile("[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日");
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BhswListRe1.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
					.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				String posttime = "";
				if (item.containsKey(Constants.POSTTIME)) {
					posttime = (String) item.get(Constants.POSTTIME);
					if (posttime.contains("条回复")) {
						String[] posts = posttime.split("条回复");
						int replyCnt = Integer.valueOf(posts[0]);
						item.put(Constants.REPLY_CNT, replyCnt);
					} else{
						item.put(Constants.REPLY_CNT, -1024);
					}

					Matcher mch = PATTIME.matcher(posttime);
					if (mch.find()) {
						posttime = mch.group().replace("年", "-")
											   .replace("月", "-")
								               .replace("日", "");
					}
					else {
						posttime = ConstantFunc.convertTime(posttime.replace("-", ""));
					}
					item.put(Constants.POSTTIME, posttime);
				}
				else{
					long now = new Date().getTime();
					posttime = ConstantFunc.convertTime(String.valueOf(now/1000));
					item.put(Constants.POSTTIME, posttime);
					item.put(Constants.REPLY_CNT, -1024);
				}
			}
		}
		if (url.contains("&pn=750") && resultData.containsKey(Constants.NEXTPAGE)) {
			resultData.remove(Constants.NEXTPAGE);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			for (int i = 0; i < tasks.size();) {
				if (tasks.get(i).get(Constants.LINKTYPE).equals("bbspostlist")) {
					tasks.remove(i);
					continue;
				}
				i++;
			}
		}
		// LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
		// + JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
