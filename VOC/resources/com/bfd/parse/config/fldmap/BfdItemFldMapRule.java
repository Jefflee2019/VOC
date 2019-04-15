package com.bfd.parse.config.fldmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class BfdItemFldMapRule {

	private String id;
	private String cid;
	private Map<String, String> directFields;
	private Map<String, String> fieldsType;
	private Map<String, String> fieldsRegex;
	private Map<String, Object> formatRules;
	private Map<String, Pattern> fieldsPattern;
	private List<String> normalizeFields;
	private List<String> requiredFields;
	private String status;
	private String createtime;

	private static final Set<String> dftRequiredFldSet = new HashSet<String>();
	private static String[] flds = { "directFields", "requiredFields" };
	static {
		dftRequiredFldSet.add("id");
		dftRequiredFldSet.add("name");
		dftRequiredFldSet.add("price");
		dftRequiredFldSet.add("image_link");
	}

	public static BfdItemFldMapRule fromMap(Map<String, Object> param) throws Exception {
		for (String key : flds) {
			if (!param.containsKey(key)) {
				return null;
			}
		}
		String id = (String) param.get("id");
		String cid = (String) param.get("cid");
		Map<String, String> directFields = (Map<String, String>) param.get("directFields");
		Map<String, String> fieldsType = (Map<String, String>) param.get("fieldsType");
		Map<String, String> fieldsRegex = (Map<String, String>) param.get("fieldsRegex");
		Map<String, Object> formatRules = (Map<String, Object>) param.get("formatRules");
		List<String> requiredFields = (List<String>) param.get("requiredFields");
		List<String> normalizeFields = (List<String>) param.get("normalizeFields");
		String status = "0";
		if (param.containsKey("status")) {
			status = (String) param.get("status");
		}
		return new BfdItemFldMapRule(id, cid, directFields, requiredFields, fieldsType, fieldsRegex, formatRules,
				normalizeFields, status);
	}

	public BfdItemFldMapRule(String id, String cid, Map<String, String> directFields, List<String> requiredFields,
			Map<String, String> fieldsType, Map<String, String> fieldsRegex, Map<String, Object> formatRules,
			List<String> normalizeFields, String status) {
		this.id = id;
		this.cid = cid;
		this.directFields = directFields;
		this.requiredFields = requiredFields;
		this.fieldsType = fieldsType;
		this.fieldsRegex = fieldsRegex;
		this.formatRules = formatRules;
		this.normalizeFields = normalizeFields;
		this.status = status;
	}

	public BfdItemFldMapRule() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public Map<String, String> getDirectFields() {
		return directFields;
	}

	public void setDirectFields(Map<String, String> directFields) {
		this.directFields = directFields;
	}

	public Map<String, String> getFieldsType() {
		return fieldsType;
	}

	public void setFieldsType(Map<String, String> fieldsType) {
		this.fieldsType = fieldsType;
	}

	public Map<String, String> getFieldsRegex() {
		return fieldsRegex;
	}

	public Map<String, Pattern> getFieldsPattern() {
		if (fieldsPattern == null) {
			fieldsPattern = new HashMap<String, Pattern>();
			if (getFieldsRegex() != null) {
				for (Entry<String, String> regexEntry : getFieldsRegex().entrySet()) {
					fieldsPattern.put(regexEntry.getKey(), Pattern.compile(regexEntry.getValue()));
				}
			}
		}
		return fieldsPattern;
	}

	public void setFieldsRegex(Map<String, String> fieldsRegex) {
		this.fieldsRegex = fieldsRegex;
		fieldsPattern = null;
	}

	public Map<String, Object> getFormatRules() {
		return formatRules;
	}

	public void setFormatRules(Map<String, Object> formatRules) {
		this.formatRules = formatRules;
	}

	public List<String> getNormalizeFields() {
		return normalizeFields;
	}

	public void setNormalizeFields(List<String> normalizeFields) {
		this.normalizeFields = normalizeFields;
	}

	public List<String> getRequiredFields() {
		if (requiredFields == null) {
			requiredFields = new ArrayList<String>();
			requiredFields.addAll(dftRequiredFldSet);
		}
		return requiredFields;
	}

	public void setRequiredFields(List<String> requiredFields) {
		this.requiredFields = requiredFields;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

}
