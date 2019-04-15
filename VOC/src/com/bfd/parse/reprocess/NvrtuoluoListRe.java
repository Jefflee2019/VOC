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
 * 站点：Nvrtuoluo
 * 功能：添加下一页URL
 * @author dph 2017年12月25日
 *
 */
public class NvrtuoluoListRe implements ReProcessor{

	private static final Pattern PATTERN_PAGE = Pattern.compile("page=(\\d+)"); 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		//添加下一页URL
		//http://www.vrtuoluo.cn/index.php?r=alltype/searchmore&s=%E5%8D%8E%E4%B8%BA&page=2
		String link = unit.getUrl();
		Matcher pageM = PATTERN_PAGE.matcher(link);
		while(pageM.find()){
			String page = pageM.group(0);
			String pageN = pageM.group(1);
			Integer  p = Integer.parseInt(pageN) + 1;
			link = link.replace(page, "page=" + p);
			Map<String,String> nextpagetask = new HashMap<String, String>();
			nextpagetask.put("link", link);
			nextpagetask.put("rawlink", link);
			nextpagetask.put("linktype", "newslist");
			resultData.put(Constants.NEXTPAGE, link);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(nextpagetask);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
