package com.bfd.parse.preprocess;


import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 淘宝的列表页url：https://hwhonor.taobao.com/category-1054691780.htm?spm=a1z10.1-c.w10632080-12167043201.15.1XQ3mz&search=y&catName=%CA%D6%BB%FA%D7%A8%C7%F8#bd
 *         模板url：https://hwhonor.taobao.com/i/asynSearch.htm?_ksTS=1445840408953_240&callback=jsonp241&mid=w-12167043240-0&wid=12167043240&path=/category-1054691780.htm&spm=a1z10.1-c.w10632080-12167043201.15.1XQ3mz&search=y&catName=%CA%D6%BB%FA%D7%A8%C7%F8&catId=1054691780&scid=1054691780
 * @function 将模板url中的干扰字符去掉，方便模板标定
 * @author BFD_499
 *
 */
public class Etaobao_hwListPre implements PreProcessor{
	private static final Log LOG = LogFactory.getLog(Etaobao_hwListPre.class);
	@Override
	//将（\"）替换为（"），替换完成后走模板
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = pageData.replace("\\\"","\"");
		System.out.println(pageData);
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
		}
		unit.setPageEncode("utf8");
		return true;
	}
}
