package com.bfd.parse.test.weibosinaparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.StringUtil;
import com.bfd.parse.Constants_TraceTask;
import com.bfd.parse.ParseResult;
import com.bfd.parse.client.TraceTaskClient;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;


public abstract class AWeiBoParser implements IWeiBoparser{
	private static final Log LOG = LogFactory.getLog(AWeiBoParser.class);
	public static Pattern p = Pattern.compile("\\burl=(.*?)&");
	public static Pattern getNumP = Pattern.compile("(\\d+)");
	public static String cookie = "UOR=qiusuoge.com,widget.weibo.com,login.sina.com.cn; SINAGLOBAL=2940834093009.452.1442541021780; ULV=1457783710879:43:4:2:5164094827468.886.1457783710876:1457322140801; SUHB=0d4_Y8DYHWxt92; myuid=1661429500; wb_feed_unfolded_1661429500=1; un=15110295667; wvr=6; YF-Ugrow-G0=56862bac2f6bf97368b95873bc687eef; SUS=SID-1661429500-1457783763-JA-7k90q-7a238de4cd86ae225d111ae9b804adff; SUE=es%3D77bdad57ec5125b16c4172bd427c77c5%26ev%3Dv1%26es2%3D4ac1455a5ba400cfb3a340499f9bead5%26rs0%3Dyl1nHUF8QpeQVNiB2AvmLGVfaXaMuC9HPTkiZFCALGy3hljVA%252BsIXoZIhPXWHg2d%252BlTceilQupteAgiiU6Uz7hnAk%252BIt0PpNTn7VclRSHM8JY7%252FtDN%252FW7Qb22ZJCi5iD7BVqxJ3%252FK9rYfyMBNNzl4om%252F%252Fnj3ah0u3fU18gJ9iHg%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1457783763%26et%3D1457870163%26d%3Dc909%26i%3Dadff%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2015-12-26%252022%253A12%253A10; SUB=_2A2574HODDeRxGedI7VMV8ifJyzyIHXVYlOJLrDV8PUNbvtAMLRHikW9LHesvTFY1_zgKyZaOpQrZfzur_x4EvQ..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; ALF=1489319760; SSOLoginState=1457783763; YF-V5-G0=572595c78566a84019ac3c65c1e95574; _s_tentry=login.sina.com.cn; Apache=5164094827468.886.1457783710876; wb_publish_vip_1661429500=1; YF-Page-G0=206250b160696bcef4885d60544c84d5";
//	public static String cookie = "SUS=SID-3800734699-1454398315-JA-bwyx5-6d008c5c4b7e569b04b1e1aa73b90380; path=/; domain=.weibo.com; httponly;SUE=es%3D5a7b1c0311549778732b34b8f6624a5d%26ev%3Dv1%26es2%3D63630a46f254ebafce13f95e2deb36bd%26rs0%3D3Ub8402DNlsVBH%252FXiUIWnMgTFOBjLqMm%252BLU2XqzC7kAFcNLUdRnvAj2%252BI4NmlFQO3Vhnt326ZDMWwbIf1Ft%252FoAZncyeXKOy715Laxc4endSXO6SI9%252F3pAZmOih6EF1ft10HE7iIVvNSB4gtbNVgi32wuNJD9C7%252FO733qlgpZM8g%253D%26rv%3D0;path=/;domain=.weibo.com;Httponly;SUP=cv%3D1%26bt%3D1454398315%26et%3D1454484715%26d%3Dc909%26i%3Ddc8b%26us%3D1%26vf%3D2%26vt%3D1%26ac%3D18%26st%3D0%26uid%3D3800734699%26name%3Dfei348282%2540163.com%26nick%3D%25E6%25AD%25A5%25E8%258D%25A3%25E5%25A8%259F%25E5%25BA%25BE%26fmp%3D%26lcp%3D2015-07-22%252003%253A41%253A53;path=/;domain=.weibo.com;SUB=_2A257tCs7DeRxGeVG61IW8yrKwjWIHXVYwBvzrDV8PUNbuNANLRWhkW9LHetLIQ9hk0eysfrUaHlwXT7WJTeGqA..; path=/; domain=.weibo.com; httponly;SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhK0UdKOsDW2HKj.Eo1G1vs5JpX5K2t; expires=Wednesday, 01-Feb-17 07:31:55 GMT; path=/; domain=.weibo.com;SUHB=0D-16QEfaORltE; expires=Wednesday, 01-Feb-17 07:31:55 GMT; path=/; domain=.weibo.com;ALF=1485934315; expires=Wednesday, 01-Feb-2017 07:31:55 GMT; path=/; domain=.weibo.com";
	public static String refer = "http://weibo.com";
	public static Pattern numurlP = Pattern.compile("^/\\d+$");
	// 染色
	private TraceTaskClient traceTask = new TraceTaskClient();
	
