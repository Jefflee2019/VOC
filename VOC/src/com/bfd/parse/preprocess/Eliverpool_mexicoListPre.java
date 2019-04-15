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
public class Eliverpool_mexicoListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(Eliverpool_mexicoListPre.class);

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
		System.out.println(pageData);
		if (pageData.contains("Precio Lista:")) {
			pageData = pageData.replaceAll("Precio Lista:\\s*", "");
		}
		
		// 去掉价格指数
		if(pageData.contains("<sup>")) {
			pageData = pageData.replaceAll("<sup>\\S*</sup>", "");
		}

		// 处理title显示不全
		Document doc = Jsoup.parse(pageData);
		// 获取所有商品信息所在的div
		Elements products = doc.select("span.product-price");
		for (Element product : products) {
			if(product.getElementsByClass("precio-promocion").hasText()) {
				// 获取货币符号对象
				Element symbolEle = product.getElementsByClass("currency-symbol").get(0);
				String symbol = symbolEle.text();
				String price = product.select("p.precio-promocion")
						       .select("span.price-amount").text();
				price = symbol+price;
				// 更新价格对象
				symbolEle.html(price);
			}else {
				// 获取货币符号对象
				Element symbolEle = product.getElementsByClass("currency-symbol").get(0);
				String symbol = symbolEle.text();
				String price = product.select("p.precio-especial")
						       .select("span.price-amount").text();
				price = symbol+price;
				// 更新价格对象
				symbolEle.html(price);
			}
		}
		// 更新页面的修改操作
		pageData = doc.html();
		return pageData;
	}
}
