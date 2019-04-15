package com.bfd.parse.facade.jspageparse;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.ThreadUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.Constants_TraceTask;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParseStat;
import com.bfd.parse.client.TraceTaskClient;
import com.bfd.parse.facade.IParse;
import com.bfd.parse.test.weibosinaparser.IWeiBoparser;
import com.bfd.parse.test.weibosinaparser.Task;
import com.bfd.parse.test.weibosinaparser.WeiboParserFactory;
import com.bfd.parse.util.KfkUtils;
/**
 * js页面的数据解析，页面里已经有数据不需要ajax数据，比如微博解析
 * @author wenchao.fu
 *
 */
public class JsPageParser implements IParse {
	private static final Log log = LogFactory.getLog(JsPageParser.class);
	private ParseStat stater = new ParseStat(ThreadUtils.getCurrentThreadName());
	private TraceTaskClient traceTask = new TraceTaskClient();
	
	@Override
	public Object parse(String jsonStr) {
		
		return parsePage(jsonStr);
	}
	public static String unzipPageAndGuessEncodeForWeibo(String compressHtml,String url) {
		byte[] data;
		//本地抓过来的都是解压过了的
		try { // 解压缩解密
			data = DataUtil.unzipAndDecode(compressHtml);
		} catch (Exception e) {
			log.warn(Thread.currentThread().getName() + " unzip and Decode data Exception, Err:", e);
			return "";
		}
		// 获取页面编码
		String charset = "UTF8";
		String page = "";
		try {
			page = new String(data, charset);
		} catch (UnsupportedEncodingException e) {
			log.warn(Thread.currentThread().getName() + " new page data string Exception, Err:", e);
			return "";
		}
		return page;
	}
	//jsonstr的数据结构是：{"pageData":{};"reqMap":{}},这个数据结构从对外的接口中组装后传入
	private Map<String, Object> parsePage(String jsonStr){
		
		log.trace("get weibo jsonstr is "+jsonStr);
		Map<String, Object> rs = new HashMap<String, Object>();
//		try {
			Map<String, Object> params = null;
			try {
				params = JsonUtils.parseObject(jsonStr);
			} catch (Exception e) {
				e.printStackTrace();
				rs.put(Constants.parsecode, ParseResult.jsonparseerror);
				Task task = new Task();
				task.setParsedata(rs);
				saveData(task);
				reportStat(task);
				return rs;
			}
//			Map pageData = (Map)params.get("pageData");
//			Map reqMap = (Map)params.get("reqMap");
			Task task = Task.createByPageData(params);
			// 染色得到任务
			traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.rcvtask, JsonUtils.toJSONString(task), 0);
			if(Integer.parseInt(task.getSpiderdata().get(Constants.code).toString())!=0){
				traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.rcvtask, JsonUtils.toJSONString(task), ParseResult.nohtmldata);
				rs.put(Constants.parsecode, task.getSpiderdata().get(Constants.code));
				rs.put("desc", "download error");
				task.setParsedata(rs);
				saveData(task);
				reportStat(task);
				return rs;
			}
//			log.debug("task:"+JsonUtils.toJSONString(task));
			
			log.debug("task taskdata is "+JsonUtils.toJSONString(task.getTaskdata()));
			log.debug("task.get spiderdata get data is null?"+task.getSpiderdata().get("data")==null);
			log.debug("task taskdata get url is null?"+task.getTaskdata().get("url"));
			String html = unzipPageAndGuessEncodeForWeibo(task.getSpiderdata().get("data").toString(), task.getTaskdata().get("url").toString());
//			Pattern p = Pattern.compile("code_change.*?node-type.*?yzm_change");
//			Matcher m = p.matcher(html);
			String regex = "[\\s\\S]*?\\$CONFIG\\['islogin'\\].*?=.*?'0'[\\s\\S]*?";
			if(!html.matches(regex)&&html.contains("$CONFIG['uid']")){
				// 染色
				traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.chklogin, "{\"chklogin\":\"0\"}", 0);
				Map parseRs = null;
				try {
					IWeiBoparser parser = WeiboParserFactory.getParserByType(task.getTaskdata().get("pagetype").toString());
					if (parser == null) {
						traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.chkpagetype, "{\"pagetype\":\"not found pagetype,pagetype:"+task.getTaskdata().get("pagetype")+"\"}", ParseResult.weiboparse_error);
					}
					// 染色
					traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.chkpagetype, "{\"pagetype\":\""+task.getTaskdata().get("pagetype")+"\"}", 0);
					parseRs = parser.parseHtml(html, task);
				} catch (Exception e) {
					e.printStackTrace();
					rs.put("data", parseRs);
					rs.put(Constants.parsecode, ParseResult.weiboparse_error);
					log.warn("url:"+task.getTaskdata().get("url")+" parse error html:"+html);
					task.setParsedata(rs);
					saveData(task);
					reportStat(task);
					// 染色
					traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, "{\"parsehtml\":\""+e.toString()+"\"}", ParseResult.weiboparse_error);
					return rs;
				}
				rs.put("data", parseRs);
				rs.put(Constants.parsecode, 0);
			}else{
				// 染色
				traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.chklogin, "{\"chklogin\":\"cookie expired\"}", ParseResult.cookie_nouse);
				rs.put(Constants.parsecode, ParseResult.cookie_nouse);
				rs.put("desc", "need auth code");
			}
//			if(m.find()||task.getSpiderdata().get("location").toString().indexOf("passport.weibo.com")>0
//					||html.indexOf("$CONFIG['islogin']='0'")>0){
//				
////				return "{\"code\":1,\"desc\":\"need auth code\"}";
////				return rs;
//			}else{
//				
//				
//			}
			log.trace("weibo url:"+task.getTaskdata().get("url")+".html:"+html);
			
			
			task.setParsedata(rs);
			saveData(task);
			reportStat(task);
			// 染色 发送结果
			traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.sendresult, "{\"sendresult\":\"ok\"}", 0);
//		} catch (Exception e) {
//			e.printStackTrace();
		// }
		// 染色 处理结束
		traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parsedone, "{\"parsedone\":\"ok\"}", 0);
		return rs;
	}
	private void saveData(Task task){
		task.setSpiderdata(null);
		log.info("url:"+task.getTaskdata().get("url")+":parseRs:"+JsonUtils.toJSONString(task));
		KfkUtils.sendKfk(task.getTaskdata().get("parsequeuetopic").toString(), JsonUtils.toJSONString(task));
	}
	private void reportStat(Task task){
		stater.increment(task.getTaskdata().get("projectname").toString(),
							task.getTaskdata().get(Constants.cid).toString(), task.getTaskdata().get("pagetype").toString(), (Integer)task.getParsedata().get(Constants.parsecode));
	}
}
