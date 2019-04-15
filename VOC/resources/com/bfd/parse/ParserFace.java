package com.bfd.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.MyStringUtil;
import com.bfd.crawler.utils.ParserException;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.client.DataOperatorClient;
import com.bfd.parse.client.DeduplicatorClient;
import com.bfd.parse.client.LoginManagerClient;
import com.bfd.parse.client.TraceTaskClient;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.FieldDefine.FieldDefineConfig;
import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.config.shelf.EcConfigCache;
import com.bfd.parse.config.sitepageconfig.SitePageConfigCache;
//import com.bfd.parse.data.Website;
//import com.bfd.parse.data.WebsiteMap;
import com.bfd.parse.entity.CreateTaskEntity;
import com.bfd.parse.entity.ECConfigEntity;
import com.bfd.parse.entity.FielddefineEntity;
import com.bfd.parse.entity.SitepageconfigEntity;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.json.JsonParserFactory;
import com.bfd.parse.json.JsonParserNotFound;
import com.bfd.parse.json.JsonParserResult;
//import com.bfd.parse.learn.LearnUnit;
import com.bfd.parse.preprocess.PreProcessor;
import com.bfd.parse.preprocess.PreProcessorFactory;
import com.bfd.parse.preprocess.PreProcessorNotFound;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.reprocess.ReProcessorFactory;
import com.bfd.parse.reprocess.ReProcessorNotFound;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.util.TextUtil;

//import com.bfd.parse.service.Parser;

/**
 * 调用dom解析，插件解析json,插件后处理，保存上报统计信息，保存源数据和解析数据
 * 
 * @author ian
 * 
 */
public class ParserFace {
	private static final Log LOG = LogFactory.getLog(ParserFace.class);
	private static final String WORKNAME = "worker-t";
	private static ParserFace instance = new ParserFace(WORKNAME);
	private String workName;
	private URLNormalizerClient normalizer;
	private TraceTaskClient traceTask = new TraceTaskClient();
	// private ParseStat stater;
	private DataSaver dataSaver;

	private DomParser domParser;

	public ParserFace(String workname) {
		this(workname, new URLNormalizerClient(), false);
	}

	public static ParserFace getInstance() {
		if (instance == null) {
			instance = new ParserFace(WORKNAME);
		}
		return instance;
	}

	private ParserFace(String workName, URLNormalizerClient normalizer,
			boolean test) {
		this.workName = workName;
		if (!test) {
			// this.stater = new ParseStat(workName);
		}
		// this.deduplicator = new DeduplicatorClient(workName);
		// this.downloader = new DownloadClient();

		this.dataSaver = new DataSaver(workName);
		this.normalizer = normalizer;
		this.domParser = new DomParser(workName, normalizer);
		// this.configClient = new ConfigClient();
	}

	public ParseResult parse(ParseUnit unit) {
		return parse(unit, false);
	}

	private boolean executePreProcessor(ParseUnit unit, ParseResult result,
			SitepageconfigEntity sitePage) {
		boolean flag = preprocess(unit, sitePage, result);
		LOG.debug("url:" + unit.getUrl() + " preprocessor parsecode is "
				+ result.getParsedata().getParsecode());
		return flag;
		// if (!flag) {
		// TODO 如果需要登录,现在就不调用登录服务，直接返回需要登录的code就可以
		// if (unit.getPreProcessCode() == ParseResult.needLogin) {
		// if (unit.getTaskdata().containsKey(Constants.userId)) {
		// logger.info("cid:" + unit.getCid() + " userId:"
		// + unit.getTaskdata().get(Constants.userId)
		// + " need call login!");
		// // call loginManager
		// lmClient.callLogin(unit.getCid().toString(), unit
		// .getTaskdata().get(Constants.userId).toString());
		// }
		//
		// }
		// else {
		// TODO 这个需要修改为前处理插件失败的code
		// result.getParsedata().setParsecode(
		// ParseResult.preprocessor_fail);
		// }

		// }
		// return flag;
	}

