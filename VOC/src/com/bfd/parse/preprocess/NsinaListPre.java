package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 新浪网-新闻(Nsina)
 * @function 下一页正常翻页异常
 * 
 * @author bfd_02
 *
 */
public class NsinaListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NsinaListPre.class);

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
		 * 处理价格前缀描述和产品名称
		 * 
		 */
		if (pageData.contains("Precio total: ")) {
			pageData = pageData.replaceAll("Precio total:\\s*", "");
		}
		
		String brandRex = "<h2>(.*?)</h2>";
		String titleRex = "<h3>(.*?)</h3>";
		Matcher match = Pattern.compile(titleRex).matcher(pageData);
		while(match.find()) {
			String brand = match.group(1);
			Matcher match2 = Pattern.compile(brandRex).matcher(pageData);
			if(match2.find()) {
				String title = match2.group(1);
				// 将品牌和标题合并成商品名称
				pageData = pageData.replaceAll("<h2>.*?</h2>", "<h2>"+brand+" "+ title +"</h2>");
				// 将当前的标题标签删除
				pageData = pageData.replaceAll("<h3>.*?</h3>", "");
			}
		}
		return pageData;
	}
}