	public static String getUrl(Task task){
		return task.getTaskdata().get("url")==null?task.getUrl():task.getTaskdata().get("url").toString();
	}
	public String formatUserParsedUrl(String url){
		if(url.startsWith("http://")){
			return url;
		}else if(url.startsWith("/p/")){
			return "http://weibo.com"+url;
		}else if(url.startsWith("/u")){
			return "http://weibo.com"+url;
		}else{
			if(numurlP.matcher(url).find()){
				return "http://weibo.com/u"+url;
			}else{
				return "http://weibo.com"+url;
			}
			
		}
	}
	@Override
	public Map<String, Object> parseHtml(String html, Task task) throws Exception {
//		LOG.info( task.getTaskdata().get("url")+" html:"+html);
		String fragmentHtmlStr = getFragmentHtml(html,task);
		
		String fragmentHtml = Utils.getHtml(fragmentHtmlStr);
		LOG.info(task.getTaskdata().get("url")+" fragmenthtml:"+fragmentHtml);
		if(fragmentHtml.equals("")){
//			LOG.info(task.getTaskdata().get("url")+" get fragmentHtml error!");
			// 染色
			traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.fragmentHtml, "{\"fragmentHtml\":\"\"}", ParseResult.weiboparse_error);
			return new HashMap<String, Object>();
		}
		// 染色
		traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.fragmentHtml, "{\"fragmentHtml\":\"ok\"}", 0);
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(fragmentHtml);
		
		Map rs = executeParse(root,task);
		// 染色
		traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parsehtml, JsonUtils.toJSONString(rs), 0);
		if(task!=null&&task.getSpiderdata()!=null&&task.getSpiderdata().get("ajaxdata")!=null){
			List<Map<String, Object>> ajaxData = (List<Map<String, Object>>)task.getSpiderdata().get("ajaxdata");
//			LOG.info(getUrl(task)+"ajaxData.size():"+ajaxData.size());
			traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.chkAjaxData, "{\"chkAjaxData\":\"ajaxData size "+ajaxData.size()+"\"", 0);
			if(ajaxData.size()>0){
				List<JsonData> jsonDatas = formatAjaxData(ajaxData, task, rs);
				
				for(JsonData data : jsonDatas){
					if (!data.downloadSuccess()) {
						continue;
					}
					String json = TextUtil.getUnzipJson(data, "utf-8");

					try {
						if(json.indexOf("[")>=0&&json.indexOf("]")>=0&&(json.indexOf("[")<json.indexOf("{"))){
							json = json.substring(json.indexOf("["), json.lastIndexOf("]")+1);
						}else if(json.indexOf("{")>=0&&json.indexOf("}")>0){
							json = json.substring(json.indexOf("{"), json.lastIndexOf("}")+1);
						}
						executeParseAjax(json,task,rs,data);
					}catch (Exception e) {
						traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parseAjaxData, "{\"parseAjaxData\":\""+e.toString()+"\"", ParseResult.weiboparse_error);
						e.printStackTrace();
						LOG.warn(task.getTaskdata().get("url")+" json :"+json+".url:"+task.getTaskdata().get("url"));
						LOG.warn(
								task.getTaskdata().get("url")+" AMJsonParser exception, taskdata url=" + task.getTaskdata().get("url") + ", jsondata="
										+ json+".jsonUrl :"+data.getUrl(), e);
					}
				}
				traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parseAjaxData, JsonUtils.toJSONString(rs), 0);
			} else {
				traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parseAjaxData, "{\"parseAjaxData\":\"\"", 0);
			}
		} else {
			traceTask.reportTraceTask(task.getTaskdata(), Constants_TraceTask.commonparser, Constants_TraceTask.parseAjaxData, "{\"parseAjaxData\":\"\"", 0);
		}
		rs.put("url", task.getTaskdata().get("url"));
		parseOtherScript(html, rs);
		Utils.addAttr(rs, task);
		LOG.info(getUrl(task)+":rs is "+JsonUtil.toJSONString(rs));
		return rs;
	}
	
	
	//解析其他script的数据，比如微博解析中的得到官微的名称数据，就是在其他script块里面
	public void parseOtherScript(String html , Map<String, String> rs){
		
	}
	//执行ajax数据
	public void executeParseAjax(String jsonDatas,Task task,Map rs,JsonData jsonData) throws Exception{
		
	}
	public List<JsonData> formatAjaxData(List<Map<String, Object>> ajaxDatas,Task task,Map rs){
		List<JsonData> dataList = new ArrayList<JsonData>();
//		List<Map<String, Object>> ajaxDatas = ajaxData;
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
						jsonData.setData(DataUtil.unzipAndDecode((String) data.get(key)));
					} else if (key.equalsIgnoreCase("charset")) {
						jsonData.setCharset((String) data.get(key));
					} else if (key.equalsIgnoreCase("code")) {
						jsonData.setHttpcode(String.valueOf(data.get("code")));
					} else {
						extend.put(key, data.get(key));
					}
				}
				jsonData.setExtend(extend);
				dataList.add(jsonData);
			} catch (Exception e) {
				LOG.warn(task.getTaskdata().get("url")+": upzip and decode ajaxdata exception:", e);
			}
		}
		return dataList;
	}
	public abstract String getFlagStr();
	public String getFragmentHtml(String html,Task task){
//		html = MyStringUtil.loadConvert(html);
//		System.out.println(html);
//		LOG.info(getUrl(task)+".html:"+html);
		Matcher m = getAllHtmlJson.matcher(html);
		String searchRs = "";
//		System.out
//				.println("--------------------------html end!------------------");
		while (m.find()) {
//			LOG.info(m.group(1));
//			LOG.info("------------------------------");
			// 内容
			if (m.group(1).indexOf(getFlagStr()) > 0) {
				searchRs = m.group(1);
			}
		}
		return searchRs;
	}
	public abstract Map<String, Object> executeParse(TagNode root,Task task) throws XPatherException;

	public static int getNums(String str ,Task task){
		String numStr = StringUtil.getRegexGroup(getNumP, str, 1);
		if(numStr.trim().length()==0){
//			LOG.info(task.getTaskdata().get("url")+" error str :"+str);
			return 0;
		}else{
			return Integer.parseInt(numStr);
		}
	}
	
	
	public static Task getTestTask(String url){
		Task task = new Task();
		Map map2 = new HashMap();
		task.setTaskdata(map2);
		map2.put("url", url);
		return task;
	}
	public static void main(String[] args) {
		String str = "(1)";
		LOG.info(getNums(str,null));
		String url = "/u/33432232";
		url = new WeiboParser().formatUserParsedUrl(url);
		LOG.info(url);
	}
	
	public void getUserInfoFromCommRepost(Object[] as , Map<String, Object> map) throws XPatherException{
		List<String> checks = new ArrayList<String>();
		String check = "";
		for(int k=0;k<as.length;k++){
			TagNode aTag = (TagNode)as[k];
			if(k==0){
//				LOG.info("username:url:"+aTag.getAttributeByName("href")+":"+aTag.getText());
				map.put(Constants.USER_URL, formatUserParsedUrl(aTag.getAttributeByName("href")));
				map.put(Constants.username, aTag.getText().toString());
				
				continue;
			}	
			if(aTag.getAttributeByName("title")!=null&&aTag.getAttributeByName("title").trim().length()>0){
//				LOG.info("check1:"+aTag.getAttributeByName("title"));
				check = aTag.getAttributeByName("title");
				
			}else if(aTag.evaluateXPath("i").length>0){
				TagNode img = (TagNode)aTag.evaluateXPath("i")[0];
				check = img.getAttributeByName("title");
//				LOG.info("check2:"+img.getAttributeByName("title"));
			}
			if(check==null||check.equals("")){
				continue;
			}
			if(check.equals("微博个人认证")){
				map.put(Constants.isChecked, 1);
				map.put(Constants.userType, 1);
				continue;
			}else if (check.equals("微博机构认证")){
				map.put(Constants.isChecked, 1);
				map.put(Constants.userType, 2);
				continue;
			}else if(check.equals("微博会员")){
				map.put(Constants.isVIP, 1);
				continue;
			}else if(check.equals("微博达人")){
				map.put(Constants.isdaren, 1);
			}
			checks.add(check);
			check = "";
		}
		if(!map.containsKey(Constants.userType)){
			map.put(Constants.userType, 1);
		}
		map.put(Constants.checks, checks);
		LOG.info("map:"+JsonUtil.toJSONString(map));
	}
	
	public static String getSecodeTime(String time){
		if(time==null||time.trim().length()==0){
			return "";
		}
		String rs = "";
		try {
			rs = time.substring(0, time.length()-3);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("error time is :"+time);
		} 
		return rs;
	}
	
}
