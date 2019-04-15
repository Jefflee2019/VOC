package com.bfd.parse;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.json.JsonParser;
import com.bfd.parse.json.JsonParserResult;
import com.bfd.parse.preprocess.PreProcessor;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 只用来测试三种插件，所有的状态判断和插件动态加载的代码都已经去掉。
 * @author wenchao.fu
 *
 */
public class ParseTestForPlugin {
	private static final Log logger = LogFactory.getLog(ParseTestForPlugin.class);
	public static final String workName = "test";
	private boolean preprocess(ParseUnit unit, ParserFace parserFace
												,PreProcessor preProcessor) {
		try {
			if (!TextUtil.unzipPageAndGuessEncodeForPluginTest(unit)) {
				return false;
			}
			if (preProcessor != null) {
				logger.info(workName + " find preprocess plugin, cid="
						+ unit.getCid() + ", type=" + unit.getPageType());
				return preProcessor.process(unit, parserFace);
			}else{
				logger.info(workName + " preprocess plugin is null, cid="
						+ unit.getCid() + ", type=" + unit.getPageType());
			}
			
			return true;
		}  catch (Exception e) {
			logger.warn(
					workName + " preprocess exception, , cid=" + unit.getCid()
							+ ", type=" + unit.getPageType(), e);
		}
		return false;
	}
	
	private JsonParserResult parseAjaxData(ParseUnit unit,
			Map<String, Object> taskData, boolean inData,JsonParser jsonParser) {

		List<JsonData> dataList = TextUtil.wrapJsonData(unit, inData);
		try {
			//归一化服务在测试插件的时候没有用到，就传入null吧
			return jsonParser.parse(taskData, dataList, null, unit);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new JsonParserResult(ParseResult.nofound_jsonprocessor, null);
	}
	
	//用于本地调试插件
		public ParseResult parseTest(ParseUnit unit,PreProcessor preProcessor
						,JsonParser jsonParser,ReProcessor reprocessor){
			ParserFace parserFace = new ParserFace("test");
			DomParser domParser = new DomParser("test",new URLNormalizerClient());
			// logger.info("url:"+unit.getUrl()+".unit is "+JsonUtil.toJSONString(unit));
			ParseResult result = ParseResult.prepareObj(unit);
			
			try {
				// logger.info("cid:"+unit.getCid()+".website : "+JsonUtil.toJSONString(website));
				// 判断网站的插件是否启用

				// 没有ajax数据，或有ajax数据但needParseData标识为true
				if (StringUtils.isNotEmpty(unit.getData())) {
					if (unit.dataTypeIsHtml()) {
						// 预处理
						if (!preprocess(unit, parserFace,preProcessor)) {
							result.getParsedata().setParsecode(
									ParseResult.PARSECODE_DWONLOADFAILED);
						} else {
							try {
								domParser.parse(unit, result);
								logger.info("url:"+unit.getUrl()+",after template parser rs is "
										+JsonUtil.toJSONString(result.getParsedata())
										+" the parse code is "+result.getParsedata().getParsecode());
//								
							} catch (Exception e) {
								logger.warn(workName + " dom parse exception, cid="
										+ unit.getCid() + ", url=" + unit.getUrl(),
										e);
								result.getParsedata().setParsecode(
										ParseResult.FAILED);
							}
						}
					} else  {
						JsonParserResult jResult = parseAjaxData(unit,
								result.getTaskdata(), true,jsonParser);
						result.getParsedata()
								.setParsecode(jResult.getParsecode());
						if (jResult.getData() != null) {
							result.getParsedata().addData(jResult.getData());
						}
					}
				}

				logger.info("unit.hasAjaxData() :"+(unit.hasAjaxData() ));
				if (unit.hasAjaxData()) {
					JsonParserResult jres = parseAjaxData(unit,
							result.getTaskdata(), false,jsonParser);
					// 若解析DATA时没有模板，又要解析AjaxData,则认为解析Data操作为成功。
					if (result.getParsedata().getParsecode() == ParseResult.FAILED) {
						result.getParsedata().setParsecode(ParseResult.SUCCESS);
					}
					result.getParsedata().setParsecode(jres.getParsecode());
					if (jres.getData() != null) {
						result.getParsedata().addData(jres.getData());
					}
				}
				logger.info("url:"+unit.getUrl()+",after json parser rs is "+JsonUtil.toJSONString(result.getParsedata()));
				executeReprocessTest(unit, result, parserFace,reprocessor);
				logger.info("url:"+unit.getUrl()+" the parse code is "+result.getParsedata().getParsecode()
						+" after reprocess rs is "+JsonUtil.toJSONString(result.getParsedata().getData()));
			} catch (Exception e) {
				logger.warn(workName + " increment exception, ", e);
			}
			return result;
		
		}
		
		private void executeReprocessTest(ParseUnit unit,ParseResult result
								,ParserFace parserFace,ReProcessor reprocessor){
			if (result.getParsedata().getParsecode() == ParseResult.SUCCESS) { // FIXME
				ReProcessResult reprocess = reprocessor.process(unit, result, parserFace);
				logger.info("after execute reprocess");
				if (reprocess != null) {
					switch (reprocess.getProcesscode()) {
//					case ReProcessResult.PARSE_FAILED:
					case ParseResult.REPROCESS_FAILED:
						logger.warn(workName + " reprocess code 2, cid="
								+ unit.getCid() + ", type=" + unit.getPageType()
								+ ", url=" + unit.getUrl());
						result.getParsedata().setParsecode(
								ParseResult.REPROCESS_FAILED);
						break;
					case ReProcessResult.NONESAVE:// 若后处理的code为3则，不掉用保存，状态码为-2,xici的情况
						logger.info(workName + " reprocess code 3, cid="
								+ unit.getCid() + ", type=" + unit.getPageType()
								+ ", url=" + unit.getUrl());
						result.getParsedata().setParsecode(
								ParseResult.REPROCESS_NONESAVE);
						break;
					case ReProcessResult.OFF: // 后处理下架
						logger.info(workName
								+ " reprocess code 4, onshelf off, cid="
								+ unit.getCid() + ", type=" + unit.getPageType()
								+ ", url=" + unit.getUrl());
						result.getParsedata().setParsecode(ParseResult.OFF_SHELF);
						break;
					case ReProcessResult.SUCCESS:
						logger.info(workName
								+ " reprocess code 0, reprocess success, cid="
								+ unit.getCid() + ", type=" + unit.getPageType()
								+ ", url=" + unit.getUrl());
						result.getParsedata().setParsecode(ParseResult.SUCCESS);
						break;
					}
				}
			}
		}
		
		public static void main(String[] args) {}
}
