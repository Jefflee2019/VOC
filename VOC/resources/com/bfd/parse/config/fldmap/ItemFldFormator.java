package com.bfd.parse.config.fldmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.util.JsonUtil;

public class ItemFldFormator {
	private static final Log LOG = LogFactory.getLog(ItemFldFormator.class);

	private static final Pattern regex1 = Pattern.compile("^([^\\s=]+?)(?:\\[(\\d+)\\])?=(?:'(.+?)')$");
	private static final Pattern regex2 = Pattern
			.compile("^([^\\s=]+?)(?:\\[(\\d+)\\])?=([^'\"\\s]*?)(?:\\[(\\d+)\\])?$");
	private static final Pattern formatRegex = Pattern
			.compile("^([^\\s=]+?)\\.((?:push)|(?:remove))\\(\\s*(-?\\d+)\\s*(?:,\\s*'(.+?)'\\s*)?\\)$");
	private static final Pattern formatRegex2 = Pattern
			.compile("^([^\\s=]+?)\\.((?:push)|(?:remove))\\(\\s*(-?\\d+)\\s*(?:,\\s*([^'\"\\s]+?)\\s*)?\\)$");

	private static boolean checkCondition(Map<String, Object> res, String condition) {
		if (res == null || StringUtils.isEmpty(condition)) {
			return false;
		}
		if ("true".equalsIgnoreCase(condition)) {
			return true;
		}
		Matcher matcher = regex1.matcher(condition);
		if (matcher.find()) {
			String left = matcher.group(1);
			String idx = matcher.group(2);
			String right = matcher.group(3);
			if (res.containsKey(left)) {
				String rVal = null;
				if (StringUtils.isNotEmpty(idx) && res.get(left) instanceof List) {
					List val = (List) res.get(left);
					rVal = String.valueOf(val.get(Integer.valueOf(idx)));
				} else if (StringUtils.isEmpty(idx)) {
					rVal = String.valueOf(res.get(left));
				}
				if (StringUtils.isNotEmpty(rVal)) {
					if (rVal.matches(right)) {
						return true;
					}
				}
			}
		} else {
			Matcher matcher2 = regex2.matcher(condition);
			if (matcher2.find()) {
				String left = matcher2.group(1);
				String idx = matcher2.group(2);
				String right = matcher2.group(3);
				if (res.containsKey(left) && res.containsKey(right)) {
					if (StringUtils.isNotEmpty(idx) && res.get(left) instanceof List) {
						List val = (List) res.get(left);
						if (String.valueOf(res.get(right)).equals(val.get(Integer.valueOf(idx)))) {
							return true;
						}
					} else if (StringUtils.isEmpty(idx)) {
						if (String.valueOf(res.get(left)).equals(String.valueOf(res.get(right)))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static void format(Map<String, Object> res, Map<String, Object> rules) {
		LOG.info("execute format!");
		for (Entry<String, Object> entry : rules.entrySet()) {
			Object obj = entry.getValue();
			if (obj instanceof Map) {
				Map<String, String> ruleMap = (Map<String, String>) obj;
				for (Entry<String, String> rule : ruleMap.entrySet()) {
					LOG.info("condition is "+rule.getKey()+".checkCondition(res, rule.getKey()) is "+checkCondition(res, rule.getKey())+".formatrule is "+rule.getValue());
					if (checkCondition(res, rule.getKey())) {
						handleFormat(res, rule.getValue());
					}
				}
			} else {
//				LOG.warn("rule is not map. rules=" + JsonUtil.toJSONString(rules));
			}
		}
	}

	private static Object parseToList(Object obj ){
		if(obj instanceof String){
			LOG.info("obj is String");
			try {
				obj = (List)JsonUtil.parseObject(obj.toString());
			} catch (Exception e) {
				LOG.error("parse error!");
				e.printStackTrace();
			}
		}
		return obj;
	}
	private static void handleFormat(Map<String, Object> res, String formatStr) {
		LOG.info("formatstr is "+formatStr);
		if (res == null) {
			return;
		}
		if (StringUtils.isEmpty(formatStr)) {
			return;
		}
		Matcher matcher = regex1.matcher(formatStr);
		if (matcher.find()) {
			String left = matcher.group(1);
			String idx = matcher.group(2);
			String right = matcher.group(3);
			LOG.info("parse regex1 res is "+left + "\t" + idx + "\t" + right);
			if (res.containsKey(left)) {
				String rVal = null;
				if (StringUtils.isNotEmpty(idx) && res.get(left) instanceof List) {
					List val = (List) res.get(left);
					val.set(Integer.valueOf(idx), right);
				} else if (StringUtils.isEmpty(idx)) {
					res.put(left, right);
				}
			}
		} else {
			Matcher matcher2 = regex2.matcher(formatStr);
			if (matcher2.find()) {
				String left = matcher2.group(1);
				String leftIndex = matcher2.group(2);
				String right = matcher2.group(3);
				String rightIndex = matcher2.group(4);
				LOG.info("parse regex2 res is "+left + "\t" + leftIndex + "\t" + right);
				if (res.containsKey(right)) {
					Object rVal =  res.get(right);
					if (StringUtils.isNotEmpty(leftIndex) && res.get(left) instanceof List) {
						if(res.get(right) instanceof List){
							List val = (List) res.get(left);
							val.set(Integer.valueOf(leftIndex), ((List)rVal).get(Integer.parseInt(rightIndex)));
						}else{
							List val = (List) res.get(left);
							val.set(Integer.valueOf(leftIndex), rVal);
						}
						
					} else if (StringUtils.isEmpty(leftIndex)) {
						if(res.get(right) instanceof List){
							res.put(left, ((List)rVal).get(Integer.parseInt(rightIndex)));
						}else{
							res.put(left, rVal);
						}
						
					}
				}
			} else {
				Matcher matcher3 = formatRegex.matcher(formatStr);
				if (matcher3.find()) {
					String left = matcher3.group(1);
					String operator = matcher3.group(2);
					String idx = matcher3.group(3);
					String right = matcher3.group(4);
					LOG.info("parse formatRegex res is "+left + "\t" + operator + "\t" + idx + "\t" + right);
					if ("remove".equals(operator)) {
						LOG.info("inside remove!res.get(left) is List?"+ (res.get(left) instanceof List)+".res.get(left) is "+res.get(left).getClass().getName());
						Object obj = res.get(left);
						if(obj instanceof String){
							obj = parseToList(obj);
						}
						LOG.info("after parseList res.get(left) is List?"+ (obj instanceof List)+".res.get(left) is "+obj.getClass().getName());
						if (obj != null && obj instanceof List) {
							LOG.info("inside obj instanceof List");
							List list = (List) obj;
							int index = Integer.valueOf(idx);
							if (index == -1) {
								list.remove(list.size() - 1);
							} else if (index >= 0 && index < list.size()) {
								list.remove(index);
							}
						}
//						LOG.info("after remove obj is "+JsonUtil.toJSONString(obj));
						res.put(left, obj);
					} else if ("push".equals(operator) && StringUtils.isNotEmpty(right)) {
						Object obj = res.get(left);
						if(obj instanceof String){
							obj = parseToList(obj);
						}
						if (obj != null && obj instanceof List) {
							List list = (List) obj;
							int index = Integer.valueOf(idx);
							if (index == -1) {
								list.add(right);
								res.put(left, list);
							} else {
								List newlist = new ArrayList();
								for (int i = 0; i < list.size(); i++) {
									if (index == i) {
										newlist.add(right);
									}
									newlist.add(list.get(i));
								}
								res.put(left, newlist);
							}
						}
					}
				} else {
					Matcher matcher4 = formatRegex2.matcher(formatStr);
					if (matcher4.find()) {
						String left = matcher4.group(1);
						String operator = matcher4.group(2);
						String idx = matcher4.group(3);
						String right = matcher4.group(4);
						LOG.info("parse formatRegex2 res is "+left + "\t" + operator + "\t" + idx + "\t" + right);
						if ("push".equals(operator) && StringUtils.isNotEmpty(right)) {
							Object obj = res.get(left);
							if (obj != null && obj instanceof List) {
								List list = (List) obj;
								int index = Integer.valueOf(idx);
								Object rObj = res.get(right);
								boolean isList = false;
								if (rObj instanceof List) {
									isList = true;
								}
								if (index == -1) {
									if (isList)
										list.addAll((List) rObj);
									else
										list.add(rObj);
								} else {
									List newlist = new ArrayList();
									for (int i = 0; i < list.size(); i++) {
										if (index == i) {
											if (isList)
												newlist.addAll((List) rObj);
											else
												newlist.add(rObj);
										}
										newlist.add(list.get(i));
									}
									res.put(left, newlist);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
//		 testCheckCondition();
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("city", "全国");
		List<String> area = new ArrayList<String>();
		area.add("中关村");
		area.add("中关村2");
		res.put("area", area);
		List<String> list = new ArrayList<String>();
		list.add("女装");
		list.add("男装");
		list.add("童装");
		res.put("cate", list);
		List<String> region = new ArrayList<String>();
		region.add("海淀");
		region.add("海淀2");
		res.put("region", region);
//		handleFormat(res, "area='全国'");
//		handleFormat(res, "area=city");
//		handleFormat(res, "brand=area[1]");
//		handleFormat(res, "cate[0]='服装's()[]服饰'");
//		 handleFormat(res, "cate[0]=area");
		 handleFormat(res, "cate.remove(0)");
//		 handleFormat(res, "cate.remove(0)");
//		handleFormat(res, "region.push(-1, area)");
//		handleFormat(res, "region.push(1, 'area')");
//		handleFormat(res, "cate.push(-1,'sss')");
	}

	private static void testCheckCondition() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("city", "全国");
		res.put("area", "海淀");
		res.put("test", "女装");
		List<String> list = new ArrayList<String>();
		list.add("女装");
		res.put("cate", list);

	}
}
