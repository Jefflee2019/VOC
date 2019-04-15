package com.bfd.parse.reprocess;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public interface ReProcessor extends ReProcessCode {

	/* 扩展点id */
	public static final String X_POINT_ID = ReProcessor.class.getName();

	/**
	 * 解析后处理
	 * 
	 * @param taskdata
	 * @param dataList
	 * @return
	 */
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace);
}
