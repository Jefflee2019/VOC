package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：京东
 * 
 * 主要功能：添加商品属性到评论页中
 * 
 * @author bfd_03
 *
 */
public class EjdContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		//将商品名称放入到attr中，以便在评论页中得到
		Object attrObj = unit.getAttr();
		Map<String, Object> attrMap = new HashMap<String, Object>();
		if (attrObj != null && attrMap instanceof Map) {
			attrMap = (Map<String, Object>) attrObj;
		}
		if(resultData.containsKey(Constants.ITEMNAME)){
			attrMap.put(Constants.TITLE, resultData.get(Constants.ITEMNAME));
		}
		
		// 规格参数
		if (resultData.containsKey(Constants.PARAMETER)) {
			stringToMap(resultData, resultData.get(Constants.PARAMETER)
					.toString(), Constants.PARAMETER);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	public static void stringToMap(Map<String, Object> resultData, String str, String keyName) {
		if(keyName.equals(Constants.PARAMETER)){
			List<String> paraList = (List<String>) resultData.get(Constants.PARAMETER);
			for(int i=0;i < paraList.size();){
				if(paraList.get(i).equals("") || !paraList.get(i).contains(" ")){	
					paraList.remove(i);
					continue;
				}
				paraList.set(i, paraList.get(i).replaceFirst(" ",":"));
				i++;
			}		
			resultData.put(Constants.PARAMETER, paraList);
		}
		
	}
}
