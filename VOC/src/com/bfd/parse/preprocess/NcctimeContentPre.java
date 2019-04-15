package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site：飞象网(新闻)
 * @function: 给摘要添加class="brief"属性
 * 
 * @author bfd_02
 *
 */
public class NcctimeContentPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NcctimeContentPre.class);
	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
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

	/**
	 * <P>
	 * &nbsp;&nbsp;&nbsp; <STRONG>摘要</STRONG>
	 * 
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		if (pageData.contains("<P>&nbsp;&nbsp;&nbsp; <STRONG>摘要</STRONG>")) {
			pageData = pageData.replace("<P>&nbsp;&nbsp;&nbsp; <STRONG>摘要</STRONG>",
					"<P class='brief'>&nbsp;&nbsp;&nbsp; <STRONG>摘要</STRONG>");
		}
		return pageData;
	}

}
