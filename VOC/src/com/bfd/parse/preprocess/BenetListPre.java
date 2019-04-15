package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 硅谷动力-论坛(Benet)
 * @function 去掉列表页中结构不同的部分
 * @author bfd_02
 *
 */
public class BenetListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BenetListPre.class);

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
		 * @function 去掉 <div class = "bm bmw fl"></div>里面的内容
		 * @field "mn"和"pgt"分别是所要去掉的div前后的标签
		 * @param pattern.DoTall 匹配多行
		 * 
		 */
		Pattern patten = Pattern.compile("<div class=\"bm bmw fl\".*(?=<div id=\"pgt\")", Pattern.DOTALL);
		Matcher matcher = patten.matcher(pageData);

		if (matcher.find()) {
			pageData = matcher.replaceFirst("");
		}
		return pageData;
	}

}
