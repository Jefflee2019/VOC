package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nsouthcn
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class NsouthcnContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// AUTHOR
		if (resultData.containsKey(Constants.AUTHOR)) {
			formatAttr(Constants.AUTHOR,
					(String) resultData.get(Constants.AUTHOR), resultData);
		}
		// SOURCE
		if (resultData.containsKey(Constants.SOURCE)) {
			formatAttr(Constants.SOURCE,
					(String) resultData.get(Constants.SOURCE), resultData);
		}
		// POST_TIME
		if (resultData.containsKey(Constants.POST_TIME)) {
			formatAttr(Constants.POST_TIME,
					(String) resultData.get(Constants.POST_TIME), resultData);
		}
		// CONTENT
		if (resultData.containsKey(Constants.CONTENT)) {
			formatAttr(Constants.CONTENT,
					(String) resultData.get(Constants.CONTENT), resultData);
		}
		// CATE
		if (resultData.containsKey(Constants.CATE)) {
			Object obj = resultData.get(Constants.CATE);
			if(obj instanceof String)
				formatAttr(Constants.CATE,
					(String) resultData.get(Constants.CATE), resultData);
		}
		// 处理内容页的下一页
		if (resultData.containsKey(Constants.NEXTPAGE)
				&& !matchTest(">下一页</a>", unit.getPageData())) {
			resultData.remove(Constants.NEXTPAGE);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			tasks.remove(0);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		if (keyName.equals(Constants.AUTHOR)) {
			value = value.replace("作者：", "").replace("记者：", "").replace("作者: ", "");
			if (value.equals(""))
				result.remove(keyName);
			else
				result.put(keyName, value);
		} else if (keyName.equals(Constants.SOURCE)) {
			int index = value.indexOf("来源");
			if (index != -1)
				value = value.substring(index + 3);
			int indexSpace = value.lastIndexOf(" ");
			if(indexSpace!=-1)
				value = value.substring(indexSpace + 1).replace(" ", "");
			result.put(keyName, value);
		} else if(keyName.equals(Constants.POST_TIME)){
			int index = value.indexOf("来源");
			if (index != -1) {
				value = value.substring(0,index - 1);
			}
			value = value.replace("北京娱乐信报", "");
			value = value.replace("南方网", "");
			value = value.replace("时间:", "");
			value = value.trim();
			result.put(keyName, value);
		} else if(keyName.equals(Constants.CONTENT)){
			if(value.startsWith("核心摘要："))
				value = value.substring(5);
			else if(value.startsWith("摘要："))
				value = value.substring(3);
			value = value.trim();
			result.put(keyName, value);
		} else if(keyName.equals(Constants.CATE)){
			String[] valueArray = value.split(">");
			List<String> valueList = new ArrayList<String>();
			for(String valueStr:valueArray){
				valueList.add(valueStr);
			}
			result.put(keyName, valueList);
		}
	}

	public Boolean matchTest(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return true;
		}

		return false;
	}

}
