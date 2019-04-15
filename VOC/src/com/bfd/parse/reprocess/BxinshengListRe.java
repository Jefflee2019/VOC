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
import com.bfd.parse.util.ParseUtils;

/**
 * 心声社区帖子列表页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BxinshengListRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BxinshengListRe.class);
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s[0-9]{2}:[0-9]{2}");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getTaskdata().get("url").toString();
		String[] urls = url.split("&p=");
		int pageIndex = 1;
		if(urls.length > 1){
			pageIndex = Integer.valueOf(urls[1]);
		}
		String nextpage = urls[0] + "&p=" + (pageIndex + 1);
		
		Map<String, Object>  nextMap = new HashMap<String, Object>();
		nextMap.put("link", nextpage);
		nextMap.put("rawlink", nextpage);
		nextMap.put("linktype", "bbspostlist");
		resultData.put("nextpage", nextpage);
		if(resultData.containsKey("tasks")){
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			tasks.add(nextMap);
		}
		if(resultData.containsKey("items")){
			List<Map<String, Object>> items  = (List<Map<String, Object>>) resultData.get("items");
			for(Map<String, Object> item : items){
				if(item.containsKey(Constants.POSTTIME)){
					String  resultStr = (String) item.get(Constants.POSTTIME);
					Matcher mch = PATTIME.matcher(resultStr);
					if(mch.find()){
						resultStr = mch.group(0);
					}
					item.put(Constants.POSTTIME, resultStr.trim());
				}
			}
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
