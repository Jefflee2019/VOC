package com.bfd.parse.facade.tmplparse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.ThreadUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.DataSaver;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParseStat;
import com.bfd.parse.ParserFace;
import com.bfd.parse.client.TraceTaskClient;
import com.bfd.parse.facade.IParse;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class TmplParser implements IParse {
	private static final Log LOG = LogFactory.getLog(TmplParser.class);
	private ParserFace parser = new ParserFace(ThreadUtils.getCurrentThreadName());
	private ParseStat stater = new ParseStat(ThreadUtils.getCurrentThreadName());
	private DataSaver dataSaver = new DataSaver(ThreadUtils.getCurrentThreadName());
	private boolean isTest = false;
	@Override
	public Object parse(String jsonStr) {
//		LOG.info("get kafka task:"+jsonStr);
		try {
			Object params = JsonUtils.parseToObject(jsonStr);
			//如果是单个任务就是map,否则如果是批量任务就是list
			if(params instanceof Map){
				return parseTmpl((Map<String, Object>)params);
			}
			else{
				//兼容之前的版本，一条记录有多个任务的情况
				return batchParseTmpl((List<Map<String, Object>>)params);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private List<ParseResult> batchParseTmpl(List<Map<String, Object>> list){
		List<ParseResult> rs = new ArrayList<ParseResult>();
		for(Map<String, Object> item : list){
			rs.add(parseTmpl(item));
		}
		return rs;
	}
	private ParseResult parseTmpl(Map<String, Object> params){
		long time = System.currentTimeMillis(); 
		ParseUnit unit = ParseUnit.fromMap(params, time);
		
		LOG.debug("get parseUnit taskdata is "+JsonUtils.toJSONString(unit.getTaskdata()));
		return parseByUnit(unit);

	}
	
	

	private ParseResult parseByUnit(ParseUnit unit ){
		if (!unit.downloadSuccess()) { // TODO
			ParseResult result = ParseResult.prepareObj(unit);
			ParseResult.ParseData parsedata = result.getParsedata();
			parsedata.setParsecode(ParseResult.PARSECODE_DWONLOADFAILED);
			parsedata.setEndtime(new Date().getTime());
			LOG.info(ThreadUtils.getCurrentThreadName() + " download httpcode=" + unit.getHttpcode() + ", will skip.  url="
					+ unit.getUrl());
			saveResult(unit, result, isTest);
			return result;
		}
		LOG.info(ThreadUtils.getCurrentThreadName() + " download httpcode=" + unit.getHttpcode() + "  url="+ unit.getUrl()+".type:"+unit.getPageType());
		if (StringUtils.isEmpty(unit.getPageType()) || unit.getPageType().toLowerCase().endsWith("img")) {
			LOG.info(ThreadUtils.getCurrentThreadName() +" url:"+unit.getUrl()+ " parse type=" + unit.getPageType() + ", will skip");
			ParseResult ps = ParseResult.prepareObj(unit);
			saveResult(unit, ps, isTest);
			return ps;
		}
		// parse data
		String url = (String) unit.getTaskdata().get("url");
		LOG.info(ThreadUtils.getCurrentThreadName() + " parsing parseunit, url -> " + url+""	);
		ParseResult res = parser.parse(unit);
		res.getParsedata().setEndtime(System.currentTimeMillis());
		//写入统计队列和保存返回队列
		saveResult(unit, res, isTest);
		LOG.info(ThreadUtils.getCurrentThreadName()  + " parsed parseunit, url -> " + url);
		return res;
	}
	
	private void saveResult(ParseUnit unit ,ParseResult result,boolean isTest){
		if (!isTest) {
			dataSaver.saveData(unit, result);
			stater.increment(unit.getProjectname(), unit.getCid(),
					unit.getPageType(), result.getParsedata().getParsecode());
			
		}
	}
	
	public static void main(String[] args) {
		ParseResult ps = new ParseResult();
		ps.setParsedata(null);
		String str = JsonUtils.toJSONString(ps);
		Object o;
		try {
			o = JsonUtils.parseToObject(str);
			System.out.println((o instanceof Map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
