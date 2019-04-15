package com.bfd.parse.preprocess;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public interface PreProcessor {

	/* 扩展点id */
	public static final String X_POINT_ID = PreProcessor.class.getName();

	/**
	 * 数据预处理
	 * 
	 * @param taskdata
	 * @param dataList
	 * 
	 */
	public boolean process(ParseUnit unit, ParserFace parseFace);
}
