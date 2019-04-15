package com.bfd.parse.reprocess;

import java.util.HashMap;
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
 * @site:财界网 (N17ok)
 * @function 新闻内容页后处理插件-处理来源和发表时间
 * 
 * @author bfd_02
 *
 */

public class N17okContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N17okContentRe.class);

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}
		// 处理来源
		// 手机上财界 2015-03-13 14:20:50 来源： 腾讯科技
		// source来源： 新浪科技 2017-04-06 13:18:36
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			Matcher match = Pattern.compile("来源：\\s*(\\S*)\\s?").matcher(source);
			if(match.find()){
				source = match.group(1);
				resultData.put(Constants.SOURCE, source);
			}
		}
		
		//处理发表时间
		//post_time 手机上财界 2015-11-27 08:35:22 来源：
		if(resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+\\:\\d+\\:\\d+").matcher(postTime);
			if(match.find()){
				postTime = match.group();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
