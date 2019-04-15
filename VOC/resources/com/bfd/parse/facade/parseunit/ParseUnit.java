package com.bfd.parse.facade.parseunit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.Constants;


public class ParseUnit {

	private static final Log LOG = LogFactory.getLog(ParseUnit.class);

	// request data
	private String cid;
	private int siteId;
	private int pageTypeId;
//	private String bfdiid;
	private String url;
	private String pageType;
	private String data;
	private String httpcode;
	private int length;
	private int pageidx;
	private String datatype;
//	private String ajaxdatatype;
	private String projectname;
//	private Object cate;
	private Object attr;
	private byte[] pageBytes;
	private String pageEncode;
	private String pageData;
	private String taskId;
	private int traceflag;
	

	// optional data
	private String charset;
	private String iid = "";
	private String purl = "";
	private String url0 = "";
	private Integer startTime;
	private List<Map<String, Object>> ajaxdata;
	private long begintime = 0L;

	private Map<String, Object> taskdata;
	private Map<String, Object> spiderdata;

	private boolean sameMd5 = false;

//	private int parsetype; // 0-> tmpl, 1->auto
	
	private int preProcessCode;
	
	private String writeToKfkName;
	

	public int getTraceflag() {
		return traceflag;
	}

	public void setTraceflag(int traceflag) {
		this.traceflag = traceflag;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public int getPageTypeId() {
		return pageTypeId;
	}

	public void setPageTypeId(int pageTypeId) {
		this.pageTypeId = pageTypeId;
	}

	public String getWriteToKfkName() {
		return writeToKfkName;
	}

	public void setWriteToKfkName(String writeToKfkName) {
		this.writeToKfkName = writeToKfkName;
	}

	public int getPreProcessCode() {
		return preProcessCode;
	}

	public void setPreProcessCode(int preProcessCode) {
		this.preProcessCode = preProcessCode;
	}

	public ParseUnit(String cid, String url, String type, String data) {
		this.cid = cid;
		this.url = url;
		this.pageType = type;
		this.data = data;
	}

	public ParseUnit() {
	}
	//某个网站返回500，需要通过返回的内容判断上下架
	public boolean downloadSuccess() {
		if ("0".equals(this.httpcode)) {
			return true;
		}
		return false;
	}

	public void setSameMd5(boolean sameMd5) {
		this.sameMd5 = sameMd5;
	}

	public boolean isSameMd5() {
		return sameMd5;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String type) {
		this.pageType = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getHttpcode() {
		return httpcode;
	}

	public void setHttpcode(String httpcode) {
		this.httpcode = httpcode;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

//	public String getBfdiid() {
//		return bfdiid;
//	}
//
//	public void setBfdiid(String bfdiid) {
//		this.bfdiid = bfdiid;
//	}

	public String getIid() {
		return iid;
	}

	public void setIid(String iid) {
		this.iid = iid;
	}

	public String getPurl() {
		return purl;
	}

	public void setPurl(String purl) {
		this.purl = purl;
	}

	public String getUrl0() {
		return url0;
	}

	public void setUrl0(String url0) {
		this.url0 = url0;
	}

	public List<Map<String, Object>> getAjaxdata() {
		return ajaxdata;
	}

	public void setAjaxdata(List<Map<String, Object>> ajaxdata) {
		this.ajaxdata = ajaxdata;
	}

	public Map<String, Object> getTaskdata() {
		return taskdata;
	}

	public void setTaskdata(Map<String, Object> taskdata) {
		this.taskdata = taskdata;
	}

	public Map<String, Object> getSpiderdata() {
		return spiderdata;
	}

	public void setSpiderdata(Map<String, Object> spiderdata) {
		this.spiderdata = spiderdata;
	}

	public long getBegintime() {
		return begintime;
	}

	public void setBegintime(long begintime) {
		this.begintime = begintime;
	}

	public int getLength() {
		return length;
	}

	public int getPageidx() {
		return pageidx;
	}

	public void setPageidx(int pageidx) {
		this.pageidx = pageidx;
	}

	public void setLength(int length) {
		this.length = length;
	}

//	public String getAjaxdatatype() {
//		return ajaxdatatype;
//	}
//
//	public void setAjaxdatatype(String ajaxdatatype) {
//		this.ajaxdatatype = ajaxdatatype;
//	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public void setProjectname(String projectname) {
		this.projectname = projectname;
	}

	public String getProjectname() {
		return projectname;
	}

//	public Object getCate() {
//		return cate;
//	}
//
//	public void setCate(Object cate) {
//		this.cate = cate;
//	}

	public void setAttr(Object attr) {
		this.attr = attr;
	}

	public Object getAttr() {
		return attr;
	}

	public boolean isItem() {
		if ("item".equalsIgnoreCase(pageType)) {
			return true;
		}
		return false;
	}

	public boolean isInfo() {
		if (pageType != null && pageType.endsWith("info")) {
			return true;
		}
		return false;
	}

	public boolean isList() {
		if ("list".equalsIgnoreCase(pageType)) {
			return true;
		}
		return false;
	}

	public boolean hasAjaxData() {
		return this.getAjaxdata() != null && this.getAjaxdata().size() > 0;
	}

	public boolean dataTypeIsHtml() {
		return "html".equalsIgnoreCase(this.getDatatype());
	}

	// item和info数据，保存源码和解析结果
	public boolean needSave() {
//		return (!"list".equalsIgnoreCase(this.getPageType()));
		return true;
	}

	/**
	 * 解析请求数据，放入parse unit
	 * 
	 * @param u
	 * @param time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ParseUnit fromMap(Map<String, Object> u, long time) {
		try {
			if (!u.containsKey("taskdata") || !u.containsKey("spiderdata"))
				return null;
			Map<String, Object> taskData = (Map<String, Object>) u.get("taskdata");
			Map<String, Object> spiderData = (Map<String, Object>) u.get("spiderdata");

			if (!taskData.containsKey("url") || !taskData.containsKey(Constants.cid) || !taskData.containsKey("pagetypeid")
					|| !spiderData.containsKey(Constants.code)) {
				return null;
			}

//			if((taskData.get("url").toString().indexOf("yixun")>0||
//					taskData.get("url").toString().indexOf("paipai")>0)&&taskData.get("type").toString().equalsIgnoreCase("list")){
//			}
			
			
			ParseUnit unit = new ParseUnit();
			unit.setUrl((String) taskData.get("url"));
			unit.setCid((String) taskData.get("cid"));
			unit.setSiteId((Integer)taskData.get("siteid"));
			unit.setPageType((String) taskData.get("pagetype"));
			if(taskData.containsKey("traceflag")){
				unit.setTraceflag(Integer.parseInt(taskData.get("traceflag").toString()));
			}
			if(taskData.containsKey(Constants.taskid)){
				unit.setTaskId(taskData.get(Constants.taskid).toString());
			}
			if(taskData.containsKey(Constants.IID)){
				unit.setIid((String) taskData.get(Constants.IID));
			}
			if(taskData.containsKey(Constants.purl)){
				unit.setPurl((String) taskData.get(Constants.purl));
			}
			
			unit.setPageTypeId((Integer)taskData.get("pagetypeid"));
//			unit.setUrl0((String) taskData.get("url0"));
//			unit.setBfdiid((String) taskData.get("bfdiid"));
			unit.setDatatype((String) taskData.get("datatype"));
//			unit.setAjaxdatatype((String) taskData.get("ajaxdatatype"));
			unit.setProjectname((String) taskData.get("projectname"));
//			unit.setParsetype((Integer) taskData.get("parsetype"));
			//TODO:这个字段的名称还要看后面怎么给
			if(taskData.containsKey(Constants.parseRsQueueName)){
				unit.setWriteToKfkName(Constants.parseRsQueueName);
			}
			if (taskData.containsKey("start_time"))
				unit.setStartTime((Integer) taskData.get("start_time"));

			Map att = (Map) taskData.get("attr");
			if (att != null)
				unit.setAttr(new HashMap(att));

			if (taskData.containsKey(Constants.pageidx))
				unit.setPageidx((Integer) taskData.get(Constants.pageidx));
			unit.setBegintime(time);

			if (spiderData.containsKey("length")) {
				if (spiderData.get("length") != null) {
					unit.setLength((Integer) spiderData.get("length"));
				}
			}

			unit.setCharset((String) spiderData.get("charset"));
			unit.setAjaxdata((List<Map<String, Object>>) spiderData.get("ajaxdata"));

			unit.setHttpcode(String.valueOf(spiderData.get(Constants.code)));
			unit.setData((String) spiderData.get("data"));

			unit.setTaskdata(taskData);
			unit.setSpiderdata(spiderData);
			return unit;
		} catch (Exception e) {
			LOG.warn("parse from unit exception, ", e);
		}
		return null;
	}

	public String getPageEncode() {
		return pageEncode;
	}

	public byte[] getPageBytes() {
		return pageBytes;
	}

	public void setPageEncode(String pageEncode) {
		this.pageEncode = pageEncode;
	}

	public void setPageBytes(byte[] pageBytes) {
		this.pageBytes = pageBytes;
	}

	public String getPageData() {
		return pageData;
	}

	public void setPageData(String pageData) {
		this.pageData = pageData;
	}

	public Integer getStartTime() {
		return startTime;
	}

	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

//	public int getParsetype() {
//		return parsetype;
//	}
//
//	public void setParsetype(int parsetype) {
//		this.parsetype = parsetype;
//	}
	public static void main(String[] args) {
		
	}
}
