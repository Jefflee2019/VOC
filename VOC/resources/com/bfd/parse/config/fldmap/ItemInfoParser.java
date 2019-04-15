package com.bfd.parse.config.fldmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;

public class ItemInfoParser {
	private static final Log LOG = LogFactory.getLog(ItemInfoParser.class);

	private static final Map<String, String> dftRule = new HashMap<String, String>();

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
		// dftRule.put("onsale", "onshelves");

		dftRequiredFldSet.add("id");
		dftRequiredFldSet.add("name");
		dftRequiredFldSet.add("price");
		dftRequiredFldSet.add("image_link");
		dftRequiredFldSet.add("category");
	}

	private static final Pattern numberRegex = Pattern.compile("^[0-9].*?");
	private static final Pattern regex = Pattern.compile("(?:BFD\\.)?BFD_(?:ITEM_)?INFO\\s*=\\s*\\{(.*?)\\};",
			Pattern.DOTALL);

	public static boolean getBfdItemInfo(ParseUnit unit, Map<String, Object> result) {
		return getBfdItemInfo(unit.getPageData(), unit.getCid(), unit.getUrl(), null, result, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, Map<String, Object> result) {
		return getBfdItemInfo(data, cid, url, null, result, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			Map<String, Object> result) {
		return getBfdItemInfo(data, cid, url, normalizer, result, null);
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			Map<String, Object> result, StringBuilder jsRes) {
		if (result == null)
			result = new HashMap<String, Object>();
		Matcher matcher = regex.matcher(data);
		if (matcher.find()) {
			String string = matcher.group(1);
			if (jsRes != null) {
				jsRes.append(string);
			}
			Map<String, Object> itemInfo = getItemInfo(string);
			BfdItemFldMapRule rule = ItemFldMapConfig.getInstance().getFldMapRule(cid);
			itemInfo.putAll(getItemInfoByRegex(data, rule));
			if (rule != null)
				// formatItemFlds(itemInfo, rule.getFormatRules());
				if (rule != null)
					if (mappingItemFlds(cid, rule, itemInfo, result)) {
						return true;
					}
			return false;
		}
		LOG.warn("not found");
		return false;
	}

	public static String getBfdItemJs(String data) {
		Matcher matcher = regex.matcher(data);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static boolean getBfdItemInfo(String data, String cid, String url, URLNormalizerClient normalizer,
			BfdItemFldMapRule rule, Map<String, Object> result, StringBuilder jsRes) {
		if (result == null)
			result = new HashMap<String, Object>();
		Matcher matcher = regex.matcher(data);
		if (matcher.find()) {
			String string = matcher.group(1);
			if (jsRes != null) {
				jsRes.append(string);
			}
			Map<String, Object> itemInfo = getItemInfo(string);
			if (rule == null)
				rule = ItemFldMapConfig.getInstance().getFldMapRule(cid);
			if (rule != null)
				itemInfo.putAll(getItemInfoByRegex(data, rule));
			if (rule != null)
				// formatItemFlds(itemInfo, rule.getFormatRules());
			if (rule != null)
				if (mappingItemFlds(cid, rule, itemInfo, result)) {
					return true;
				}
			return false;
		}
		LOG.warn("not found");
		return false;
	}

	private static void formatItemFlds(Map<String, Object> itemInfo, Map<String, String> formatRules) {
		if (formatRules != null && formatRules.size() > 0) {
			// ItemFldFormator.format(itemInfo, formatRules);
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
					String group = matcher.group(i + 1);
					if (group != null)
						list.add(matcher.group(i + 1));
				}
			}
			tmp.put(r.getKey(), list);
		}
		// Map<String, String> fieldsType = rule.getFieldsType();
		// for (Entry<String, Object> entry : tmp.entrySet()) {
		// String key = fieldsType.get(entry.getKey());
		// if ("string_array".equals(key)) {
		// result.put(key, entry.getValue());
		// }
		// }
		// return result;
		return tmp;
	}

	private static boolean mappingItemFlds(String cid, BfdItemFldMapRule rule, Map<String, Object> itemInfo,
			Map<String, Object> result) {
		List<String> requiredFields = rule.getRequiredFields();
		Map<String, String> directFields = rule.getDirectFields();
		if (!itemInfo.keySet().containsAll(requiredFields))
			return false;
		for (Entry<String, Object> entry : itemInfo.entrySet()) {
			String key = directFields.get(entry.getKey());
			if (!StringUtils.isEmpty(key)) {
				if ("onshelves".equalsIgnoreCase(key)) {
					if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue() == false) {
						result.put("onshelves", "off");
						result.put("offreason", "tagoff");
					}
				} else
					result.put(key, entry.getValue());
			}
		}
		return true;
	}

	public static Map<String, Object> getItemInfo(String string) {
		//TODO:页面没有bfd代码
		if(string==null||string.trim().length()==0){
			return new HashMap<String, Object>();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		int posCode = 0;
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		char curChar = 0;
		char befChar = 0;
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if ('\n' == ch) {
				continue;
			}
			if (posCode == 0 && ch == ':') {
				posCode = 1;
				continue;
			}
			if (posCode == 0) {
				if (',' == ch) {
					befChar = ch;
					continue;
				}
				if ('/' == ch) {
					befChar = ch;
					int ii = string.substring(i).indexOf('\n');
					i += ii;
					befChar = 0;
					continue;
				}
				if (']' == ch) {
					i += 1;
					befChar = 0;
					continue;
				}
				key.append(ch);
				befChar = ch;
			}
			if (posCode == 1) {
				if (' ' == ch) {
					continue;
				}
				if ('\"' == ch || '\'' == ch) {
					curChar = ch;
					posCode = 3; //
				} else if ('[' == ch) {
					posCode = 4;
				} else if ('{' == ch) {
					posCode = 5;
				} else {
					posCode = 2;
					i--;
				}
				continue;
			}
			if (posCode == 2) {
				if (',' == ch) {
					posCode = 0;
					String val = value.toString().trim();
					if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
						result.put(key.toString().trim(), Boolean.valueOf(val));
					} else if (numberRegex.matcher(val).find()) {
						if (val.contains(".")){
							try {
								result.put(key.toString().trim(), Double.valueOf(val) + "");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else{
							try {
								result.put(key.toString().trim(), Long.valueOf(val) + "");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
							
					} else {
						LOG.info("Invalid data, key=" + key.toString().trim() + ", value=" + val);
					}
					key = new StringBuilder();
					value = new StringBuilder();
					posCode = 0;
					befChar = 0;
					continue;
				} else {
					value.append(ch);
				}
			}
			if (posCode == 3) {
				if (ch == curChar && befChar == '\\') {
					value.setCharAt(value.length() - 1, ch);
					befChar = ch;
				} else if (ch == curChar) {
					result.put(key.toString().trim(), value.toString());
					key = new StringBuilder();
					value = new StringBuilder();
					posCode = 0;
					befChar = 0;
				} else {
					value.append(ch);
					befChar = ch;
				}
			}
			if (posCode == 4) {
				String subStr = string.substring(i);
				int idx = subStr.indexOf(']');
				subStr = subStr.substring(0, idx - 1);
				result.put(key.toString().trim(), getListValue(subStr));
				key = new StringBuilder();
				value = new StringBuilder();
				befChar = 0;
				posCode = 0;
				i += idx;
			}
			if (posCode == 5) {
				String substr = string.substring(i);
				int idx = substr.indexOf('}');
				substr = substr.substring(0, idx);
				result.put(key.toString().trim(), getItemInfo(substr));
				key = new StringBuilder();
				value = new StringBuilder();
				posCode = 0;
				befChar = 0;
				i += idx;
			}
		}
		return result;
	}

	public static List<Object> getListValue(String string) {
		if(string==null||string.trim().length()==0){
			return new ArrayList<Object>();
		}
		List<Object> result = new ArrayList<Object>();
		string = string.trim();
		if (string.startsWith("[")) {
			string = string.substring(1);
		}
		char c = string.charAt(0);
		if ('\"' != c && '\'' != c) {
			String[] array = string.split(",");
			result.addAll(Arrays.asList(array));
		} else {
			String[] array = string.split(c + "," + c);
			for (int i = 0; i < array.length; i++) {
				if (i == 0) {
					result.add(array[i].substring(1).trim());
				} else {
					result.add(array[i]);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		Map<String, List<Map>> map = new HashMap<String, List<Map>>();
		List<Map> rules = new ArrayList<Map>();
		Map<String, String> rule = new HashMap<String, String>();
		rule.put("city=='全国'", "aera='全国'");
		Map<String, String> rule2 = new HashMap<String, String>();
		rule2.put("cate[0]=='(男装)|(女装)'", "cate[add]='服装服饰'");
		Map<String, String> rule3 = new HashMap<String, String>();
		rule3.put("cate[0]=='休闲零食'", "cate[add:0]='休闲食品'");
		Map<String, String> rule4 = new HashMap<String, String>();
		rule4.put("", "region[add]=area");
		rules.add(rule);
		rules.add(rule2);
		rules.add(rule3);
		rules.add(rule4);
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("city", "全国");
		res.put("aera", "ss");
		handleFormatData(res, rule);

		Map<String, Object> res2 = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		list.add("女装");
		res2.put("cate", list);
		handleFormatData(res2, rule2);
	}

	private static void handleFormatData(Map<String, Object> res, Map<String, String> rule) {
		for (Entry<String, String> rr : rule.entrySet()) {
			String[] ff = rr.getKey().split("==");
			String fld;
			boolean ok = false;
			String re = ff[1].substring(1, ff[1].length() - 1);
			if (ff[0].trim().endsWith("]")) {
				int idx = 0;
				String[] kk = ff[0].split("\\[");
				fld = kk[0];
				idx = Integer.valueOf(kk[1].split("\\]")[0]);
				List list = (List) res.get(fld);
				ok = ((String) list.get(idx)).matches(re);
			} else {
				ok = ((String) res.get(ff[0].trim())).matches(re);
			}

			if (ok) {
				String[] v_ff = rr.getValue().split("=");
				String v_fld;
				int v_idx = 0;
				String vv = v_ff[1];
				vv = vv.substring(1, vv.length() - 1);
				if (ff[0].trim().endsWith("]")) {
					int idx = 0;
					String[] kk = v_ff[0].split("\\[");
					v_fld = kk[0];
					List list = (List) res.get(v_fld);
					if (list == null) {
						continue;
					}
					String[] v_oo;
					if (kk[1].contains(":")) {
						v_oo = kk[1].split(":");
					} else {
						v_oo = kk[1].split("\\]");
					}
					if (v_oo[0].equalsIgnoreCase("add")) {
						if (v_oo.length == 2) {
							idx = Integer.valueOf(v_oo[1].split("\\]")[0]);
						} else {
							idx = -1;
						}

						if (idx != -1) {
							List newlist = new ArrayList();
							for (int i = 0; i < list.size(); i++) {
								if (idx == i) {
									newlist.add(vv);
								}
								newlist.add(list.get(i));
							}
							res.put(v_fld, newlist);
						} else {
							list.add(vv);
							res.put(v_fld, list);
						}
					}
				} else {
					v_fld = v_ff[0];
					res.put(v_fld, vv.substring(1, vv.length() - 1));
				}
			}
		}
	}
}
