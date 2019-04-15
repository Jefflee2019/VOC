package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：亚马逊海外
 * 功能：商品内容页处理 
 * @author dph 2018年1月24日
 *
 */
public class EamazonukContentRe implements ReProcessor{

	private static final Pattern PATTERN_ID = Pattern.compile("dp/(.*)/ref");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// REPLY_CNT
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			String replyCnt = (String) resultData.get(Constants.REPLY_CNT);
			replyCnt = replyCnt.substring(0, replyCnt.indexOf(' ')).replace(
					",", "");
			resultData.put(Constants.REPLY_CNT, replyCnt);
		}
		// 添加COMMENT_URL
		String url = unit.getUrl();
		Matcher idM = PATTERN_ID.matcher(url);
		if(idM.find()){
			String id = idM.group(1);
			String commenturl = "https://www.amazon.co.uk/Huawei-Mate-Pro-SIM-Free-Smartphone-Grey/product-reviews/" + id;
			resultData.put(Constants.COMMENT_URL, commenturl);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
