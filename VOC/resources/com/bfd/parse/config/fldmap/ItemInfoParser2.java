package com.bfd.parse.config.fldmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
//import com.prnasia.mediawatch.common.utilities.MyCrawler;

public class ItemInfoParser2 {
	private static final Log LOG = LogFactory.getLog(ItemInfoParser2.class);

	private static final Map<String, String> dftRule = new HashMap<String, String>();
	
	private static final Pattern checkColonPattern = Pattern.compile(":(?!\\/)");

	private static final Set<String> dftRequiredFldSet = new HashSet<String>();
	static {
		dftRule.put("id", "iid");
		dftRule.put("name", "name");
		dftRule.put("image_link", "large_img");
		dftRule.put("price", "price");
		dftRule.put("market_price", "market_price");
		dftRule.put("category", "cate");
		dftRule.put("city", "location");
		dftRule.put("region", "district");
		dftRule.put("del_time", "del_time");
		dftRule.put("sales_num", "sales_num");
		dftRule.put("discount", "discount");
		dftRule.put("longitude", "longitude");
		dftRule.put("latitude", "latitude");

		dftRequiredFldSet.add("id");
		dftRequiredFldSet.add("name");
		dftRequiredFldSet.add("price");
		dftRequiredFldSet.add("image_link");
		dftRequiredFldSet.add("category");
	}

	private static final Pattern NUMBER_REGEX = Pattern.compile("^[0-9].*?");
	private static final Pattern BFD_ITEM_REGEX = Pattern.compile("(?:BFD\\.)?BFD_(?:ITEM_)?INFO\\s*=\\s*\\{(.*?)\\};",
			Pattern.DOTALL);