	private boolean executeWebPageParse(ParseUnit unit, ParseResult result
								,SitepageconfigEntity sitePageConfig) {
		int templateTag = sitePageConfig.getNeedtemplate();
		Map<String, Object> templateRsReport = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("templateTag", templateTag);
		traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chktemplate, JsonUtils.toJSONString(data), 0);
		LOG.debug("url:"+unit.getUrl()+" datatypeishtml?"+unit.dataTypeIsHtml());
		if (unit.dataTypeIsHtml()) {
			try {
				domParser.parse(unit, result);
				LOG.debug("url:" + unit.getUrl()
						+ " template parse code is "
						+ result.getParsedata().getParsecode());
				// logger.info("url:"+unit.getUrl()+".parsers:"+JsonUtil.toJSONString(result.getParsedata().getData()));
				if (result.getParsedata().getParsecode() == ParseResult.FAILED) {
					templateRsReport.put(Constants.errMsg, result.getParsedata().getErrMsg());
					traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, JsonUtils.toJSONString(templateRsReport), ParseResult.FAILED);
					return false;
				}
			} catch (Exception e) {
				LOG.warn(
						workName + " dom parse exception, cid=" + unit.getCid()
								+ ", url=" + unit.getUrl(), e);
				result.getParsedata().setParsecode(ParseResult.FAILED);
				templateRsReport.put(Constants.errMsg, "template parse exception");
				traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, JsonUtils.toJSONString(templateRsReport), ParseResult.FAILED);
				return false;
			}
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, "", 0);
			return true;
		} else {
			JsonParserResult jResult = parseAjaxData(unit,
					result.getTaskdata(), true);
			result.getParsedata().setParsecode(jResult.getParsecode());
			LOG.debug("url:" + unit.getUrl() + " template parse code is "
					+ result.getParsedata().getParsecode());
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, JsonUtils.toJSONString(jResult.getData()), jResult.getParsecode());
			if (jResult.getData() != null) {
				result.getParsedata().addData(jResult.getData());
				return true;
			} else {
				return false;
			}
		}

	}

	private boolean executeJsonParse(ParseUnit unit, ParseResult result,
			SitepageconfigEntity entity) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Jsonprocesstag", entity.getJsonprocesstag());
		data.put("hasajaxdata", unit.hasAjaxData());
		data.put("status", entity.getStatus());
		traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chkjsonplugin, JsonUtils.toJSONString(data), 0);
		// TODO:通过websiteconfig表得到是否需要json插件
		if ( entity.getStatus() == 1
				&& entity.getJsonprocesstag() == 1) {
			if(!unit.hasAjaxData()){
				Map<String, Object> traceData = new HashMap<String, Object>();
				traceData.put("desc", "no ajax data");
				traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.jsonparse, JsonUtils.toJSONString(traceData), 0);
				return true;
			}else{
				LOG.info("url:" + unit.getUrl() + ":need call jsonplugin!");
				JsonParserResult jres = parseAjaxData(unit, result.getTaskdata(),
						false);
				LOG.info("after execute parseAjaxData,url+" + unit.getUrl());
				// 若解析DATA时没有模板，又要解析AjaxData,则认为解析Data操作为成功。
				// if (result.getParsedata().getParsecode() ==
				// ParseResult.FAILED_NO) {
				// result.getParsedata().setParsecode(ParseResult.SUCCESS);
				// }
				result.getParsedata().setParsecode(jres.getParsecode());
				traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.jsonparse, "", jres.getParsecode());
				LOG.debug("url:" + unit.getUrl() + " json parsecode is "
						+ result.getParsedata().getParsecode());
				if (jres.getData() != null) {
					result.getParsedata().addData(jres.getData());
				} else {
					return false;
				}
			}
			
		}
		return true;
	}
	/**
	 * true为在架，false为下架
	 * @param unit
	 * @return
	 */
	public boolean onOffShelf(ParseUnit unit,ParseResult result){
		ECConfigEntity ecConfig = EcConfigCache.getInstance().getECConfig(unit.getCid());
		if(ecConfig==null){
			LOG.debug("url:"+unit.getUrl()+" no ecconfig");
			return true;
		}
		if(ecConfig.getIscontain()!=null
				&&ecConfig.getIscontain().trim().length()>0
				&&MyStringUtil.isRegexMatched(ecConfig.getIscontain(), unit.getPageData())){
			LOG.debug("url:"+unit.getUrl()+" match ecconfig iscontain");
			result.getParsedata().getData().put(Constants.errMsg, "off shelf match iscontain");
			return false;
		}
		if(unit.getSpiderdata().containsKey(Constants.HTTPHEADER_LOCATION)
				&&unit.getSpiderdata().get(Constants.HTTPHEADER_LOCATION).toString().trim().length()>0){
			if(unit.getSpiderdata().get(Constants.HTTPHEADER_LOCATION).toString().trim().equalsIgnoreCase(ecConfig.getRedirecturl())){
				LOG.debug("url:"+unit.getUrl()+" match ecconfig redirect");
				result.getParsedata().getData().put(Constants.errMsg, "off shelf match redirect");
				return false;
			}
		}
		return true;
	}
	/**
	 * 解析数据
	 * 
	 * @param unit
	 *            解析任务单元
	 * @param isTest
	 *            是否为测试任务
	 * @return
	 */
	public ParseResult parse(ParseUnit unit, boolean isTest) {
		traceTask.reportTraceTask(unit, "commonparser", "rcvtask", "",0);
		ParseResult result = ParseResult.prepareObj(unit);

		try {

			// 判断是否有html数据
			if (!StringUtils.isNotEmpty(unit.getData())) {
				LOG.debug("url:" + unit.getUrl() + " no html data");
				result.getParsedata().setParsecode(ParseResult.nohtmldata);
				traceTask.reportTraceTask(unit, "commonparser", Constants_TraceTask.chkhtml, "",ParseResult.nohtmldata);
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" has html data");
			if (!TextUtil.unzipPageAndGuessEncode(unit)) {
				result.getParsedata().setParsecode(ParseResult.uncompress_fail);
				traceTask.reportTraceTask(unit, "commonparser", Constants_TraceTask.chkhtml, "",ParseResult.uncompress_fail);
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" unzipandguessencode success!");
			
			
			if(!onOffShelf(unit,result)){
				LOG.info("url:"+unit.getUrl()+" off shelf");
				result.getParsedata().setParsecode(ParseResult.OFF_SHELF);
				traceTask.reportTraceTask(unit, "commonparser", Constants_TraceTask.chkhtml, "",ParseResult.OFF_SHELF);
				return result;
			}
			traceTask.reportTraceTask(unit, "commonparser", Constants_TraceTask.chkhtml, "",0);
			SitepageconfigEntity sitePageConfig = SitePageConfigCache.getInstance()
					.getSitePageConfig(
							unit.getSiteId() + "|" + unit.getPageTypeId());
			LOG.trace("url:" + unit.getUrl() + " key: " + unit.getSiteId()
					+ "|" + unit.getPageTypeId() + " sitePageConfig:"
					+ JsonUtils.toJSONString(sitePageConfig));

			if (!executePreProcessor(unit, result, sitePageConfig)) {
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" preprocessor success!");
			if (!executeWebPageParse(unit, result,sitePageConfig)) {
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" template parse success!");
			LOG.debug("url:"+unit.getUrl()+" unit.hasAjaxData() :" + (unit.hasAjaxData()));
			//json plugin  
			if (!executeJsonParse(unit, result, sitePageConfig)) {
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" json parse success!");
			Map<String, Object> chkiid = new HashMap<String, Object>();
			chkiid.put("iidtag", sitePageConfig.getIidtag());
			
			// 从taskdata获得attr,cate;获得iid，生成tasks
			if (!addExtraInfo(unit, result)) {
				result.getParsedata().setParsecode(ParseResult.GETIID_FAILED);
				return result;
			}
			LOG.debug("url:"+unit.getUrl()+" addExtraInfo success!");
			executeReprocess(unit, result, sitePageConfig);
			
			if(0==result.getParsedata().getParsecode()){
				traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.sendresult, JsonUtils.toJSONString(result.getParsedata().getData()), 0);
				
				traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.parsedone, "", 0);	
			}

			LOG.info("url is " + unit.getUrl() + " type is "
					+ unit.getPageType() + ".getParsecode is "
					+ result.getParsedata().getParsecode() + ",cid :"
					+ unit.getCid()+" task over!");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.warn(workName + " increment exception, ", e);
		}
		return result;
	}

	private void executeReprocess(ParseUnit unit, ParseResult result,
			SitepageconfigEntity website) {
		LOG.info("url:" + unit.getUrl() + ",parsecode is "
				+ result.getParsedata().getParsecode() + ",plugintag :"
				+ website.getReprocesstag());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Reprocesstag", website.getReprocesstag());
		traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chkproplugin, JsonUtils.toJSONString(data), 0);
		// TODO:改为从websiteconfig表里面得到是否需要后处理插件
		if (result.getParsedata().getParsecode() == ParseResult.SUCCESS
				&& website.getReprocesstag() == 1) { // FIXME
			LOG.info("url:" + unit.getUrl() + " need reprocess plugin");
			ReProcessResult reprocess = reprocess(unit, result);
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.reprocess, "", result.getParsedata().getParsecode());
			if (reprocess != null) {
				switch (reprocess.getProcesscode()) {
//				case ReProcessResult.PARSE_FAILED:
				case ParseResult.REPROCESS_FAILED:
					LOG.warn(workName + " reprocess code 2, cid="
							+ unit.getCid() + ", type=" + unit.getPageType()
							+ ", url=" + unit.getUrl());
					result.getParsedata().setParsecode(
							ParseResult.REPROCESS_FAILED);
					break;
				case ReProcessResult.NONESAVE:// 若后处理的code为3则，不掉用保存，状态码为-2,xici的情况
					LOG.info(workName + " reprocess code 3, cid="
							+ unit.getCid() + ", type=" + unit.getPageType()
							+ ", url=" + unit.getUrl());
					result.getParsedata().setParsecode(
							ParseResult.REPROCESS_NONESAVE);
					break;
				case ReProcessResult.OFF: // 后处理下架
					LOG.info(workName
							+ " reprocess code 4, onshelf off, cid="
							+ unit.getCid() + ", type=" + unit.getPageType()
							+ ", url=" + unit.getUrl());
					result.getParsedata().setParsecode(ParseResult.OFF_SHELF);
					break;
				case ReProcessResult.SUCCESS:
					LOG.info(workName
							+ " reprocess code 0, reprocess success, cid="
							+ unit.getCid() + ", type=" + unit.getPageType()
							+ ", url=" + unit.getUrl());
					result.getParsedata().setParsecode(ParseResult.SUCCESS);
					break;
				}
			}
		}
	}

	private boolean addExtraInfo(ParseUnit unit, ParseResult result) {
		// 处理cate
		final Map<String, Object> resData = result.getParsedata().getData();

		// 处理attr
		Map attr = (Map) unit.getAttr();
		if (attr != null && attr.size() > 0) {
			// 将task带过来的attr放入解析结果
			resData.put("attr", unit.getAttr());
		}
		this.domParser.handExtraArgs(unit,  unit.getPageEncode()
				, null, resData);
		// 获取iid
		if (!ParseUtils.getIid(unit, result)) {

			LOG.debug("url is " + unit.getUrl()
					+ ".getiid failed");
			result.getParsedata().setParsecode(ParseResult.GETIID_FAILED);
			return false;
		}
		
		return true;
	}

	public static boolean urlHasNoPathAndQuery(String location) {

		return false;
	}

	// TODO:得到需要生成任务的字段,现在createTask字段已经从数据库表移到了模板里面，此函数弃用
	// private List<Fielddefine> getCreateTaskField(ParseUnit unit) {
	// List<Fielddefine> rs = new ArrayList<Fielddefine>();
	// Fielddefine field =
	// FieldDefineConfig.getInstance().getFieldDefine(unit.getPageTypeId());
	// Fielddefine tmp = field;
	// while(tmp!=null){
	// if(tmp.getCreatetask()==1){
	// rs.add(tmp);
	// }
	// tmp = (Fielddefine)tmp.getNext();
	// }
	//
	//
	// // List<Fielddefine> fields = this.configClient
	// // .getFieldDefineByPageTypeId(Integer.parseInt(unit.getPageType()));
	// // for (Fielddefine field : fields) {
	// // if (field.getCreateTask() == 1) {
	// // rs.add(field);
	// // }
	// // }
	// return rs;
	// }

	// TODO:通过cid和pageTypeId得到iid规则
