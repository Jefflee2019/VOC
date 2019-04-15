package com.bfd.parse.json;

import java.util.List;
import java.util.Map;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;

public interface JsonParser {

	/* 扩展点id */
//	public static final String X_POINT_ID = JsonParser.class.getName();
	//为了混淆
	public static final String X_POINT_ID = "com.bfd.parse.json.JsonParser";

	/**
	 * 解析json数据
	 * 
	 * @param taskdata
	 * @param dataList
	 * @return JsonParserResult：data 解析结果 key,value; parsecode:解析失败为4， 成功为0;
	 * 
	 */
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient,ParseUnit unit);
}