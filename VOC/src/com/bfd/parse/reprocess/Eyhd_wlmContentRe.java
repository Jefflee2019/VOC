package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：一号店
 * 
 * 主要功能： 1、处理商品路径 2、参数中提取品牌
 * 
 * @author lth
 *
 */
public class Eyhd_wlmContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 品牌
		if (resultData.containsKey("parameter")) {
			String parameter = resultData.get("parameter").toString();
			String regex = "品牌：\\s*(\\S*)";
			Matcher match = Pattern.compile(regex).matcher(parameter);
			if (match.find()) {
				String brand = match.group(1);
				resultData.put("brand", brand);
			}
		}

		/**
		 * @function:处理cate,将cate分成目录、品牌、产品三部分,并且品牌只取第一个 
		 * 处理前：                                             
		 * "cate":[ "美容护理、洗发、沐浴 美发护发/假发 洗护发 洗发水 吕 吕 花源润活洗发乳 400g/瓶 韩国进口"] 
		 * 处理后：                                              
		 * "cate":[ "美容护理、洗发、沐浴$美发护发/假发$洗护发$洗发水","吕","吕 花源润活洗发乳 400g/瓶 韩国进口"]                                             
		 *                                               
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			ArrayList<String> newcateList = new ArrayList<String>();
			if (cateList != null && !cateList.isEmpty()) {
				String cate = cateList.get(0);
				if (cate.contains("")) {
					String[] cateArr = cate.split("");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < cateArr.length - 2; i++) {
						if (cateArr[i].equals("")) {
							break;
						} else {
							sb.append(cateArr[i].trim()).append("$");
						}
					}
					newcateList.add(sb.toString().substring(0, sb.length()-1));
					String brand = cateArr[cateArr.length - 2];
					// 如果有多个品牌，只取1个
					if (brand.equals(" ")) {
						String[] brandarr = brand.split(" ");
						if (brandarr.length > 1) {
							brand = brandarr[0];
						}
					}
					
					newcateList.add(brand.trim());
					newcateList.add(cateArr[cateArr.length - 1].trim());
				}
				resultData.put(Constants.CATE, newcateList);
			}
			resultData.put(Constants.CATE, newcateList);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
