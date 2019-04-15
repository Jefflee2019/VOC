package com.bfd.parse.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.config.shelf.JudgeRule;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.task.ParseJob;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	private static final Log log = LogFactory.getLog(JsonUtil.class);

	private static JsonFactory _jsonFactory = null;
	static {
		if (_jsonFactory == null) {
			_jsonFactory = new JsonFactory();
			_jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
			_jsonFactory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		}
	}

	public static final String REQUEST_JUDGE_RULES = "{\"req\":\"judge_rule\"}";
	public static final String REQUEST_ITEMFLDMAPRULES = "{\"req\":\"itemFldMapRule\"}";
	public static final String REQUEST_PARSECONFIG = "{\"req\":\"parseConfig\"}";
	public static final String REQUEST_WEBSITE = "{\"req\":\"website\"}";
	public static final String REQUEST_TEMPLATE = "{\"req\":\"template\",\"type\":\"ALL\"}";
	public static final String REQUEST_COMMON = "{\"type\":\"ALL\"}";

	

	public static String toJSONString(Object object) {
		// return JSON.toJSONString(object);
		try {
			ObjectMapper om = new ObjectMapper(_jsonFactory);
			try {
				return om.writeValueAsString(object);
			} catch (Exception e) {
				log.warn("to json string exception, will use fastjson to parse", e);
			}
			return JSON.toJSONString(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Object parseObject(String str) throws Exception {
		ObjectMapper om = new ObjectMapper(_jsonFactory);
		return om.readValue(str.getBytes(), Object.class);
		// try {
		// return JSON.parseObject(str);
		// } catch (Exception e) {
		// throw e;
		// }
	}

	public static List parseArray(String str) throws Exception {
		ObjectMapper om = new ObjectMapper(_jsonFactory);
		return (List) om.readValue(str.getBytes(), Object.class);
		// try {
		// return (List<Object>) JSON.parseArray(str);
		// } catch (Exception e) {
		// throw e;
		// }
	}

	// public static <T> T parse2Object(String jsonStr, Class<T> clazz) throws
	// Exception {
	// return JSON.toJavaObject(JSON.parseObject(jsonStr), clazz);
	// }
	//
	// public static <T> List<T> parse2List(String jsonStr, Class<T> clazz)
	// throws Exception {
	// if (log.isDebugEnabled())
	// log.debug("parse2List jsonStr is:" + jsonStr);
	// return JSON.parseArray(jsonStr, clazz);
	// }
	//
	// public static List<Template> getTemplates(String jsonStr) throws
	// Exception {
	// return parse2List(jsonStr, Template.class);
	// }

	public static boolean getRuleResult(String rules) throws Exception {
		parseObject(rules);
		return false;
	}

	// public static List<JudgeRule> getRules(String rules) throws Exception {
	// return parse2List(rules, JudgeRule.class);
	// }
	//
	// public static Template getTmplate(String jsonStr) throws Exception {
	// return (Template) parse2Object(jsonStr, Template.class);
	// }

	public static ParseJob parse2Job(String request) {
		return null;
	}

	// =======================================================================
	// public static Template[] getTemplates_(String jsonStr) {
	// if (jsonStr == null || jsonStr.trim().equals("")) {
	// return null;
	// }
	// try {
	// JSONObject obj = JSONObject.parseObject(jsonStr);
	// return getTemplates(obj);
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// public static Template[] getTemplates(JSONObject obj) {
	// try {
	// JSONArray array = obj.getJSONArray("all");
	// Template[] templates = new Template[array.size()];
	// for (int i = 0; i < templates.length; i++) {
	// templates[i] = parseTempalte(array.getJSONObject(i));
	// }
	// return templates;
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// public static Template parseTempalte(JSONObject object) throws
	// JSONException {
	// Template template = new Template();
	// if (object.containsKey("cid")) {
	// template.setCid(object.getString("cid"));
	// }
	// if (object.containsKey("type")) {
	// template.setCid(object.getString("type"));
	// }
	// return template;
	// }

	// --------------------------------------------------------------
	public static List<ParsetemplateEntity> parseTemplates(String templates) {
		List<ParsetemplateEntity> tList = new ArrayList<ParsetemplateEntity>();
		try {
			Object o = parseObject(templates);
			if (o instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> result = (Map<String, Object>) o;
				if (result.containsKey("code") && result.containsKey("data")) {
					if ((Integer) result.get("code") == 0) {
						List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
						for (Map<String, Object> tmpl : list) {
//							ParseTemplate template = ParseTemplate.fromMap(tmpl);
							ParsetemplateEntity template = JacksonUtils.extractObject(JsonUtils.toJSONString(tmpl), ParsetemplateEntity.class);
							tList.add(template);
						}
						return tList;
					}
				}
			}
		} catch (Exception e) {
			log.warn("parse json exception, err:" + e.getMessage());
		}
		return tList;
	}

	public static List<JudgeRule> parseRules(String rules) {
		List<JudgeRule> rList = new ArrayList<JudgeRule>();
		try {
			Object o = parseObject(rules);
			Map<String, Object> result = (Map<String, Object>) o;
			if (result.containsKey("code") && result.containsKey("data")) {
				if ((Integer) result.get("code") == 0) {
					List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
					for (Map<String, Object> tmpl : list) {
						JudgeRule template = JudgeRule.create(tmpl);
						rList.add(template);
					}
					return rList;
				}
			}
		} catch (Exception e) {
			log.warn("parse json exception, err:" + e.getMessage());
		}
		return rList;
	}

	public static void main(String[] args) {
		// System.out.println(new JSONArray() instanceof List);
		try {
			JsonUtil.parseObject(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
