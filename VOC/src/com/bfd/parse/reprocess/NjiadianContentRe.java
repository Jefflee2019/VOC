package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 站点名：家电网
 * <p>
 * 主要功能：处理抓取字段
 * @author bfd_01
 *
 */
public class NjiadianContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NjiadianContentRe.class);
	private static final Pattern PATTERN_CATE = Pattern.compile("当前位置：(.*)");
	private static final Pattern PATTERN_TITLE = Pattern.compile("<div class=\"article-header\"><h1>(.*?)</h1>");
	private static final Pattern PATTERN_DATE = Pattern.compile("<span class=\"date\">(.*?)</span>");
	private static final Pattern PATTERN_CONTENT = Pattern.compile("<span id=\"post1\"> <p>(.*?)</span>");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String data = unit.getPageData();
			String catesource = null;
			List<String> cate = null;
			catesource = parseByReg(data,PATTERN_CATE);
			cate = parseCate(resultData, catesource);
			resultData.put(Constants.CATE, cate);
			String title = null;
			title = parseByReg(data,PATTERN_TITLE);
			resultData.put(Constants.TITLE, title);
			String date = null;
			date = parseByReg(data,PATTERN_DATE);
			resultData.put(Constants.NEWSTIME, date);
			String content = null;
			content = parseByReg(data,PATTERN_CONTENT);
			content = content.replaceAll("<.*?>", "");
			resultData.put(Constants.CONTENT, content);
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

	private List<String> parseCate(Map<String, Object> resultData, String catesource) {
		String cate = null;
		cate = catesource.replaceAll("<.*?>", "");
		cate = cate.replaceAll("->", "-&gt;");
		String[] ca = cate.split("-&gt;");
		List<String> list = new ArrayList<String>();
		for(int i=0;i<ca.length;i++){
			list.add(ca[i].trim());
		}
		return list;
	}
	
	public String parseByReg(String data, Pattern p){
		Matcher mch = p.matcher(data);
		String result = null;
		if(mch.find()){
			result = mch.group(1);
		}
		return result;
	}
	
}
