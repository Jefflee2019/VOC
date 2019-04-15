package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NandroidchineseCommentPre implements PreProcessor {

	private static final Log LOG = LogFactory
			.getLog(NandroidchineseCommentPre.class);
	private static final Pattern COMMENT_CONTENTS = Pattern
			.compile("<div class=\"comment-item cl\">[\\s\\S]*?</div>");
	private static final Pattern CONTENTS = Pattern
			.compile("</span></a><br/>([\\s\\S]*?)<div class=\"cl comment-fb\">");
	private static final Pattern UP_CNT = Pattern
			.compile("<a href=\"javascript:;\" >赞\\(<span class=\"num\">(\\d+)</span>\\)");

	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		Matcher matcherreplynpage = COMMENT_CONTENTS.matcher(pageData);

		while (matcherreplynpage.find()) {
			try {
				String attrs = matcherreplynpage.group(0);
				attrs = attrs.replaceAll("&nbsp;", "\n");
				Matcher matcherreplynpageA = CONTENTS.matcher(attrs);
				Matcher matcherUpcnt = UP_CNT.matcher(attrs);

				if (matcherreplynpageA.find()) {
					String content = matcherreplynpageA.group(1).trim();
					String posttab = "<div class=\"content\">" + content
							+ "</div>";
					pageData = pageData.replace(content, posttab);
				}
				if (matcherUpcnt.find()) {
					String div = matcherUpcnt.group().trim();
					String upCnt = matcherUpcnt.group(1).trim();
					String posttab = "<div class=\"upcnt\">" + upCnt
							+ "</div>";
					pageData = pageData.replace(div, posttab);
				}
				
				
			} catch (Exception e) {
				LOG.warn("excuteParse error");
			}
		}
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
		}
		unit.setPageEncode("utf8");
		return true;
	}

}
