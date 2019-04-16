package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site Eclaro_peru
 * @function 处理商品名称
 *  
 * @author bfd_02
 *
 */
public class EcarrefourListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EcarrefourListPre.class);

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
		 * 处理产品名称省略情况
		 * 
		 */

		// 处理title显示不全
		Document doc = Jsoup.parse(pageData);
		// 获取所有商品信息所在的div
		Elements products = doc.select("div.comp-productcard__wrap");
		for (Element product : products) {
			// 获取商品名称的标签对象
			Element itemname = product.getElementsByClass("comp-productcard__name").get(0);
			String title = product.select("img.comp-productcard__img").eq(0).attr("title");
			// 更新品牌的标签对象
			itemname.html(title);
		}
		// 更新页面的修改操作
		pageData = doc.html();
		return pageData;
	}
}
