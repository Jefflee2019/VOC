package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Ejd
 * 
 * 主要功能: 对于图片链接，手工添加http:头
 * 
 * @author bfd_03
 *
 */
public class EjdContentPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(EjdContentPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData);

		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**
	 * 给链接添加http: src=
	 * '//img12.360buyimg.com/n5/jfs/t1213/266/831015679/132692/53bad208/554c7526N8e6544ba.jpg
	 * '
	 * 
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		Pattern patten = Pattern.compile("//img\\d+\\.360buyimg\\.com[/\\w]+\\.jpg",
		// img12\\.360buyimg\\.com/n5[/\\w]+\\.jpg
				Pattern.DOTALL);
		Matcher matcher = patten.matcher(pageData);
		while (matcher.find()) {
			pageData = pageData.replace(matcher.group(), "http:" + matcher.group());
		}
		return pageData;
	}

}
