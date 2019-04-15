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
 * @site Ejumbo
 * @function 处理商品名称省略情况
 * 
 * @author bfd_02
 *
 */
public class EjumboListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EjumboListPre.class);

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


		// 处理title显示不全
		Document doc = Jsoup.parse(pageData);
		// 获取所有商品信息所在的div
		Elements products = doc.select("li.clearfix");
		for (Element product : products) {
			// 获取标题的标签
			Elements titleEle = product.select("span.variant-title>a");
			String title = titleEle.attr("title");
			// 更新品牌的标签对象
			titleEle.html(title);
		}
		// 更新页面的修改操作
		pageData = doc.html();
		System.out.println(pageData);
		return pageData;
	}
}
