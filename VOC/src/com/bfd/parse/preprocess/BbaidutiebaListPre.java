package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class BbaidutiebaListPre implements PreProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BbaidutiebaListPre.class);

	private static final Pattern PATTERNNEXTPAGE = Pattern
			.compile("<form action\\=\".*?\" method\\=\"get\"><div class\\=\"bc p\">[\\s\\S]*?</div></form>");
	private static final Pattern PATTERNNEXTPAGEA = Pattern
			.compile("<a accesskey\\=\"\\d+\" href=.*?\">下一页</a>");

	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		Matcher matcherreplynpage = PATTERNNEXTPAGE.matcher(pageData);

		if (matcherreplynpage.find()) {
			try {
				String attrs = matcherreplynpage.group(0);
				Matcher matcherreplynpageA = PATTERNNEXTPAGEA.matcher(attrs);
				if (matcherreplynpageA.find()) {
					String nextpage = matcherreplynpageA.group(0);
					String posttab = "<div class=\"nextpage_new\">" + nextpage
							+ "</div>";
					pageData = pageData.replace(nextpage, posttab);
				}
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("regex parse error");
			}
		}
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}
}