package com.bfd.parse.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.MyStringUtil;
import com.bfd.crawler.utils.ParserException;
import com.bfd.parse.Constants;
import com.bfd.parse.Constants_TraceTask;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.client.TraceTaskClient;
import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.config.sitepageconfig.SitePageConfigCache;
import com.bfd.parse.entity.SitepageconfigEntity;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class ParseUtils {
	private static final Log logger = LogFactory.getLog(ParseUtils.class);
	private static TraceTaskClient traceTask = new TraceTaskClient();
	/**
	 * 来了一个任务，知道了这个任务的页面类型，遍历这个页面类型的每个字段
	 * ，取得需要生成任务的字段（即查询FieldDefine表里面的createTask字段），从解析结果中得到这个字段，
	 * 这个字段的数据结构肯定是{"link":"";"linkType":""}然后通过cid加pageTypeId去
	 * sitepageConfig表去查iid规则；结构 {"link":"";"linkType":""}中增加iid;然后将这些map
	 * 结构放到名为tasks的字段里面，表示tasks里面的url都是要在返回生成新任务的。
	 * 
	 * @param unit
	 * @param result
	 * @return
	 */
	public static boolean getIid(ParseUnit unit, ParseResult result) {
		if(!result.getParsedata().getData().containsKey(Constants.tasks)){
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chkiidrule, "{\"iidtag\":0}", 0);
			return true;
		}
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) result
				.getParsedata().getData().get(Constants.tasks);
		logger.info("url:"+unit.getUrl()+" tasks size "+tasks.size());
		boolean reportChkiid = false;
//		boolean siteconfigIsNull = false;
		String errMsg = "";
		int getIidSucess = 0;
		int getIidFail = 0;
		List<Map<String, Object>> getIidTasks = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> task : tasks) {
			
			String pageTypeId = PageDefineConfig.getInstance().getIdByName(task.get(Constants.linktype).toString());
			logger.info("url:"+unit.getUrl()+" linktype:"+task.get(Constants.linktype)+" pagetypeId:"+pageTypeId);
			String cacheKey = unit.getSiteId() + "|" + pageTypeId;
			SitepageconfigEntity sitePageConfig = SitePageConfigCache.getInstance().getSitePageConfig(cacheKey);
			if(urlFilter(task.get(Constants.link).toString(), sitePageConfig)){
				logger.info("url:"+unit.getUrl()+" has urlfilter. "+task.get(Constants.link)+" "+sitePageConfig.getUrlrule()+" "+sitePageConfig.getDetailrule());
				continue;
			}
			if (sitePageConfig == null) {
				errMsg = "cacheKey:"+cacheKey+" no Sitepageconfig pagetype:"+task.get(Constants.linktype);
				logger.debug("cachekey:" + cacheKey + " get config null");
				getIidFail++;
				continue;
			}
			logger.info("url:"+unit.getUrl()+" cachekey:"+cacheKey+" sitepageconfig:"+JsonUtils.toJSONString(sitePageConfig));
			if(sitePageConfig.getIidtag()==1){
				logger.info("url:"+unit.getUrl()+",task cachekey:"+cacheKey+" iid tag is 1.reportchkiid :"+reportChkiid);
				
				reportChkiid = true;
				String iidRegex = sitePageConfig.getIidregex();
				if(iidRegex==null||iidRegex.trim().length()==0){
					getIidFail++;
					continue;
				}
				try {
					task.put(Constants.IID, 
							MyStringUtil.getRegexGroup(iidRegex, task.get(Constants.link).toString(), 1));
					getIidTasks.add(task);
					getIidSucess++;
				} catch (ParserException e) {
					logger.error("get iid error iidrule is :" + iidRegex + ",url:"
							+ task.get("link"));
					getIidFail++;
					errMsg += "; url:"+task.get("link")+" ParseException ";
					e.printStackTrace();
				}
			}else{
				task.put(Constants.IID, DataUtil.calcMD5(task.get(Constants.link)+""));
				getIidTasks.add(task);
				getIidSucess++;
			}
		}
		
		result.getParsedata().getData().put(Constants.tasks, getIidTasks);
		Map<String, Object> chkiidReportData = new HashMap<String, Object>();
		
		if(reportChkiid){
			logger.info("url:"+unit.getUrl()+" report chkiidtag");
			chkiidReportData.put(Constants.iidtag, "1");
			
		}else{
			chkiidReportData.put(Constants.iidtag, "0");
		}
		traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chkiidrule, JsonUtils.toJSONString(chkiidReportData), 0);
		
		if(reportChkiid){
			Map<String, Object> iidReprocessReportData = new HashMap<String, Object>();
			iidReprocessReportData.put(Constants.getiidfail, getIidFail);
			iidReprocessReportData.put(Constants.getiidsuccess, getIidSucess);
			iidReprocessReportData.put(Constants.errMsg, errMsg);
			logger.info("url:"+unit.getUrl()+" report iidprocess");
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.iidprocess, JsonUtils.toJSONString(iidReprocessReportData), 0);
		}
		return true;
	}
	
	/**
	 * url过滤
	 * @param url
	 * @param config
	 * @return true表示这个url被过滤掉。
	 */
	private static boolean urlFilter(String url , SitepageconfigEntity config){
		if(config.getUrlrule()==0){
			return false;
		}
		if(config.getUrlrule()==1 
				&& url.contains(config.getDetailrule())){
			return false;
		}
		if(config.getUrlrule()==2 
				&& !url.contains(config.getDetailrule())){
			return false;
		}else if(config.getUrlrule()==3 
				&& MyStringUtil.isRegexMatched(config.getDetailrule(), url)){
			return false;
		}
		return true;
	}
	public static void main(String[] args) {
		SitepageconfigEntity config = new SitepageconfigEntity();
		config.setUrlrule(null);
		config.setDetailrule("sina");
		ParserFace face = new ParserFace("");
		boolean flag = urlFilter("http://www.sina.com", config);
		System.out.println(flag);
	}
}
