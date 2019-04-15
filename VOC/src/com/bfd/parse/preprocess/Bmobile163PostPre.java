package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 网易手机/数码-论坛(Bmobile163)
 * @function deal楼主和回复人等级
 * 
 * @author bfd_02
 *
 */
public class Bmobile163PostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(Bmobile163PostPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();

		pageData = parsePageData(pageData);

		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.warn("Charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	public String parsePageData(String pageData) {

		/**
		 * 给reply_level和author_level添加span标签附带等级值
		 */
		if (pageData.contains("<div class=\"info-class-bar\">")) {
			Matcher m = Pattern
					.compile(
							"<div\\s+class=\"info-class-bar\">\\s+<img\\s*src=\"[:\\w/\\.\\s]+\"\\s*\\S+\\s*title=\"等级:(\\d+)\"\\s*/>",
							Pattern.DOTALL).matcher(pageData);
			while (m.find()) {
				String str = m.group();
				// 给等级添加一个span，将author_level和reply_level放入span中
				pageData = pageData.replace(str, str + "<span>" + m.group(1) + "</span>");
			}
		}

		/**
		 * 给下一页添加一个class属性，其值为"nextpage"
		 */

		if (pageData.contains(">下一页&gt;</a>")) {
			pageData = pageData.replace(">下一页&gt;</a>", "class=\"nextpage\">下一页&gt;</a>");
		}

		return pageData;
	}
}
