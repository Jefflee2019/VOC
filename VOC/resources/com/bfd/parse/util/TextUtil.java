package com.bfd.parse.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.parse.Constants;
//import com.bfd.parse.data.Website;
//import com.bfd.parse.data.WebsiteMap;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.test.ParseTester;

public class TextUtil {
	private static final Pattern cntFilter = Pattern.compile("\\d+");
	private static final Log LOG = LogFactory.getLog(TextUtil.class);

	public static String removeHtml(String str) {
		str = str.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		str = TextUtil.removeAllHtmlTags(str);
		return str;
	}

	public static void replaceByReplaceRule(Map<String, List<String>> replaceRule,
			Map<String, Object> reply, String key) {
		if (!replaceRule.containsKey(key)) {
			return;
		}
		List<String> rules = replaceRule.get(key);
		String source = reply.get(key).toString();
		for (String rule : rules) {
			if (rule.indexOf("|") > 0) {
				String[] strs = rule.split("\\|");
				source = source.replace(strs[0], strs[1]);
			} else {
				source = source.replace(rule, "");
			}
		}
		reply.put(key, source.trim());
	}

	public static List<JsonData> wrapJsonData(ParseUnit unit, boolean inData) {
		List<JsonData> dataList = new ArrayList<JsonData>();
		// 构造AJAX数据 , 不在data中的json数据, 如zouxiu
		if (!inData) {
			List<Map<String, Object>> ajaxDatas = unit.getAjaxdata();
			for (int i = 0; i < ajaxDatas.size(); i++) {
				Map<String, Object> data = ajaxDatas.get(i);
				try {
					Set<String> keys = data.keySet();
					Map<String, Object> extend = new HashMap<String, Object>();
					JsonData jsonData = new JsonData();
					for (String key : keys) {
						if (key.equalsIgnoreCase("url")) {
							jsonData.setUrl((String) data.get(key));
						} else if (key.equalsIgnoreCase("data")) {
							jsonData.setData(DataUtil
									.unzipAndDecode((String) data.get(key)));
						} else if (key.equalsIgnoreCase("charset")) {
							jsonData.setCharset((String) data.get(key));
						} else if (key.equalsIgnoreCase(Constants.code)) {
							jsonData.setHttpcode(String.valueOf(data
									.get(Constants.code)));
						} else {
							extend.put(key, data.get(key));
						}
					}
					jsonData.setExtend(extend);
					dataList.add(jsonData);
				} catch (Exception e) {
					LOG.warn(" upzip and decode ajaxdata exception:", e);
				}
			}
		} else {
			// 在data中的json数据, 如guoku
			Map<String, Object> extend = new HashMap<String, Object>();
			JsonData jsonData = new JsonData();
			try {
				byte[] bytes = DataUtil.unzipAndDecode(unit.getData());
				jsonData.setData(bytes);
				String dftCharset = "UTF8";
				if (StringUtils.isNotEmpty(unit.getCharset())) {
					dftCharset = unit.getCharset();
				}
				String charset = EncodeUtil.getHtmlEncode(bytes, dftCharset);
				jsonData.setCharset(charset);
			} catch (Exception e) {
				LOG.warn(" parseJsonData exception:", e);
			}
			jsonData.setExtend(extend);
			jsonData.setUrl(unit.getUrl());
			jsonData.setHttpcode(unit.getHttpcode());
			dataList.add(jsonData);
		}
		return dataList;
	}

