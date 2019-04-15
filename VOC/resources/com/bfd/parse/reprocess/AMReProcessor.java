package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;


public abstract class AMReProcessor implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(AMReProcessor.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> processdata = null;
		try {
			processdata = new HashMap<String, Object>();
			Map<String, Object> resultData = result.getParsedata().getData();
			Map<String, Object> taskData = result.getTaskdata();

			// LOG.info("url is"+unit.getUrl()+".type is  "+unit.getType());
			extend(resultData, unit);
			// description不需要加入到properties里面，像天猫http://detail.tmall.com/item.htm?id=38083058510
			// addDescToProperties(resultData, unit);
			// if(resultData.get(Constants.DESC)!=null){
			// TextUtil.stringToMap(resultData,unit,resultData.get(Constants.DESC).toString(),Constants.PROPERTIES);
			// }
			if (resultData.get(Constants.PROPERTIES) == null
					|| ((resultData.get(Constants.PROPERTIES) instanceof Map) && ((Map) resultData
							.get(Constants.PROPERTIES)).size() == 0)) {
				addBriefToProperties(resultData, unit);
			}

			// if(resultData.get(Constants.BRIEF)!=null){
			// TextUtil.stringToMap(resultData,unit,resultData.get(Constants.BRIEF).toString(),Constants.PROPERTIES);
			// }
			TextUtil.addCate(resultData, unit, taskData);
			addListCate(resultData, unit, taskData);
			TextUtil.addBrand(resultData, unit);
			addListBrand(resultData, unit, taskData);
			TextUtil.addModelType(resultData, unit);
			TextUtil.addMinMaxPrice(resultData, unit);
			formatData(resultData);
			LOG.info("url is " + unit.getUrl() + ".after reprocess is  "
					+ JsonUtil.toJSONString(resultData));
		} catch (Exception e) {
			LOG.error("reprocess error total!");
			result.getParsedata().setParsecode(ParseResult.REPROCESS_FAILED);
			e.printStackTrace();
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void formatData(Map<String, Object> resultData) {
		if (resultData.get(Constants.promotion) != null
				&& resultData.get(Constants.promotion).toString().trim()
						.length() < 2) {
			resultData.put(Constants.promotion, "");
		}
		if (resultData.get(Constants.MODEL_TYPE) != null) {
			resultData.put(
					Constants.MODEL_TYPE,
					resultData.get(Constants.MODEL_TYPE).toString()
							.replace("--", ""));
		}
		if (resultData.get(Constants.BRAND_NAME) != null) {
			resultData.put(
					Constants.BRAND_NAME,
					resultData.get(Constants.BRAND_NAME).toString()
							.replace("--", ""));
		}

	}

	public void addShopName() {

	}

	public void addListCate(Map<String, Object> resultData, ParseUnit unit, Map<String, Object> taskData) {
		Map<String, Object> attr = (Map) taskData.get("attr");
		if (attr != null && attr.get("list_cate") != null) {
			resultData.put(Constants.LIST_CATE, attr.get("list_cate"));
		} else {
			LOG.info("url:" + unit.getUrl() + ".taskdata not  " + "list_cate");
		}
	}

	public void addListBrand(Map<String, Object> resultData, ParseUnit unit, Map<String, Object> taskData) {
		Map<String, Object> attr = (Map) taskData.get("attr");
		if (attr != null && attr.get("list_brand") != null) {
			resultData.put(Constants.LIST_BRAND, attr.get("list_brand"));
		} else {
			LOG.info("url:" + unit.getUrl() + ".taskdata not  " + "list_brand");
		}
	}

	public void addBriefToProperties(Map<String, Object> resultData,
			ParseUnit unit) {
		if (resultData.get(Constants.BRIEF) != null) {
			TextUtil.stringToMap(resultData, unit,
					resultData.get(Constants.BRIEF).toString(),
					Constants.PROPERTIES);
		}
	}

	// public void addDescToProperties(Map<String, Object> resultData,ParseUnit
	// unit){
	// if(resultData.get(Constants.DESC)!=null){
	// TextUtil.stringToMap(resultData,unit,resultData.get(Constants.DESC).toString(),Constants.PROPERTIES);
	// }
	// }
	public abstract void extend(Map<String, Object> resultData, ParseUnit unit);

}
