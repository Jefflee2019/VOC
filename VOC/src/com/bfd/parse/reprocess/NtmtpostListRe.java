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
 * 站点名：21cnit
 * <p>
 * 主要功能：处理生成任务的链接
 * 
 * @author bfd_01
 *
 */
public class NtmtpostListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(N21cnitListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String pagedata = unit.getPageData();
			String url = unit.getUrl();
			// 处理下一页链接
			if (pagedata.contains("下一页</button>")) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
//				http://www.tmtpost.com/tag/299177/2
				int pageIndex = Integer.parseInt(this.getCresult(url, "tag/\\d+/(\\d+)")) + 1;
				String nextpage = this.getCresult(url, "(http.*tag/\\d+)") + "/" + pageIndex;
				resultData.put(Constants.NEXTPAGE, nextpage);
				Map<String, Object> nextMap = new HashMap<String, Object>();
				nextMap.put("link", nextpage);
				nextMap.put("rawlink", nextpage);
				nextMap.put("linktype", "newslist");
				tasks.add(nextMap);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return "1";
	}
}
