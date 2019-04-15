package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nkdslife
 * 
 * 功能：处理边界问题 给出评论页连接
 * 
 * @author bfd_06
 */
public class NkdslifeContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
				.get(Constants.TASKS);
		/**
		 * 标准化部分字段
		 */
		// SOURCE
		if (resultData.containsKey(Constants.SOURCE)) {
			formatAttr(Constants.SOURCE,
					(String) resultData.get(Constants.SOURCE), resultData);
		}
		/**
		 * 删除最后一页的任务
		 */
		if (resultData.containsKey(Constants.NEXTPAGE)
				&& !matchTest("title='下一页'>", unit.getPageData())) {
			resultData.remove(Constants.NEXTPAGE);
			rtasks.remove(0);
		}
		/**
		 * 加上评论页链接
		 */
		Map<String, Object> rtask = new HashMap<String, Object>();
		String nextUrl = unit.getUrl();
		nextUrl += "#comment";
		rtask.put("link", nextUrl);
		rtask.put("rawlink", nextUrl);
		rtask.put("linktype", "newscomment");
		rtasks.add(rtask);
		resultData.put(Constants.COMMENT_URL, nextUrl);
		ParseUtils.getIid(unit, result);
		
		return new ReProcessResult(processcode, processdata);
	}

	public Boolean matchTest(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		return matcher.find();
	}
	
	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		if(keyName.equals(Constants.SOURCE)){
			int indexA = value.indexOf("：");
			if(value.contains("［")){
				int indexB = value.indexOf("［");
				value = value.substring(indexA+1,indexB-1);
			} else {
				value = value.substring(indexA+1);
			}
			
			result.put(keyName, value);
		}
	}
	
}
