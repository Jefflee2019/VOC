package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
 * 红豆社区列表页
 * 列表页
 * 后处理插件
 * @author bfd_05
 *
 */
public class BhongdouListRe implements ReProcessor{
	
	private static final Pattern PATTIME = Pattern
			.compile("[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日");
	private static final Log LOG = LogFactory.getLog(BhongdouListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getUrl();
		StringBuffer sb = new StringBuffer();
		if (resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if(!item.containsKey(Constants.POSTTIME))
				{
					item.put(Constants.POSTTIME, "2010-1-1 0:0:0");
				}
				if (!item.containsKey(Constants.REPLY_CNT)) {
					item.put(Constants.REPLY_CNT, -1024);
				}
			}
		}
				
			Map nextpageTask = new HashMap();
			String oldPageNum = getPage(url);
			int pageNum = Integer.valueOf(oldPageNum) + 1;
			url = url.replace("&pageno=" + oldPageNum, "&pageno=" + pageNum);
			sb.append(url);
			String sNextpageUrl = sb.toString();
			nextpageTask.put(Constants.LINK, sNextpageUrl);
			nextpageTask.put(Constants.RAWLINK, sNextpageUrl);
			nextpageTask.put(Constants.LINKTYPE, "bbspostlist");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.NEXTPAGE, sNextpageUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(nextpageTask);	
			}
	
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	private String getPage(String url) {
			Pattern pattern = Pattern.compile("&pageno=(\\d+)");
			Matcher matcher = pattern.matcher(url);
			String curPage;
			if (matcher.find()) {
				curPage = matcher.group(1);
				return curPage;
			}else {
				return "0";
			}
	}
	
}
