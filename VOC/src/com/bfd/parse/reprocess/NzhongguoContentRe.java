package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：中国网
 * <p>
 * 主要功能：处理取到的数据，发表时间，来源等，过滤不需要的内容
 * @author bfd_01
 *
 */
public class NzhongguoContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NzhongguoContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if(resultData.containsKey(Constants.SOURCE)){
				String source = (String) resultData.get(Constants.SOURCE);
				source = source.replace("来源：", "").replace("来源 ：", "").trim();
				resultData.put(Constants.SOURCE, source);
			}
			if(resultData.containsKey(Constants.POST_TIME)){
				String postTime = (String) resultData.get(Constants.POST_TIME);
				postTime = postTime.replace("发布时间：", "").trim();
				postTime = ConstantFunc.getDate(postTime);
				String[] time = postTime.split(" ");
				postTime = time[0] + " " + time[1];
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
	
//	private String getTime(String newstime) {
//		Pattern p = Pattern.compile("(\\d{4}[年|-]{1}\\d{2}[月|-]{1}\\d{2}[日|\\s]+[\\S]*:\\d{2}:\\d{2})");
//		Matcher m = p.matcher(newstime);
//		while (m.find()) {
//			newstime = m.group(1);
//		}
//		return newstime;
//	}
}
