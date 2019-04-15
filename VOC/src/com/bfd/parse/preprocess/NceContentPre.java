package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：Nce
 * 
 * 主要功能: 修复<html>标签
 * 
 * @author bfd_04
 *
 */
public class NceContentPre implements PreProcessor {

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		
		pageData.replace("<html\">", "<html>");
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {

		}
		unit.setPageEncode("utf8");
		return true;
	}

}
