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
public class EaxiomListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EaxiomListPre.class);

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
			Object[] objs = root.evaluateXPath("//span[@class='variant-title']/a");
			for (Object obj : objs) {
				TagNode tag = (TagNode) obj;
				String title = tag.getAttributeByName("title");
//				System.err.println(tag.hasChildren() + title);
				tag.removeAllChildren();
				cleaner.setInnerHtml(tag, title);
			}
			pageData = cleaner.getInnerHtml(root);
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pageData;
	}
	
}
