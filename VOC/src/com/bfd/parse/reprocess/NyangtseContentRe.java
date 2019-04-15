package com.bfd.parse.reprocess;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NyangtseContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NyangtseContentRe.class);

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			if (resultData.containsKey("post_time")) {
				String sPostTime = resultData.get("post_time").toString();
				Pattern pattern = Pattern.compile("\\d{4}[-|/]\\d{1,2}[-|/]\\d{1,2}\\s(\\d{1,2}:\\d{1,2})?(:\\d{1,2})?");
				Matcher matcher = pattern.matcher(sPostTime);
				if (matcher.find()) {
					sPostTime = matcher.group();
					sPostTime = sPostTime.replaceAll("/", "-");
					resultData.put("post_time", sPostTime);
				}
			}
			if (resultData.containsKey("source")) {
				String sSource = resultData.get("source").toString();
				sSource = sSource.replace("：", ":");
				if (sSource.contains("发布于") && sSource.contains("来源:")) {
					sSource = sSource.split("发布于")[0].replace("来源:", "");
				} else if (sSource.contains("来源:")) {
					sSource = sSource.replace("来源:", "").trim();
					if(sSource.indexOf(" ") > 0) { // 可能后边还有其他不需要的信息
						sSource = sSource.substring(0, sSource.indexOf(" "));
					}
				}
				resultData.put(Constants.SOURCE, sSource);
			}
			LOG.info("url:" + unit.getUrl() + ".after reprocess rs is " + JsonUtil.toJSONString(resultData));
		} else {
			LOG.info("url:" + unit.getUrl() + "result.getParsedata().getData() is null");
		}
		return new ReProcessResult(0, processdata);
	}
}