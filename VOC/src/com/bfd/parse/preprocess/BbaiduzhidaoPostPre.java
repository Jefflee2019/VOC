package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 百度知道
 * @function 网友采纳部分修改clss属性，使之在多模板间通用
 * @author bfd_02
 *
 */
public class BbaiduzhidaoPostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostPre.class);
	private static final Pattern CHARSETPATTERN = Pattern
			.compile("(meta|\\?\\s?xml).*?(charset=|encoding=)\"?([\\w-]+)");

	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		String encode = unit.getPageEncode();
		Matcher charsetM = CHARSETPATTERN.matcher(pageData);

		if (charsetM.find()) {
			String relCode = charsetM.group(3);
			if (!encode.equals(relCode)) {
				try {
					pageData = new String(unit.getPageBytes(), relCode);
				} catch (UnsupportedEncodingException e) {
					LOG.warn("charsetTransform exception");
				}
			}
		}

		// class = "wgt-recommend mod-shadow "
		// class = "wgt-recommend mod-shadow has-other"
		if (pageData.contains("wgt-recommend mod-shadow has-other")) {
			pageData = pageData.replace("wgt-recommend mod-shadow has-other", "wgt-recommend mod-shadow ");
		}

		// class = "grid f-aid"
		// class = "grid f-aid ml-20"
		if (pageData.contains("grid f-aid ml-20")) {
			pageData = pageData.replace("grid f-aid ml-20", "grid f-aid");
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