//	private String getIidRegex(ParseUnit unit,String pageTypeId) {
//		String cacheKey = unit.getSiteId() + "|" + pageTypeId;
//		Sitepageconfig config = SitePageConfigCache.getInstance()
//				.getSitePageConfig(cacheKey);
//		if (config == null) {
//			logger.debug("url:" + cacheKey + " get config null");
//			return "";
//		}
//		if (config.getIidtag() == 1) {
//			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.chkiidrule, "", 0);
//			return config.getIidregex();
//		} else {
//			logger.debug("url:" + cacheKey + " iidtag is 0");
//			return "";
//		}
//
//	}

	// TODO:遍历parsedata找到所有的key，得到所有的需要生成任务的字段
	// private void getAllCreateTasks(List<String> createTaskFieldNames,
	// List<CreateTaskEntity> tasks, Map<String, Object> parsedata) {
	// Iterator<Entry<String, Object>> it = parsedata.entrySet().iterator();
	// while (it.hasNext()) {
	// Entry<String, Object> entry = it.next();
	// if (createTaskFieldNames.contains(entry.getKey())) {
	// CreateTaskEntity task = new CreateTaskEntity();
	// Map<String, Object> value = (Map<String, Object>) entry
	// .getValue();
	// task.setLink(value.get("link").toString());
	// task.setLinkType(value.get("lineType").toString());
	// tasks.add(task);
	// continue;
	// } else {
	// if (entry.getValue() instanceof Map) {
	// getAllCreateTasks(createTaskFieldNames, tasks,
	// (Map<String, Object>) entry.getValue());
	// }
	// }
	// }
	// }

	
	
	

	private boolean preprocess(ParseUnit unit, SitepageconfigEntity sitePageConfig,
			ParseResult result) {
		// SitePageConfigEntity sitePageConfig =
		// SitePageConfig.getInstance().getSitePageConfig(unit.getSiteId()+"|"+unit.getPageTypeId()+"|"+Constants.pluginType_preprocess);
		Map<String, Object> precessdata = new HashMap<String, Object>();
		try {
			// TODO:通过siteId，pageTypeId，pluginType来得到插件
			PreProcessor preProcessor = PreProcessorFactory.getPreProcessor(
					unit.getSiteId()+"", unit.getPageTypeId()+"");
			if (preProcessor != null) {
				LOG.info(workName + " find preprocess plugin, cid="
						+ unit.getCid() + ", type=" + unit.getPageType()
						+ " url:" + unit.getUrl());
				if (!preProcessor.process(unit, this)) {
					precessdata.put(Constants.errMsg, "process error!");
					
					if(unit.getPreProcessCode()!=0){
						result.getParsedata().setParsecode( unit.getPreProcessCode());
					}else {
						result.getParsedata().setParsecode(ParseResult.preprocessor_fail);
					}
					traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, 
							Constants_TraceTask.preprocess, JsonUtils.toJSONString(precessdata),ParseResult.preprocessor_fail);
					return false;
				} else {
					traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.preprocess, "", 
							0);
					return true;
				}
			}
			precessdata.put(Constants.errMsg, "siteID or pageTypeId is null");
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.preprocess, JsonUtils.toJSONString(precessdata), 
					ParseResult.NOFOUND_PREPROCESSOR);
			LOG.info(workName + " preprocess plugin not found, cid="
					+ unit.getCid() + ", type=" + unit.getPageType() + " url:"
					+ unit.getUrl());
			result.getParsedata()
					.setParsecode(ParseResult.NOFOUND_PREPROCESSOR);
			return false;
		} 
		catch (PreProcessorNotFound e) {
			
			LOG.warn(
					workName + " preprocess plugin not found, cid="
							+ unit.getCid() + ", type=" + unit.getPageType(), e);
			precessdata.put(Constants.errMsg, "throw preprocessorNotFound");
			traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.preprocess, JsonUtils.toJSONString(precessdata), 
					ParseResult.NOFOUND_PREPROCESSOR);
			return false;

		} 
		catch (Exception e) {
			LOG.warn(
					workName + " preprocess exception, , cid=" + unit.getCid()
							+ ", type=" + unit.getPageType(), e);
		}
		precessdata.put(Constants.errMsg, "process throw exception");
		result.getParsedata().setParsecode(ParseResult.preprocessor_fail);
		traceTask.reportTraceTask(unit, Constants_TraceTask.commonparser, Constants_TraceTask.preprocess, JsonUtils.toJSONString(precessdata), 
				ParseResult.preprocessor_fail);
		return false;
	}

	/**
	 * 调用后处理插件,进行后处理
	 */
	private ReProcessResult reprocess(ParseUnit unit, ParseResult result) {
		try {
			// TODO:改为siteId pagetypeId pluginType得到插件
			ReProcessor reProcessor = ReProcessorFactory.getReProcessor(
					unit.getSiteId()+"", unit.getPageTypeId()+"");
			if (reProcessor != null) {
				LOG.info(workName + " find reprocess plugin, cid="
						+ unit.getCid() + ", type=" + unit.getPageType());
				return reProcessor.process(unit, result, this);
			}
			LOG.debug(workName + " reprocess plugin not found, cid="
					+ unit.getCid() + ", type=" + unit.getPageType()
					+ ".url is " + unit.getUrl());
		} catch (ReProcessorNotFound e) {
			e.printStackTrace();
			LOG.warn(
					workName +" url:"+unit.getUrl()+ "  reprocess plugin not found, cid="
							+ unit.getCid() + ", type=" + unit.getPageType(), e);
			result.getParsedata().setParsecode(ParseResult.nofound_reprocessor);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("url:"+unit.getUrl()+" get reprocess error!");
			result.getParsedata().setParsecode(ParseResult.REPROCESS_FAILED);
		}catch (Throwable e) {
			LOG.error("url:"+unit.getUrl()+" runtime error");
			e.printStackTrace();
			result.getParsedata().setParsecode(ParseResult.REPROCESS_FAILED);
		}
		
		return null;
	}

	/**
	 * 调用对应插件，解析JSON数据
	 */
	// TODO:这个得到json插件的方法也需要修改，siteId pagetypeId plugintype
	private JsonParserResult parseAjaxData(ParseUnit unit,
			Map<String, Object> taskData, boolean inData) {

		List<JsonData> dataList = TextUtil.wrapJsonData(unit, inData);
		try {
			return JsonParserFactory.getJsonParser(unit, inData, workName)
					.parse(taskData, dataList, this.normalizer, unit);
		} catch (JsonParserNotFound e) {
			e.printStackTrace();
			LOG.warn(
					workName + " JsonParser not found exception "
							+ unit.getUrl(), e);
			return new JsonParserResult(ParseResult.nofound_jsonprocessor, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.warn(
					workName + " JsonParser execute exception " + unit.getUrl(),
					e);
			return new JsonParserResult(ParseResult.jsonprocess_FAILED, null);
		}

	}

	public String getWorkName() {
		return workName;
	}

	public URLNormalizerClient getNormalizerClient() {
		return normalizer;
	}

//	public DataOperatorClient getDataOperatorClient() {
//		return dataSaver.getDataOperatorClient();
//	}

	// public StatServiceClient getStatServiceClient() {
	// return stater.getStatServiceClient();
	// }

	// public DownloadClient getDownloadClient() {
	// return downloader;
	// }

	public DataSaver getDataSaver() {
		return dataSaver;
	}

	public DomParser getDomParser() {
		return domParser;
	}

	public static void main(String[] args) {
//		boolean res = urlHasNoPathAndQuery("http://www.baidu.com;dd");
//		System.out.println(res);
//		try {
//			String iid = MyStringUtil.getRegexGroup("http\\://k\\.autohome\\.com\\.cn/spec/\\d+/view_(\\d+)_\\d+\\.html(\\?.+)?", "http://k.autohome.com.cn/spec/18322/view_401054_1.html?st=1&piap=0|3217||0|1|0|0|0|0|0"
//					.toString(), 1);
//			System.out.println(iid);
//		} catch (ParserException e) {
//			e.printStackTrace();
//		}
		
	}
}
