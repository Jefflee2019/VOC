package com.bfd.parse.reprocess;

import java.util.Arrays;
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
/**
 * @site：东方网
 * @function：处理作者，发表时间，来源等
 * @author bfd_04
 *
 */
public class NjuhangyeContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NjuhangyeContentRe.class);
	private static final Pattern TIME_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]"+
			"{2}\\s+[0-9]{2}:[0-9]{2}([0-9]{2})?");

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
//		String url = unit.getUrl();
		String url = result.getSpiderdata().get("location").toString();
		if(resultData != null && !resultData.isEmpty()) {
			/**
			 * "cate": [
             *  "您所在的位置： 首页 > 自贸区 > 正文"], 
			 */
			if(resultData.containsKey(Constants.CATE)) {
				List cate = (List)resultData.get(Constants.CATE);
				String[] temp = null;
				temp = cate.get(0).toString().replace("您所在的位置：", "").trim().split(">");
				cate = Arrays.asList(temp);
				resultData.put(Constants.CATE, cate);
			}
			/**
			 *  "post_time": "新华网 2016-03-20 16:28"
			 */
			if(resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				Matcher timeMatcher = TIME_PATTERN.matcher(postTime);
				if(timeMatcher.find()) {
					postTime = timeMatcher.group();
				}
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
