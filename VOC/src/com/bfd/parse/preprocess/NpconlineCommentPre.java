package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 太平洋电脑网-新闻(Npconline)
 * @function 下一页正常翻页异常
 * 
 * @author bfd_02
 *
 */
public class NpconlineCommentPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NpconlineCommentPre.class);

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
		 * 解决翻页正常问题，给下一页<a>添加一个class
		 * 
		 */
		if (pageData.contains(">下一页<")) {
			int index = pageData.indexOf(">下一页<");
			pageData = pageData.substring(0, index) + " class='nextpage'"
					+ pageData.substring(index);
		}
		return pageData;
	}
}