	public static boolean getBfdItemInfo(ParseUnit unit, Map<String, Object> result) {
		return getBfdItemInfo(unit.getPageData(), unit.getCid(), unit.getUrl(), null, result, null, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, Map<String, Object> result) {
		return getBfdItemInfo(data, cid, url, null, result, null, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			Map<String, Object> result) {
		return getBfdItemInfo(data, cid, url, normalizer, result, null, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			Map<String, Object> result, StringBuilder jsRes, StringBuilder errMsg) {
		BfdItemFldMapRule rule = ItemFldMapConfig.getInstance().getFldMapRule(cid);
		return getBfdItemInfo(data, cid, url, normalizer, rule, result, jsRes, errMsg);
	}

	public static String getBfdItemJs(String data) {
		Matcher matcher = BFD_ITEM_REGEX.matcher(data);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			BfdItemFldMapRule rule, Map<String, Object> result, StringBuilder jsRes, StringBuilder errMsg) {
		if (result == null)
			result = new HashMap<String, Object>();
		if (rule == null) {
			return false;
		}
		Matcher matcher = BFD_ITEM_REGEX.matcher(data);
		if (matcher.find()) {
			String string = matcher.group(1);
			if (jsRes != null) {
				jsRes.append(string);
			}
			Map<String, Object> itemInfo = getItemInfoFromBfdJs(string);
//			LOG.info("url is "+url+".after getItemInfo::"+JsonUtil.toJSONString(itemInfo));
			itemInfo.putAll(getItemInfoByRegex(data, rule));
//			LOG.info("url is "+url+".after getItemInfoByRegex::"+JsonUtil.toJSONString(itemInfo));
			itemInfo = keyNormalize(itemInfo);
			if (mappingItemFlds(cid, rule, itemInfo, result)) {
//				LOG.info("url is "+url+".after mappingItemFlds::"+JsonUtil.toJSONString(result));
				formatItemFlds(result, rule.getFormatRules());
//				LOG.info("url is "+url+".after formatItemFlds::"+JsonUtil.toJSONString(result));
//				LOG.info("url is "+url+".cid=" + cid + "url=" + url + ", bfditemInfo=" + JsonUtil.toJSONString(result));
				return true;
			} else {
				pushErrMsg(errMsg, "解析字段没有完全包括必选字段！");
			}
		} else {
			pushErrMsg(errMsg, "页面上没有发现bfd实施代码块");
		}
		return false;
	}

	private static Map<String, Object> keyNormalize(Map<String, Object> itemInfo){
//		LOG.info("before keyNormalize:"+JsonUtil.toJSONString(itemInfo));
		Map<String, Object> rs = new HashMap<String, Object>();
		Iterator<Entry<String, Object>>it = itemInfo.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Object> entry = it.next();
			rs.put(entry.getKey().replaceAll("\"", "").replaceAll("'", ""), entry.getValue());
			
		}
		LOG.info("after keyNormalize:"+JsonUtil.toJSONString(rs));
		return rs;
	}
	
	private static void pushErrMsg(StringBuilder errMsg, String message) {
		if (errMsg != null) {
			errMsg.append(message);
		}
	}

	private static void formatItemFlds(Map<String, Object> result, Map<String, Object> formatRules) {
		if (formatRules != null && formatRules.size() > 0) {
			ItemFldFormator.format(result, formatRules);
		}
	}

	private static Map<String, Object> getItemInfoByRegex(String data, BfdItemFldMapRule rule) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (rule == null) {
			return result;
		}
		Map<String, Object> tmp = new HashMap<String, Object>();
		Map<String, Pattern> fieldsRegex = rule.getFieldsPattern();
		for (Entry<String, Pattern> r : fieldsRegex.entrySet()) {
			Pattern pattern = r.getValue();
			Matcher matcher = pattern.matcher(data);
			List<String> list = new ArrayList<String>();
			while (matcher.find()) {
				for (int i = 0; i < matcher.groupCount(); i++) {
					list.add(matcher.group(i + 1));
				}
			}
			tmp.put(r.getKey(), list);
		}
		return tmp;
	}

	private static boolean mappingItemFlds(String cid, BfdItemFldMapRule rule, Map<String, Object> itemInfo,
			Map<String, Object> result) {
		List<String> requiredFields = rule.getRequiredFields();
		Map<String, String> directFields = rule.getDirectFields();
//		LOG.info("mappingItemFlds::"+JsonUtil.toJSONString(directFields));
		for (Entry<String, String> entry : directFields.entrySet()) {
			Object itemValue = itemInfo.get(entry.getValue());
			if (itemValue != null) {
				if ("onshelves".equalsIgnoreCase(entry.getKey())) {
					if (itemValue instanceof Boolean && (Boolean) itemValue == false) {
						result.put("onshelves", "off");
						result.put("offreason", "tagoff");
					}
				} else {
					result.put(entry.getKey(), itemValue);
				}
			}
		}
		if (!result.keySet().containsAll(requiredFields)) {
			LOG.warn("required Fields, failed");
			return false;
		}
		return true;
	}

//	public static Map<String, Object> getItemInfo(String string) {
//		Map<String, Object> result = new HashMap<String, Object>();
//		int posCode = 0;
//		StringBuilder key = new StringBuilder();
//		StringBuilder value = new StringBuilder();
//		char curChar = 0;
//		char befChar = 0;
//		for (int i = 0; i < string.length(); i++) {
//			char ch = string.charAt(i);
//			if ('\n' == ch) {
//				continue;
//			}
//			if (posCode == 0 && ch == ':') {
//				posCode = 1;
//				continue;
//			}
//			if (posCode == 0) {
//				if (',' == ch) {
//					befChar = ch;
//					continue;
//				}
//				if ('/' == ch) {
//					befChar = ch;
//					int ii = string.substring(i).indexOf('\n');
//					i += ii;
//					befChar = 0;
//					continue;
//				}
//				if (']' == ch) {
//					i += 1;
//					befChar = 0;
//					continue;
//				}
//				key.append(ch);
//				befChar = ch;
//			}
//			if (posCode == 1) {
//				if (' ' == ch) {
//					continue;
//				}
//				if ('\"' == ch || '\'' == ch) {
//					curChar = ch;
//					posCode = 3; //
//				} else if ('[' == ch) {
//					posCode = 4;
//				} else if ('{' == ch) {
//					posCode = 5;
//				} else {
//					posCode = 2;
//					i--;
//				}
//				continue;
//			}
//			if (posCode == 2) {
//				if (',' == ch) {
//					posCode = 0;
//					String val = value.toString().trim();
//					if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
//						result.put(key.toString().trim(), Boolean.valueOf(val));
//					} else if (NUMBER_REGEX.matcher(val).find()) {
//						if (val.contains(".")){
//							try {
//								result.put(key.toString().trim(), Double.valueOf(val) + "");
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}else{
//							try {
//								result.put(key.toString().trim(), Long.valueOf(val) + "");
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//							
//					} else {
//						LOG.info("Invalid data, key=" + key.toString().trim() + ", value=" + val);
//					}
//					key = new StringBuilder();
//					value = new StringBuilder();
//					posCode = 0;
//					befChar = 0;
//					continue;
//				} else {
//					value.append(ch);
//				}
//			}
//			if (posCode == 3) {
//				if (ch == curChar && befChar == '\\') {
//					value.setCharAt(value.length() - 1, ch);
//					befChar = ch;
//				} else if (ch == curChar) {
//					result.put(key.toString().trim(), value.toString());
//					key = new StringBuilder();
//					value = new StringBuilder();
//					posCode = 0;
//					befChar = 0;
//				} else {
//					value.append(ch);
//					befChar = ch;
//				}
//			}
//			if (posCode == 4) {
//				String subStr = string.substring(i - 1);
//				int idx = 1;
//				if (subStr.trim().startsWith("[")) {
//					if (subStr.substring(1).trim().startsWith("[")) {
//						idx = subStr.indexOf("]]") + 2;
//					} else {
//						idx = subStr.indexOf("]") + 1;
//					}
//				}
//				subStr = subStr.substring(0, idx);
//				List<Object> listValue = getListValue(subStr);
//				if (listValue.size() > 0) {
//					result.put(key.toString().trim(), listValue);
//				}
//				key = new StringBuilder();
//				value = new StringBuilder();
//				befChar = 0;
//				posCode = 0;
//				i += idx;
//			}
//			if (posCode == 5) {
//				String substr = string.substring(i);
//				int idx = substr.indexOf('}');
//				substr = substr.substring(0, idx);
//				result.put(key.toString().trim().replace("\"", "").replace("'", ""), getItemInfo(substr));
//				key = new StringBuilder();
//				value = new StringBuilder();
//				posCode = 0;
//				befChar = 0;
//				i += idx;
//			}
//		}
//		return result;
//	}

	public static List<Object> getListValue(String string) {
		List<Object> result = new ArrayList<Object>();
		try {
			List list = JsonUtil.parseArray(string);
			result.addAll(list);
		} catch (Exception e) {
			LOG.warn("get list value exception, ", e);
		}
		return result;
	}
	//TODO:
	public static Map<String , Object> getItemInfoFromBfdJs(String bfdJs){
		//去除多行注释
		bfdJs = bfdJs.replaceAll("\\/\\*[\\s\\S]*?\\*\\/", "");
		//去除单行注释
		bfdJs = bfdJs.replaceAll("[\\s,\"]\\/\\/.*", "");
		String[] strArray = bfdJs.split("[\r\n]+");
		Map<String, String> tempRs = new HashMap<String, String>();
		LOG.debug("strArray is :"+JsonUtil.toJSONString(strArray));
		String lastKey = "";
		String tempKey = "";
		for(int index=0;index<strArray.length;index++){
			if(strArray[index]==null||strArray[index].trim().length()==0){
				continue;
			}
//			if(strArray[index].indexOf(":")>0){
			if(checkColonPattern.matcher(strArray[index]).find()){
//				tempArray = strArray[index].split(": ");
				tempKey = strArray[index].substring(0, strArray[index].indexOf(":")).trim();
				tempRs.put(tempKey, strArray[index].substring(strArray[index].indexOf(":")+1));
				lastKey = tempKey;
			}else{
				tempRs.put(lastKey, tempRs.get(lastKey)+strArray[index]);
			}
		}
		LOG.debug("tempRs is :"+JsonUtil.toJSONString(tempRs));
		Map<String , Object> rs = cleanAndParse(tempRs);
		LOG.debug("rs is :"+JsonUtil.toJSONString(rs));
		return rs;
	}
	private static Map<String, Object> cleanAndParse(Map<String, String> tempRs){
		Map<String, Object> rs = new HashMap<String, Object>();
		Iterator<Entry<String, String>> it = tempRs.entrySet().iterator();
		String tempStr = "";
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			tempStr = entry.getValue().trim();
//			tempStr = entry.getValue().replaceAll("\"", "").replaceAll("'", "");
			if(tempStr.lastIndexOf(",")>=0&&tempStr.lastIndexOf(",")==tempStr.length()-1){
				tempStr = tempStr.substring(0, tempStr.lastIndexOf(","));
			}
			if(tempStr.indexOf("[")<0){
				tempStr = tempStr.replaceAll("\"", "");
			}
			tempStr = tempStr.replaceAll("\\t", "");
			try {
//				if(tempStr)
				rs.put(entry.getKey()
						.replaceAll("\"", "")
						.replaceAll("'", "")
						.trim(), 
						tempStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rs;
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://www.aipintuan.com/team/32839.html";
//		String url = "http://t.58.com/bj/90132855999493013/?linkid=bj_liebiao_xxyl_1&PGTID=14042731250820.8877865203232088&ClickID=1";
//		String url = "http://i.midea.com/item/105.html";
//		String url = "http://t.55bbs.com/pin/2348262/";
//		String url = "http://www.sheinside.com/Grey-Draped-Asymmetric-Hem-Skirt-p-173776-cat-1732.html";
//		String url = "http://www.sheinside.com/Blue-V-Neck-Long-Sleeve-Rivet-Dipped-Hem-Blouse-p-165891-cat-1733.html";
//		String url = "http://www.111.com.cn/news/lister/15.html";
//		String url = "http://www.mbaobao.com/item/1501008602";
//		String url = "http://www.tiantian.com/cosmetic/a008966.html";
//		String url = "http://fangjia.cric.com/shanghai/jiading/xibeiqu/102452_pricetable";
		
//		String url = "http://www.zhiwo.com/product/1103052.html";
//		String data = new MyCrawler().get(url);
////		
//		Matcher matcher = BFD_ITEM_REGEX.matcher(data);
//		if (matcher.find()) {
//			String string = matcher.group(1);
////			Map<String, Object> itemInfo = getItemInfo(string);
//			Map<String, Object> itemInfo = getItemInfoFromBfdJs(string);
//		}
		
//		String comment = "afd/*dfjkdjfkdfjk*/erere";
//		String regex = "\\/\\*[\\s\\S]*?\\*\\/";
//		String rs = comment.replaceAll(regex, "");
//		Map map = new HashMap();
//		map.put("1", "1");

	}
}
