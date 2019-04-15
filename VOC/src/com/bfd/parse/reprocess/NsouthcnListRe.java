package com.bfd.parse.reprocess;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nsouthcn
 * 
 * 添加列表页下一页 组合部分列表
 * 
 * @author bfd_06
 */
public class NsouthcnListRe implements ReProcessor {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		String pageUrl = unit.getUrl();
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		int processcode = 0;
		int pageNumberNow = matchPageNum("page=(\\d+)", pageUrl); // 当前页码
		int pageSize = 0;
		// 调整页面显示格式
		List<Map<String, Object>> item1 = (List<Map<String, Object>>) resultData
				.get("item1");
		List<Map<String, Object>> item2 = (List<Map<String, Object>>) resultData
				.get("item2");
		List<Map<String, Object>> item3 = (List<Map<String, Object>>) resultData
				.get("item3");
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
				.get("items");
		List<Map<String, Object>> showITEMS = new ArrayList();
		if (item1 != null && item1.size() > 0)
			showITEMS.add(item1.get(0));
		if (item2 != null && item2.size() > 0)
			showITEMS.add(item2.get(0));
		if (item3 != null && item3.size() > 0)
			showITEMS.add(item3.get(0));
		if (items != null && items.size() > 0)
			showITEMS.addAll(items);
		resultData.remove("item1");
		resultData.remove("item2");
		resultData.remove("item3");
		resultData.put("items", showITEMS);
		// 添加下一页
		pageSize += showITEMS.size();
		if (pageSize >= 10) {
			if (pageNumberNow == 0)
				addNextUrl(pageUrl.replace("search?", "search?page=2&"),
						resultData, unit, result);
			else
				addNextUrl(
						pageUrl.replace("page=" + pageNumberNow, "page="
								+ (pageNumberNow + 1)), resultData, unit,
						result);
			ParseUtils.getIid(unit, result);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String nextUrl, Map<String, Object> resultData,
			ParseUnit unit, ParseResult result) {
		List<Map> tasks = (List<Map>) resultData.get("tasks");
		Map<String, Object> task = new HashMap<String, Object>();
//		task.put("iid", DataUtil.calcMD5(nextUrl));
//		task.put("iid", StringToMD5(nextUrl));
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "newslist");
		resultData.put("nextpage", nextUrl);
		tasks.add(task);
	}
	
	private String StringToMD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
