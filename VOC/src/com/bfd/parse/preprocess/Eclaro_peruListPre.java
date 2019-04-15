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
 * @function 处理商品名称和价格
 * 
 * @author bfd_02
 *
 */
public class Eclaro_peruListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(Eclaro_peruListPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		// System.out.println(pageData);
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
		 * 处理价格前缀描述和产品名称
		 * 
		 */
		if (pageData.contains("Precio total: ")) {
			pageData = pageData.replaceAll("Precio total:\\s*", "");
		}

		// 处理title显示不全
		Document doc = Jsoup.parse(pageData);
		// 获取所有商品信息所在的div
		Elements products = doc.select("div.box-producto-in");
		for (Element product : products) {
			// 获取品牌的标签对象
			Element brand = product.getElementsByTag("h2").get(0);
			String brandname = brand.text();
			String title = product.getElementsByTag("h3").text();
			brandname = brandname + " " + title;
			// 更新品牌的标签对象
			brand.html(brandname);
		}
		// 更新页面的修改操作
		pageData = doc.html();
		return pageData;
	}
}
