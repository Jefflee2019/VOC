package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 京东商城(Ejd_wlm)
 * @function 1、针对列表页中"抢购中"的商品价格，不在标签体中，而在标签属性中
 *           2、针对ccc-x.jd这类随机url的处理(ccc-x类部分随机生成，而且会不定时变化，
 *              导致消重失效，种子任务持续无限增加)，用item.jd这类进行替换
 * @author lth
 *
 */
public class Ejd_wlmListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(Ejd_wlmListPre.class);

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

	public String parsePageData(String data) {
		// 将data-price的值全部匹配出来，放在集合priceList中
		String datarex = "data-price=\"(\\d+.\\d+)\"";
		List<String> priceList = getRex(data, datarex);
		// 将所要插入的位置匹配出来，放在集合placeList中
		String docRex = "(</em><i></i></strong>)";
		List<String> placeList = getRex(data, docRex);
		if (priceList.size() == placeList.size()) {
			for (int i = 0; i < priceList.size(); i++) {
				for (int j = i; j <= i; j++) {// 内层循环每次只需1个索引，和外层循环对应的位置，以便准确替换
					data = data.replaceFirst(placeList.get(j), "</em><i>" + priceList.get(j) + "</i></strong>");
				}
			}
		}

		// 处理ccc-x.jd类url
		Document doc = Jsoup.parse(data);
		// 提取商品名称、url所在的div块
		Elements productEle = doc.select("div.p-name.p-name-type-2");
		for (Element element : productEle) {
			// 当前商品url
			String urlhref = element.select("a").get(0).attr("href");
			// 开始处理ccc-x这类url
			if (urlhref.contains("ccc-x.jd.com")) {
				// 提取商品url标签
				Element urlEle = element.getElementsByTag("a").get(0);
				// 去掉当前ccc-x类的url链接属性
				urlEle.removeAttr("href");
				// 商品itemNo所在
				String itemNoStr = element.select("a").get(0).attr("onclick");
				Matcher match = Pattern.compile("searchlog\\(\\d+,(\\d+),\\d+,\\d+").matcher(itemNoStr);
				if (match.find()) {
					String itemno = match.group(1);
					// 拼接处对应准确的item.jd类url
					urlhref = new StringBuffer().append("//item.jd.com/").append(itemno).append(".html").toString();
					// 将拼接好的精准url链接属性放入
					urlEle.attr("href", urlhref);
				}
			}
		}
		// 更新html替换操作，相当于update
		data = doc.html();
		return data;
	}

	private static List<String> getRex(String data, String datarex) {
		Matcher match = Pattern.compile(datarex).matcher(data);
		// 放在属性中的价格
		List<String> priceList = new ArrayList<String>();
		// 匹配出价格
		while (match.find()) {
			String itemprice = match.group(1);
			priceList.add(itemprice);
		}
		return priceList;
	}
}
