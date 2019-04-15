package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site CCTV-新闻(Ncctv)
 * @function 下一页正常翻页异常
 * 
 * @author bfd_02
 *
 */
public class NcctvListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NcctvListPre.class);

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
		 * 解决翻页正常问题，下一页<a>添加一个class属性与其他列表属性相同，翻页时索引会移动 
		 * eg: <a href=
		 * "search.php?qtext=%E5%8D%8E%E4%B8%BA+%E6%89%8B%E6%9C%BA&type=web&page=2&datepid=1&vtime=-1&sort=relevance"
		 * class="btn_page" ><span>下一页>></span></a>
		 */
		// 将源码中的所有class="btn_page"替换成""
		if (pageData.contains("class=\"btn_page\"")) {
			pageData.replace("class=\"btn_page\"", "");
		}
		// 在源码中添加class=nextpage
		if (pageData.contains("><span>下一页>></span><")) {
			int index = pageData.indexOf("><span>下一页>></span><");
			pageData = pageData.substring(0, index) + " class='nextpage'"
					+ pageData.substring(index);
//			System.out.println(pageData);
		}
		return pageData;
	}
}
