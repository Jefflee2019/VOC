package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Bcnmo
 * 
 * 主要功能: 
 * 
 * @author bfd_03
 *
 */
public class EkilimallListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EkilimallListPre.class);

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
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		//处理title显示不全
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(pageData);
		try {
			TagNode tag = (TagNode) root.evaluateXPath("//div[@class='new-pagination']")[0];
			TagNode tag1 = (TagNode) tag.evaluateXPath("//a[span='>']")[0];
			tag1.getParent().addAttribute("class", "next");

			pageData = cleaner.getInnerHtml(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return pageData;
	}
	
}
