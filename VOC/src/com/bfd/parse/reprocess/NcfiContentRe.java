package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 	@site：中财网
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NcfiContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcfiContentRe.class);
	private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]{4}年[0-9]{2}月[0-9]{2}日"
			+ ""+
			"\\s+[0-9]{2}:[0-9]{2}:[0-9]{2})"); //2016年03月08日 15:15:45
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 * "post_time": "时间：2016年03月08日 15:15:45 中财网"
			 */
			 if(resultData.containsKey(Constants.POST_TIME)) {
				 String postTime = resultData.get(Constants.POST_TIME).toString();
				 Matcher timeMatcher = TIME_PATTERN.matcher(postTime);
				 if(timeMatcher.find()) {
					 postTime = timeMatcher.group(1);
				 }
				 resultData.put(Constants.POST_TIME, postTime);
			 }
			 /**
			  *  "source": "时间：2016年03月08日 15:15:45 中财网", 
			  */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 String[] tempArr = source.split(resultData.get(Constants.POST_TIME).toString());
				 if(tempArr.length > 1) {
					 source = tempArr[1].trim();
				 }
				 resultData.put(Constants.SOURCE, source);
			 }
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
