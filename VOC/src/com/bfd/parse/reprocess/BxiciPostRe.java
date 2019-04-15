package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 手机之家论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BxiciPostRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BxiciPostRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		String url = unit.getUrl();
		Pattern p = Pattern.compile("(.*)?/d\\d+[.\\d+]*.htm");
		Matcher indexMch = p.matcher(url);
		if(indexMch.find()){
			Pattern nextPat = Pattern.compile("<a name='nextPage' href=\"(/d\\d+[.\\d+]*.htm)\" title=\"下一页\">下一页</a>");
			Matcher m = nextPat.matcher(pageData);
			if(m.find()){
				String nextpage = new StringBuilder().append(indexMch.group(1)).append(m.group(1)).toString();
				List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
				Map<String, Object> nextMap = new HashMap<String, Object>();
				nextMap.put("link", nextpage);
				nextMap.put("rawlink", nextpage);
				nextMap.put("linktype", "bbspost");
				tasks.add(nextMap);
				resultData.put(Constants.NEXTPAGE, nextMap);
				resultData.put(Constants.TASKS, tasks);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
