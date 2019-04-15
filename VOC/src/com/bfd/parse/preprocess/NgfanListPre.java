package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：机锋网(新闻)
 * 
 * 主要功能: 对于下一页链接，手工添加class="nextpage"属性
 * 
 * @author bfd_03
 *
 */
public class NgfanListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostPre.class);

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
	 * 给<a>标签添加class='nextpage' <a href=
	 * '/article?q=%E5%8D%8E%E4%B8%BA%E6%89%8B%E6%9C%BA&template=&channelId=0&f=9001&ie=utf-8&st=20'>下一页</a
	 * >
	 * 
	 * @param pageData
	 * @return
	 */
	public String parsePageData(String pageData) {
		int index = -1;
		if ((index = pageData.indexOf(">下一页</a>")) >= 0) {
			pageData = pageData.substring(0, index) + " class='nextpage'" + pageData.substring(index);
		}
		return pageData;
	}

}
