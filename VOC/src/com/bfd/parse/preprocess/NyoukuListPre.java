package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NyoukuListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NyoukuListPre.class);
	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData, unit);
		
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData,ParseUnit unit) {
		Pattern pattern = Pattern.compile("<script type=\"text/javascript\">bigview.view.*?</script>");
		Matcher matcher = pattern.matcher(pageData);
		String json = "";
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		if(matcher.find()){
			json = matcher.group();
		}
		try {
			if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
					&& (json.indexOf("[") < json.indexOf("{"))) {
				json = json.substring(json.indexOf("["),
						json.lastIndexOf("]") + 1);
			} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
				json = json.substring(json.indexOf("{"),
						json.lastIndexOf("}") + 1);
			}
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
		}
		String html = (String) jsonMap.get("html");
		StringBuffer buffer = new StringBuffer();
		buffer.append("<!doctype html>");
		buffer.append("<html lang=\"en\">");
		buffer.append("<head>");
		buffer.append("<title>Document</title>");
		buffer.append("</head>");
		buffer.append("<body>");
		buffer.append(html);
		buffer.append("</body>");
		buffer.append("</html>");
		pageData = buffer.toString();
//		System.err.println(pageData.substring(0, 500));
		return pageData;
	}
}
