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

public class NycwbContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NycwbContentRe.class);

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			if (resultData.containsKey("post_time")) {
				String sPostTime = resultData.get("post_time").toString();
				Pattern pattern = Pattern.compile("\\d{4}.\\d{1,2}.\\d{1,2}.(\\d{1,2}:\\d{1,2})?(:\\d{1,2})?");
				Matcher matcher = pattern.matcher(sPostTime);
				if (matcher.find()) {
					sPostTime = matcher.group();
					resultData.put("post_time", sPostTime);
				}
			}
			if (resultData.containsKey("source")) {
				String sSource = resultData.get("source").toString();
				sSource = sSource.replace("：", ":");
				Pattern pattern = Pattern.compile("来源:\\s*(\\S+)");
				Matcher matcher = pattern.matcher(sSource);
				if (matcher.find()) {
					sSource = matcher.group(1);
				} else {
					String[] sArray = sSource.split("\\s");
					if ((sArray != null) && (sArray.length > 2)) {
						sSource = sArray[0];
					} else {
						sSource = null;
					}
				}
				if (sSource !=null) {
					resultData.put("source", sSource);
				} else {
					resultData.remove(Constants.SOURCE);
				}
			}
			if (resultData.containsKey("author")) {
				String sAuthor = resultData.get("author").toString();
				sAuthor = sAuthor.replace("：", ":").replaceAll("\\s+", "");
				Pattern pattern = Pattern.compile("(作者|编辑):(\\S+)");
				Matcher matcher = pattern.matcher(sAuthor);
				if (matcher.find()) {
					sAuthor = matcher.group(2);
					resultData.put("author", sAuthor);
				} else {
					resultData.remove("author");
				}
			}
			LOG.info("url:" + unit.getUrl() + ".after reprocess rs is " + JsonUtil.toJSONString(resultData));
		} else {
			LOG.info("url:" + unit.getUrl() + "result.getParsedata().getData() is null");
		}
		return new ReProcessResult(0, processdata);
	}
}