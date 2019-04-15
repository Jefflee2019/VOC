package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 网易手机/数码-论坛(Bmobile163)
 * @function 下一页添加class属性，解决翻页异常
 * @author bfd_02
 *
 */
public class Bmobile163ListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(Bmobile163ListPre.class);

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
		if (pageData.contains(">下一页 &gt;</a>")) {
			pageData = pageData.replace(">下一页 &gt;</a>",
					"class= \"nextpage\">下一页 &gt;</a>");
		}
		//处理title链接混乱问题
		if(pageData.contains("class=\"cate\"")) {
//	  [<a href="/boardset/mobile_4eps.html" class="cate">P970</a>]
			String regex = "\\[<a\\s*href=\"\\S*\"\\s*class=\"cate\">\\S*</a>\\]";
			pageData = pageData.replaceAll(regex, "");
		}
		return pageData;
	}

}
