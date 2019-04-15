package com.bfd.parse.facade.autoparse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.kafka7.KfkProducer;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.ThreadUtils;
import com.bfd.parse.AutoRuleParser;
import com.bfd.parse.Constants;
import com.bfd.parse.DataSaver;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParseStat;
import com.bfd.parse.ParserFace;
import com.bfd.parse.client.DeduplicatorClient;
import com.bfd.parse.config.iid.ParseCfg;
import com.bfd.parse.config.iid.ParseConfigure;
import com.bfd.parse.config.iid.ParseReProcessor;
//import com.bfd.parse.data.WebsiteMap;
import com.bfd.parse.facade.IParse;
import com.bfd.parse.facade.parseunit.ParseUnit;
//import com.bfd.parse.learn.LearnUnit;
//import com.bfd.parse.service.Parser;
import com.bfd.parse.util.JsonUtil;

public class AutoParser implements IParse {

	@Override
	public Object parse(String jsonStr) {
		return null;
	}
//	private static final Log logger = LogFactory.getLog(ParserFace.class);
//	private AutoRuleParser autoRuleParser = this.autoRuleParser = new AutoRuleParser(ThreadUtils.getCurrentThreadName());
//	private DeduplicatorClient deduplicator = new DeduplicatorClient(ThreadUtils.getCurrentThreadName());
//	private ParseStat stater = new ParseStat(ThreadUtils.getCurrentThreadName());
//	private DataSaver dataSaver = new DataSaver(ThreadUtils.getCurrentThreadName());
//	private boolean verifyIid(ParseUnit unit) {
//		Integer iidtype = WebsiteMap.INSTANCE.get(unit.getCid()).getIidtype();
//		if (iidtype != null && iidtype == 1) {
//			if (StringUtils.isNotEmpty(unit.getIid())
//					&& unit.getUrl().contains(unit.getIid())) {
//				return true;
//			}
//			ParseCfg config = ParseConfigure.getInstance().getParseConfig(
//					unit.getCid());
//			if (config == null) {
//				logger.warn(ThreadUtils.getCurrentThreadName() + " iid rule empty, cid -> "
//						+ unit.getCid());
//				return false;
//			}
//			Pattern pattern = config.getIidPattern();
//			String iid = ParseReProcessor.parseIid(pattern, unit.getUrl());
//			if (StringUtils.isEmpty(iid)) {
//				logger.warn(ThreadUtils.getCurrentThreadName()
//						+ " url match iid rule failed, url -> " + unit.getUrl());
//				return false;
//			}
//			logger.info(ThreadUtils.getCurrentThreadName()
//					+ " got info iid from url by iidrule, iid->" + iid
//					+ ", url->" + unit.getUrl());
//			unit.setIid(iid);
//		}
//		return true;
//	}
//	
//	@Override
//	public Object parse(String jsonStr) {
//		ParseUnit unit = null;
//		ParseResult result = ParseResult.prepareObj(unit);
//		try {
//			
//			long time = System.currentTimeMillis();
//			Object params = JsonUtils.parseToObject(jsonStr);
//			unit = ParseUnit.fromMap((Map<String, Object>)params, time);
//			
//			if (unit.isInfo() && !verifyIid(unit)) { // 自动解析的情况下，验证iid
//				result.getParsedata().setParsecode(ParseResult.FAILED_NO);
//				//流程中不在调用统计，而是写入到kafka队列中
//				stater.increment(unit.getProjectname(), unit.getCid(),
//						unit.getPageType(), ParseResult.FAILED_NO);
//				//TODO:需要测试
//				this.dataSaver.saveData(unit, result);
//				return result;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return autoParse(unit, result, false);
//	}
//	
//	private boolean offPageByLoc(String location, ParseUnit unit) {
//		if (StringUtils.isNotEmpty(location)
//				&& !location.equalsIgnoreCase(unit.getUrl())) {
//			URL url;
//			try {
//				url = new URL(location);
//				if ((StringUtils.isEmpty(url.getPath()) || "/".equals(url
//						.getPath())) && StringUtils.isEmpty(url.getQuery())) {
//					logger.info(ThreadUtils.getCurrentThreadName()
//							+ " off page by has no path and query, location -> "
//							+ location + ", url -> " + unit.getUrl());
//					return true;
//				}
//			} catch (MalformedURLException e) {
//				logger.warn(ThreadUtils.getCurrentThreadName(), e);
//			}
//			String type = Parser.getPageType(unit.getPageData(), unit.getCid(),
//					location, false);
//			if (!"info".equalsIgnoreCase(type)) {
//				logger.info(ThreadUtils.getCurrentThreadName()
//						+ " off page by pagetype is not info, location -> "
//						+ location + ", url -> " + unit.getUrl());
//				return true;
//			}
//			if (deduplicator.duplicate("location", false)) {
//				logger.info(ThreadUtils.getCurrentThreadName() + " off page by dedup, location -> "
//						+ location + ", url -> " + unit.getUrl());
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	private ParseResult autoParse(ParseUnit unit, ParseResult result,
//			boolean test) {
//		try {
//			String location = (String) unit.getSpiderdata().get("location");
//			if (unit.isInfo() && offPageByLoc(location, unit)) {
//				Map<String, Object> res = new HashMap<String, Object>();
//				res.put("cid", unit.getCid());
//				res.put("bfdiid", unit.getBfdiid());
//				res.put("url", unit.getUrl());
//				res.put("type", unit.getPageType());
//				res.put("iid", unit.getIid());
//				res.put("onshelf", "off");
//				res.put("offreason", "onshelvesloc");
//				result.getParsedata().setParsecode(ParseResult.OFF);
////				dataSaver.saveData(unit, result);
//				//TODO:
//				stater.increment(unit.getProjectname(), unit.getCid(),
//						unit.getPageType(), ParseResult.NOT_ITEM_TASK);
//				return result;
//			}
//			logger.info(ThreadUtils.getCurrentThreadName() + " autoparse prepare pagedata, url->"
//					+ unit.getUrl());
//			String pageData = LearnUnit.prepare(unit.getData(), unit.getCid(),
//					unit.getUrl(), true);
//			unit.setPageData(pageData);
//		} catch (Exception e) {
//			result.getParsedata().setParsecode(
//					ParseResult.PARSECODE_DWONLOADFAILED);
//			return result;
//		}
//
//		if (unit.isInfo()
//				&& StringUtils.isNotEmpty(unit.getPurl())
//				&& !unit.getPageType().equalsIgnoreCase(
//						Parser.getPageType(unit.getPageData(), unit.getCid(),
//								unit.getUrl(), false))) {
//			result.getParsedata().setParsecode(ParseResult.NOT_ITEM_TASK);
//			logger.info(ThreadUtils.getCurrentThreadName() + " autoparse got wrong type, url ->"
//					+ unit.getUrl());
//			Map<String, Object> res = new HashMap<String, Object>();
//			res.put("cid", unit.getCid());
//			res.put("bfdiid", unit.getBfdiid());
//			res.put("url", unit.getUrl());
//			res.put("type", unit.getPageType());
//			res.put("iid", unit.getIid());
//			res.put("onshelf", "off");
//			res.put("offreason", "wrongtype");
//			result.getParsedata().setParsecode(ParseResult.SUCCESS);
//			dataSaver.saveData(unit, result);
//			stater.increment(unit.getProjectname(), unit.getCid(),
//					unit.getPageType(), ParseResult.NOT_ITEM_TASK);
//			return result;
//		}
//		Map<String, Object> data = autoRuleParser.parse(unit);
//		Map<String, Object> resultData = result.getParsedata().getData();
//		resultData.putAll(data);
//		if (unit.isInfo()) {
//			logger.info(ThreadUtils.getCurrentThreadName() + " autoparse got result data ->"
//					+ JsonUtil.toJSONString(data));
//			if (!data.containsKey("title") || !data.containsKey("contents")) {
//				logger.warn(ThreadUtils.getCurrentThreadName()
//						+ " autoparse parse result failed, no title or contents");
//				result.getParsedata().setParsecode(
//						ParseResult.AUTO_PARSE_FAILED);
//				return result;
//			}
//			resultData.put("cid", unit.getCid());
//			resultData.put("bfdiid", unit.getBfdiid());
//			resultData.put("url", unit.getUrl());
//			resultData.put("type", unit.getPageType());
//			resultData.put("iid", unit.getIid());
//			result.getParsedata().setParsecode(ParseResult.SUCCESS);
//			if (!test)
//				dataSaver.saveData(unit, result);
//		}
//		if (!test)
//			stater.increment(unit.getProjectname(), unit.getCid(),
//					unit.getPageType(), ParseResult.SUCCESS);
//		return result;
//	}

}
