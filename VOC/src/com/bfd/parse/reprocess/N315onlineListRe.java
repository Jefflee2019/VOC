package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：N315online
 * 
 * 主要功能：处理列表页最后一页然后会生成本页的链接问题
 * 
 * @author bfd_03
 *
 */
public class N315onlineListRe implements ReProcessor {

	@SuppressWarnings("rawtypes")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		String url = (String) unit.getTaskdata().get("url");
		Pattern pattern = Pattern.compile("&page=(\\d+)");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			int curPageNo = Integer.parseInt(matcher.group(1));
			String pageData = unit.getPageData();
			Pattern pat = Pattern.compile("&page=(\\d+)\" class=\"a1\">下一页");
			Matcher mat = pat.matcher(pageData);
			if(mat.find()){
				int nextPageNo = Integer.parseInt(mat.group(1));
				if(curPageNo >= nextPageNo){
					resultData.remove(Constants.NEXTPAGE);
					List tasks = (ArrayList) resultData
							.get(Constants.TASKS);
					for (int i = 0; i < tasks.size();) {
						if (((Map) tasks.get(i)).get(Constants.LINKTYPE).equals("newslist")) {
							tasks.remove(i);
							continue;
						}
						i++;
					}
				}
			}
			
		}
		
		if (url.contains("so.com")) {
			String pagedata = null;
			pagedata = unit.getPageData();
			url = unit.getUrl();
			// 做url的处理，获得重定向之后的url
			if (resultData.containsKey(Constants.TASKS)) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				ConstantFunc.decodeLink(tasks, pagedata);
				// 处理下一页链接
				if (pagedata.contains(">下一页<") && url.contains("www.so.com")) {
					ConstantFunc.getNextpage(resultData, url, tasks);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