	public static boolean unzipPageAndGuessEncode(ParseUnit unit) {
		if (unit.getPageBytes() != null) {
			return true;
		}
		byte[] data;
		try { // 解压缩解密
			data = DataUtil.unzipAndDecode(unit.getData());
		} catch (Exception e) {
			LOG.warn(" unzip and Decode data Exception, Err:", e);
			return false;
		}
		unit.setPageBytes(data);
		String charset = Constants.CHARSET_UTF8;
		// 获取页面编码
		if (StringUtils.isNotEmpty(unit.getCharset())
				&& !unit.getCharset().toLowerCase().contains("html")
				&& !unit.getCharset().toLowerCase().contains("json")
				&& !unit.getCharset().toLowerCase().contains("javascript")) {
			charset = unit.getCharset();
		} else {
			charset = EncodeUtil.getHtmlEncode(data, charset);
			if (charset.equalsIgnoreCase("GB18030")) {
				LOG.warn("Guess page encode = GB18030， url=" + unit.getUrl());
			}
		}
//		if(unit.getUrl().equals("http://forum.sports.sina.com.cn/forum-8-1.html")){
//			dftCharset = "UTF8";
//		}
		
		
		LOG.info("url:"+unit.getUrl() + " guess encode=" + charset + ", " );
		unit.setPageEncode(charset);
		try {
			String page = new String(unit.getPageBytes(), unit.getPageEncode());
//			Website website = WebsiteMap.INSTANCE.get(unit.getCid());
//			if (website != null && website.getCleantag() == 1) {
			//TODO 现在都做补全
//				page = Jsoup.parse(page).html();
//				unit.setPageBytes(page.getBytes());
//				unit.setCharset("utf-8");
//			}
			unit.setPageData(page);
			// logger.info("html:"+page);
			LOG.info("url is " + unit.getUrl() + ".length is "
					+ unit.getPageData().length() + ".");
		} catch (UnsupportedEncodingException e) {
			LOG.warn(" new page data string Exception, Err:", e);
			try {
				unit.setPageData(new String(unit.getPageBytes(), Constants.CHARSET_UTF8));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}

	public static boolean unzipPageAndGuessEncodeForPluginTest(ParseUnit unit) {
		if (unit.getPageBytes() != null) {
			return true;
		}
		byte[] data;
		try { // 解压缩解密
			data = DataUtil.unzipAndDecode(unit.getData());
		} catch (Exception e) {
			LOG.warn(" unzip and Decode data Exception, Err:", e);
			return false;
		}
		unit.setPageBytes(data);
		String dftCharset = "UTF8";
		// 获取页面编码
		if (StringUtils.isNotEmpty(unit.getCharset())
				&& !unit.getCharset().contains("html")) {
			dftCharset = unit.getCharset();
		}
		String charset = EncodeUtil.getHtmlEncode(data, dftCharset);
		if (charset.equalsIgnoreCase("GB18030")) {
			LOG.warn("Guess page encode = GB18030， url=" + unit.getUrl());
		}
		// logger.info(workName + " guess encode=" + charset + ", url=" +
		// unit.getUrl());
		unit.setPageEncode(charset);
		try {
			String page = new String(unit.getPageBytes(), unit.getPageEncode());
			// Website website = WebsiteMap.INSTANCE.get(unit.getCid());
			// if (website != null && website.getCleantag() == 1) {
			// page = Jsoup.parse(page).html();
			// unit.setPageBytes(page.getBytes());
			// unit.setCharset("utf8");
			// }
			unit.setPageData(page);
			// logger.info("html:"+page);
			LOG.info("url is " + unit.getUrl() + ".length is "
					+ unit.getPageData().length() + ".");
		} catch (UnsupportedEncodingException e) {
			LOG.warn(" new page data string Exception, Err:", e);
			return false;
		}
		return true;
	}

	public static String filterInteger(String string) {
		if (StringUtils.isNotEmpty(string)) {
			if (StringUtils.isNumeric(string)) {
				return string;
			} else {
				Matcher matcher = cntFilter.matcher(string);
				if (matcher.find()) {
					string = matcher.group(0);
					return string;
				}
			}
		}
		return null;
	}

	/**
	 * 补全标签
	 * 
	 * @return
	 */
	public static byte[] balanceTag(byte[] data, String charset) {
		try {

			String page = new String(data, charset);
			page = Jsoup.parse(page).html();
			// page = StringEscapeUtils.unescapeHtml(page);
			// TODO:用于测试，需要删除，否则日志太大
			// LOG.info("补全后的html："+page);
			data = page.getBytes(charset);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static boolean hasValue(String str) {
		return str == null || str.trim().equals("") ? false : true;
	}

	/**
	 * 去除指定文本中所有html标签 。
	 * 
	 * @param str
	 *            指定文本
	 */
	public static String removeAllHtmlTags(String str) {
		if (hasValue(str))
			return str.replaceAll("<[^<>]+?>", "");

		return "";
	}

	public static String getRegexGroup(String regex, String str, int id) {
		String resultStr = "";
		if (hasValue(str)) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(str);
			if (m.find()) {
				resultStr = m.group(id);
			}
		}
		// if(resultStr.equals("")){
		// log.error(regex+" parser error!");
		// }
		return resultStr;
	}

	public static String getRegexGroup(Pattern p, String str, int id) {
		String resultStr = "";
		if (hasValue(str)) {
			Matcher m = p.matcher(str);
			if (m.find()) {
				resultStr = m.group(id);
			}
		}
		// if(resultStr.equals("")){
		// log.error(regex+" parser error!");
		// }
		return resultStr;
	}

	public static String getstr(String source, String regex) {
		String tmp = null;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(source);
		if (m.find()) {
			tmp = m.group(1);
		}
		return tmp;
	}

	public static void stringToMap(Map<String, Object> resultData,
			ParseUnit unit, String str, String keyName) {
		Map<String, String> properties = executeStringToMap(str);
		if (!resultData.containsKey(keyName)) {
			resultData.put(keyName, new HashMap<String, String>());
		}
		((Map) resultData.get(keyName)).putAll(properties);
	}

	public static Map<String, String> executeStringToMap(String str){
		Map<String, String> properties = new HashMap<String, String>();
		String[] descArray = str.split("\t");
		if (str.indexOf("\t") >= 0) {
			descArray = str.split("\t");
		} else {
			descArray = str.split(" ");
		}
		String[] temp = null;
		for (int i = 0; i < descArray.length; i++) {
			if (descArray[i].indexOf(":") == 0
					|| descArray[i].indexOf("：") == 0) {
				descArray[i] = descArray[i].substring(1);
			}
			if (descArray[i].indexOf(": ") >= 0) {
				temp = descArray[i].split(": ");
			} else if (descArray[i].indexOf("： ") >= 0) {
				temp = descArray[i].split("： ");
			} else if (descArray[i].indexOf("：") > 0) {
				temp = descArray[i].split("：");
			} else {
				temp = descArray[i].split(" ");
			}
			if (temp.length < 2) {
				continue;
			}
			if (temp.length > 2) {
				String value = "";
				for (int j = 1; j < temp.length; j++) {
					value += temp[j] + " ";
				}
				temp[1] = value;
			}
			temp[0] = temp[0].replaceAll("：", "").replace(":", "")
					.replace(" ", "").trim();
			properties.put(temp[0], temp[1]);
		}
		return properties;
	}
	public static String getUnzipJson(JsonData data, ParseUnit unit) {
		String json = null;

		try {
			json = new String(data.getData(), unit.getPageEncode());
			LOG.info("url:"+data.getUrl()+" use encode "+unit.getPageEncode());
		} catch (Exception e1) {
			try {
				// 如果异常就用主页的编码
				json = new String(data.getData(), data.getCharset());
				LOG.info("url:"+data.getUrl()+" use chaset "+data.getCharset());
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("twice UnsupportedEncodingException charset is "+unit.getPageEncode());
			}
//			e1.printStackTrace();
			LOG.error("UnsupportedEncodingException2 charset is "+data.getCharset());
		}
		return json;
	}

	public static String getUnzipJson(JsonData data, String defaultEncode) {
		String json = null;

		try {
			json = new String(data.getData(), data.getCharset());
			LOG.info("url:"+data.getUrl()+" use charset "+data.getCharset());
		} catch (Exception e1) {
			try {
				// 如果异常就用主页的编码
				json = new String(data.getData(), defaultEncode);
				LOG.info("url:"+data.getUrl()+" use encode "+defaultEncode);
			} catch (Exception e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
		}
		return json;
	}

	public static void addCate(Map<String, Object> resultData, ParseUnit unit,
			Map<String, Object> taskData) {
		if (taskData.get(Constants.CATE) != null
				&& resultData.get(Constants.CATE) == null) {
			resultData.put(Constants.CATE, taskData.get(Constants.CATE));
		}
	}

	public static void addModelType(Map<String, Object> resultData,
			ParseUnit unit) {
		if (resultData.get(Constants.PROPERTIES) == null) {
			return;
		}
		Map<String, String> properties = (Map<String, String>) resultData
				.get(Constants.PROPERTIES);
		String[] modelType = { "型号", "产品型号", "其他型号", "分类" };
		for (int i = 0; i < modelType.length; i++) {
			if (properties.containsKey(modelType[i])) {
				resultData.put(Constants.MODEL_TYPE,
						properties.get(modelType[i]));
				LOG.info("url:" + unit.getUrl() + ".modelType key is "
						+ modelType[i]);
				break;
			}
		}
		if (!resultData.containsKey(Constants.MODEL_TYPE)) {
			Iterator<Entry<String, String>> it = properties.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (entry.getKey().endsWith("型号")) {
					resultData.put(Constants.MODEL_TYPE, entry.getValue());
					LOG.info("url:" + unit.getUrl() + ".modelType key is "
							+ entry.getKey());
					break;
				}
			}
		}
	}

	public static void addBrand(Map<String, Object> resultData, ParseUnit unit) {
		if (resultData.get(Constants.PROPERTIES) == null) {
			return;
		}
		if (resultData.get(Constants.BRAND_NAME) != null) {
			LOG.info("url:" + unit.getUrl() + ".template rs brand_name is "
					+ resultData.get(Constants.BRAND_NAME));
			return;
		}
		Map<String, String> properties = (Map<String, String>) resultData
				.get(Constants.PROPERTIES);
		Iterator<Entry<String, String>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			if (entry.getKey().indexOf("品牌") >= 0) {
				resultData.put(Constants.BRAND_NAME, entry.getValue());
				LOG.info("url:" + unit.getUrl() + ".BRAND_NAME key is "
						+ entry.getKey());
				break;
			}
		}
		// 适用于淘宝情况
		if (resultData.get(Constants.BRAND_NAME) == null) {
			resultData.put(Constants.BRAND_NAME, properties.get("产品名称"));
		}
		// 品牌优先级高
		// String[] brand = { "品牌","品牌名称", "产品名称" };
		// for (int i = 0; i < brand.length; i++) {
		// if (properties.containsKey(brand[i])) {
		// resultData.put(Constants.BRAND_NAME, properties.get(brand[i]));
		// LOG.info("url:" + unit.getUrl() + ".BRAND_NAME key is "
		// + brand[i]);
		// break;
		// }
		// }
	}

	public static void addShopName(Map<String, Object> resultData,
			ParseUnit unit) {
		if (resultData.get(Constants.SHOP_PROPERTIES) == null) {
			return;
		}
		if (resultData.get(Constants.SHOP_NAME) != null) {
			LOG.info("url:" + unit.getUrl() + ".template rs SHOP_NAME is "
					+ resultData.get(Constants.SHOP_NAME));
			return;
		}
		Map<String, String> properties = (Map<String, String>) resultData
				.get(Constants.SHOP_PROPERTIES);
		String[] brand = { "掌柜", "公司名" };
		for (int i = 0; i < brand.length; i++) {
			if (properties.containsKey(brand[i])) {
				resultData.put(Constants.SHOP_NAME, properties.get(brand[i]));
				LOG.info("url:" + unit.getUrl() + ".SHOP_NAME key is "
						+ brand[i]);
				break;
			}
		}
	}

	public static void addMinMaxPrice(Map<String, Object> resultData,
			ParseUnit unit) {
		if (resultData.get("price") == null) {
			LOG.info("result no price . url :" + unit.getUrl());
			return;
		}
		String price = resultData.get("price").toString();
		String[] priceArray = price.split("-");
		if (priceArray != null && priceArray.length == 2) {
			resultData.put(Constants.PRICE_LOW, priceArray[0].trim()
					.replaceAll(":", "").replaceAll("￥", ""));
			resultData.put(Constants.PRICE_HIGH, priceArray[1].trim()
					.replaceAll(":", "").replaceAll("￥", ""));
		} else {
			resultData.put(Constants.PRICE_HIGH, price.replaceAll(":", "")
					.replaceAll("￥", ""));
		}
	}

	public static void addMinMaxPriceTest(String price) {

		String[] priceArray = price.split("-");
		System.out.println(priceArray[0].trim().replaceAll(":", "")
				.replaceAll("￥", ""));
		System.err.println(priceArray[1].trim().replaceAll(":", "")
				.replaceAll("￥", ""));

	}

	/**
	 * 得到最长公共子串
	 * 
	 * @param str1
	 * @param str2
	 */
	public static String getMaxCommonStr(String s1, String s2) {
		char[] str1 = s1.toCharArray();
		char[] str2 = s2.toCharArray();
		int i, j;
		int len1, len2;
		len1 = str1.length;
		len2 = str2.length;
		int maxLen = len1 > len2 ? len1 : len2;
		int[] max = new int[maxLen];
		int[] maxIndex = new int[maxLen];
		int[] c = new int[maxLen];

		for (i = 0; i < len2; i++) {
			for (j = len1 - 1; j >= 0; j--) {
				if (str2[i] == str1[j]) {
					if ((i == 0) || (j == 0))
						c[j] = 1;
					else
						c[j] = c[j - 1] + 1;
				} else {
					c[j] = 0;
				}

				if (c[j] > max[0]) { // 如果是大于那暂时只有一个是最长的,而且要把后面的清0;
					max[0] = c[j];
					maxIndex[0] = j;

					for (int k = 1; k < maxLen; k++) {
						max[k] = 0;
						maxIndex[k] = 0;
					}
				} else if (c[j] == max[0]) { // 有多个是相同长度的子串
					for (int k = 1; k < maxLen; k++) {
						if (max[k] == 0) {
							max[k] = c[j];
							maxIndex[k] = j;
							break; // 在后面加一个就要退出循环了
						}

					}
				}
			}
		}
		List<String> rss = new ArrayList<String>();
		for (j = 0; j < maxLen; j++) {
			StringBuffer sb = new StringBuffer();
			if (max[j] > 0) {
				// System.out.println("第" + (j + 1) + "个公共子串:");
				for (i = maxIndex[j] - max[j] + 1; i <= maxIndex[j]; i++) {
					// System.out.print(str1[i]);
					sb.append(str1[i]);
				}
				rss.add(sb.toString());

			}
		}
		// System.out.println(JsonUtil.toJSONString(rss));
		if (rss.size() > 0) {
			return rss.get(0);
		} else {
			return "";
		}
	}

	public static void main(String[] args) {
		// String str = "dfdkfjd23小时23分钟结束";
		// String regexDay = "(\\d+)天";
		// String regexHour = "(\\d+)小时";
		// String regexMinute = "(\\d+)分钟";
		// System.out.println(getRegexGroup(regexDay, str, 1));
		// System.out.println(getRegexGroup(regexHour, str, 1));
		// System.out.println(getRegexGroup(regexMinute, str, 1));
		// String price = "1273.00 - 1666.00";
		// new TextUtil().addMinMaxPriceTest(price);
		// s1:产品名称：Samsung/三星 UA65HU9800....s2:title="Samsung/三星 UA65HU9800J"

//		String s1 = "产品名称：Samsung/三星 UA65HU9800...";
//		String s2 = "title=\"Samsung/三星 UA65HU9800J\"";
//		String rs = getMaxCommonStr(s1, s2);
//		System.out.println("rs:" + rs);
//		String str = "财产: 6199 爱卡币	帖子: 4329帖 查看>>	注册: 2009-10-09	来自: 江苏|南京";
		String str = "财产: 489 爱卡币 帖子: 151帖 查看>> 注册: 2013-09-22 来自: 上海市|上海市";
		Map<String, String> rs = executeStringToMap(str);
		System.out.println(JsonUtil.toJSONString(rs));
//				new HashMap<String, Object>();
//		stringToMap(rs, null, str, "rs12");
//		System.out.println(JsonUtil.toJSONString(rs));
	}
}
