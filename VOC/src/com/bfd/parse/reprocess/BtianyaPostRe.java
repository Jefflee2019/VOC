package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：天涯
 * 
 * 功能：标准化帖子页或者问答页并转换问答页时间格式
 * 
 * @author bfd_06
 */
public class BtianyaPostRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String subUrl = urlFilter(unit.getUrl()); // 去掉 http https 后的链接
		/* 标准化问答页 */
		if (subUrl.startsWith("wenda")) {
			// NEWSTIME
			formatStr2(resultData, resultData.get(Constants.NEWSTIME)
					.toString(), Constants.NEWSTIME);
			// REPLYCOUNT
			formatStr2(resultData, resultData.get(Constants.REPLYCOUNT)
					.toString(), Constants.REPLYCOUNT);
			if (resultData.containsKey(Constants.REPLYS))
				formatStr2(resultData, resultData.get(Constants.REPLYS)
						.toString(), Constants.REPLYS); // REPLYS
			/* 标准化帖子页 */
		} else {
			// CATE
			formatStr(resultData, resultData.get(Constants.CATE).toString(),
					Constants.CATE);
			// VIEWS
			formatStr(resultData, resultData.get(Constants.VIEWS).toString(),
					Constants.VIEWS);
			// REPLYCOUNT
			formatStr(resultData, resultData.get(Constants.REPLYCOUNT)
					.toString(), Constants.REPLYCOUNT);
			// NEWSTIME
			formatStr(resultData,
					resultData.get(Constants.NEWSTIME).toString(),
					Constants.NEWSTIME);
			// REPLYS
			formatStr(resultData, resultData.get(Constants.REPLYS).toString(),
					Constants.REPLYS);
		}

		return new ReProcessResult(SUCCESS, processdata);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void formatStr(Map<String, Object> resultData, String str,
			String keyName) {
		switch (keyName) {
		case Constants.CATE:
			List<String> rCate = new ArrayList<String>();
			String[] strArray = str.split(" > ");
			rCate.add(strArray[0].substring(1));
			int indexE0 = strArray[1].indexOf("[");
			if (indexE0 != -1) {
				rCate.add(strArray[1].substring(0, strArray[1].indexOf("[") - 1));
			} else {
				rCate.add(strArray[1]);
			}
			resultData.put(keyName, rCate);
			break;
		case Constants.VIEWS:
			int indexE1 = str.indexOf("：");
			resultData.put(keyName, str.substring(indexE1 + 1));
			break;
		case Constants.REPLYCOUNT:
			int indexE2 = str.indexOf("：");
			resultData.put(keyName, str.substring(indexE2 + 1));
			break;
		case Constants.NEWSTIME:
			int indexE3 = str.indexOf("：");
			resultData.put(keyName, str.substring(indexE3 + 1));
			break;
		case Constants.REPLYS:
			List listReplys = (ArrayList) (resultData.get(Constants.REPLYS));
			List resultList = new ArrayList();
			for (int i = 0; i < listReplys.size(); i++) {
				if (listReplys.get(i) instanceof Map) {
					Map mapReplys = (HashMap) listReplys.get(i);
					String dataStr = (String) mapReplys
							.get(Constants.REPLYDATE);
					dataStr = dataStr.substring(dataStr.indexOf("：") + 1);
					mapReplys.put(Constants.REPLYDATE, dataStr);
					// deal with replyfloor
					if (mapReplys.containsKey(Constants.REPLYFLOOR)) {
						String replyfoor = mapReplys.get(Constants.REPLYFLOOR)
								.toString();
						replyfoor = replyfoor.replace("楼", "");
						mapReplys.put(Constants.REPLYFLOOR, replyfoor);
					}
					resultList.add(mapReplys);
				}
			}
			resultData.put(Constants.REPLYS, resultList);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public void formatStr2(Map<String, Object> resultData, String value,
			String keyName) {
		if (keyName.equals(Constants.NEWSTIME)) {
			String time = formatDate(value);
			resultData.put(keyName, time);
		} else if (keyName.equals(Constants.REPLYCOUNT)) {
			resultData.put(keyName, value.substring(0, value.indexOf("个")));
		} else if (keyName.equals(Constants.REPLYS)) {
			List<Map<String, Object>> listReplys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < listReplys.size(); i++) {
				if (listReplys.get(i) instanceof Map) {
					Map<String, Object> mapReplys = (Map<String, Object>) listReplys
							.get(i);
					String dataStr = formatDate((String) mapReplys
							.get(Constants.REPLYDATE));
					mapReplys.put(Constants.REPLYDATE, dataStr);
					// deal with replyfloor
					if (mapReplys.containsKey(Constants.REPLYFLOOR)) {
						String replyfoor = mapReplys.get(Constants.REPLYFLOOR)
								.toString();
						replyfoor = replyfoor.replace("楼", "");
						mapReplys.put(Constants.REPLYFLOOR, replyfoor);
					}
					resultList.add(mapReplys);
				}
			}
			resultData.put(Constants.REPLYS, resultList);
		}
	}

	public String formatDate(String value) {
		String time = match("\\d+-+\\d+-\\d+", value);
		if (time == null)
			return ConstantFunc.convertTime(value);
		
		return "20" + time;
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group();
		}

		return null;
	}

	/**
	 * 过滤 http:// https://
	 */
	public String urlFilter(String url) {
		if (url.startsWith("http://")) {
			return url.substring(7);
		} else {
			return url.substring(8);
		}
	}

}
