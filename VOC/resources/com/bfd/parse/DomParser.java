package com.bfd.parse;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.StringUtil;
import com.bfd.crawler.utils.crawler.httpclient.MyCrawler;
import com.bfd.parse.ParseResult.ParseData;
import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.dom.DomCFGTree;
import com.bfd.parse.config.dom.DomConfig;
import com.bfd.parse.config.dom.DomSearch;
import com.bfd.parse.config.dom.DomTemplate;
import com.bfd.parse.config.fldmap.BfdItemFldMapRule;
import com.bfd.parse.config.fldmap.ItemInfoParser2;
import com.bfd.parse.config.shelf.JudgeRule;
import com.bfd.parse.config.shelf.JudgeRuleConfig;
import com.bfd.parse.config.shelf.JudgeStatue;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
//import com.bfd.parse.json.MTaoBaoJsonParser;
import com.bfd.parse.service.proxy.BizConfigureProxy;
import com.bfd.parse.test.weibosinaparser.Task;
import com.bfd.parse.test.weibosinaparser.WeiboParser;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.MyDateUtil;
import com.bfd.parse.util.TextUtil;

public class DomParser {

	private static final Log LOG = LogFactory.getLog(DomParser.class);
	private static final Log TLOG = LogFactory.getLog("com.bfd.parse.TemplateLog");

	private DomSearch domSearch;
	private URLNormalizerClient normalizerClient;
	private String workName;

	private static final Pattern cntFilter = Pattern.compile("\\d+");
	private static final Pattern totalNumReges = Pattern.compile("共(\\d+)页");

	// 用于打印解析失败的页面源代码，每个cid最多打印200次
	private static String countDate = MyDateUtil.getStr(new Date(),
			MyDateUtil.DATE_FORMAT);
	private static Map<String, Integer> printErrCountCidMap = new ConcurrentHashMap<String, Integer>();
	private static int printErrCount = 200;

	public DomParser() {
		workName = Thread.currentThread().getName();
		domSearch = new DomSearch(workName, normalizerClient);
	}

	public DomParser(String workName, URLNormalizerClient normalizerClient) {
		this.workName = workName;
		domSearch = new DomSearch(workName, normalizerClient);
		this.normalizerClient = normalizerClient;
	}

	public ParseResult parse(ParseUnit unit, ParseResult result) {
		ParseData parsedata = result.getParsedata();
		parsedata.setParsebegintime(System.currentTimeMillis());

		// item类型判定上下架状态
//		String itemStatus = null;

		ParseRS templateRS = null;
		templateRS = parseByTemplate(unit);
		parsedata.setParsecode(templateRS.parseCode);
		if (templateRS != null && templateRS.getRs() != null) {
			parsedata.getData().putAll(templateRS.getRs());
			// return result;
		}
		
		if (parsedata.getParsecode() == ParseResult.FAILED) {
			parsedata.setErrMsg(templateRS.getErrMsg());
			return result;
		}
		//TODO:这一步已经移到parserface类的addExtraInfo方法里，因为datatype为json的也需要执行这个函数
//		handExtraArgs(unit, unit.getPageEncode(), itemStatus,
//				parsedata.getData());
		parsedata.setParsecode(ParseResult.SUCCESS);

		return result;
	}

	public <T> List<T> transDataToListObjs(T bean, Object data) {
		List<T> result = new ArrayList<T>();
		// Object data = getData();
		if (data instanceof List) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) data;
			for (Map<String, Object> map : list) {
				T nBean;
				try {
					nBean = (T) BeanUtils.cloneBean(bean);
					// BeanUtil.setValue(map, nBean);
					BeanUtils.populate(nBean, map);
					result.add(nBean);
				} catch (Exception e) {
					LOG.warn("transDataToObj exception. ", e);
				}
			}
		}
		return result;
	}

	class ParseRS {
		private int parseCode = -1;
		private Map<String, Object> rs = null;
		private String errMsg ;
		
		
		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}

		public int getParseCode() {
			return parseCode;
		}

		public void setParseCode(int parseCode) {
			this.parseCode = parseCode;
		}

		public Map<String, Object> getRs() {
			return rs;
		}

		public void setRs(Map<String, Object> rs) {
			this.rs = rs;
		}

	}

	private ParseRS parseByTemplate(ParseUnit unit) {
		ParseRS result = new ParseRS();
		// 获取解析模板通过siteId和pagetypeid得到，通过查parseTemplate表得到；相应的缓存也要改
		DomTemplate tpl = getDomTemplate(unit.getSiteId()+"");
		LOG.info(workName+" url:"+unit.getUrl()+",cid:"+unit.getCid());

		if (!checkDomTemplate(tpl, unit.getPageTypeId()+"")) {
			LOG.info(workName + " NO PARSE template cid=" + unit.getCid()
					+ ", typeId=" + unit.getPageTypeId() + ", tpl=" + JsonUtils.toJSONString(tpl));
			TLOG.info("no template, cid = " + unit.getCid() + ", typeId = "
					+ unit.getPageTypeId() + ", url = " + unit.getUrl());
			result.setErrMsg("siteId:"+unit.getSiteId()+" pageTypeId:"+unit.getPageTypeId()+" no found template");
			result.setParseCode(ParseResult.NOFOUND_TEMPLATE);
			return result;
		}

		// 解析DOM数据
		DocumentFragment doc = null;
		// 标签补全
		unit.setPageBytes(TextUtil.balanceTag(unit.getPageBytes(),
				unit.getPageEncode()));
		try {
			doc = DomParser.parse2Html(unit.getPageBytes(),
					unit.getPageEncode());
		} catch (Exception e) {
			LOG.warn(workName + " parsing to HTML Exception, Err=" + ",url="
					+ unit.getUrl(), e);
		}
		Map<String, Object> errMsg = new HashMap<String, Object>();
		long templateParseBeginTime = System.currentTimeMillis();
		Map<String, Object> rmap = parseData(unit.getPageTypeId()+"", unit.getCid(),
				unit.getUrl(), doc, tpl, unit.getPageEncode(),errMsg);
		long templateParseEndTime = System.currentTimeMillis();
		LOG.info("url:"+unit.getUrl()+" template parse consumer time:"+(templateParseEndTime-templateParseBeginTime));
		if (rmap == null) {
			result.setParseCode(ParseResult.FAILED);
			result.setErrMsg(errMsg.get(Constants.errMsg).toString());
		} else {
			result.setParseCode(ParseResult.SUCCESS);
			result.setRs(rmap);
		}
//		 LOG.info("url : "+unit.getUrl()+".parseByTemplate:result:"+JsonUtil.toJSONString(result));
		return result;
	}
	


	/**
	 * 添加上下架信息，处理下一页，添加task data中的参数
	 * 
	 * @param unit
	 * @param charset
	 * @param itemStatus
	 * @param rmap
	 */
	public void handExtraArgs(ParseUnit unit, String charset,
			String itemStatus, Map<String, Object> rmap) {
		if (itemStatus != null)
			rmap.put("onshelf", itemStatus);
		String nextpage = "";
//		if (rmap.containsKey("nextpage") && unit.getPageidx() < 50) {
		//针对华为的小米和花粉抓取，分页不止50页
		if (rmap.containsKey(Constants.nextpage) ) {
			Object obj = rmap.get(Constants.nextpage);
			nextpage = handleNextPage(unit.getPageidx(), obj, unit.getSiteId()+"",
					unit.getPageType(), unit.getUrl());
			if (unit.getUrl().equalsIgnoreCase(nextpage)
					|| nextpage.equalsIgnoreCase(unit.getUrl0()))
				nextpage = "";
			rmap.put(Constants.nextpage, nextpage);
		} else {
			if (unit.getUrl0() != null
					&& StringUtils.isNotEmpty(unit.getUrl0().trim())) {
				nextpage = "";
				rmap.put(Constants.nextpage, nextpage);
			}
		}
		if (StringUtils.isNotEmpty(nextpage) && unit.getPageidx() > 0) {
			rmap.put("pageidx", unit.getPageidx() + 1);
		}

		rmap.put("cid", unit.getCid());
//		rmap.put("bfdiid", unit.getBfdiid());
		rmap.put(Constants.URL, unit.getUrl().trim());
		rmap.put("type", unit.getPageType());
		rmap.put(Constants.IID, unit.getIid());
		rmap.put("length", unit.getLength());
		rmap.put("charset", charset);

		// 将任务中的category放入结果
		if (unit.getTaskdata().containsKey(Constants.category)) {
			Object category = unit.getTaskdata().get(Constants.category);
			if (category != null) {
				rmap.put(Constants.category, category);
			}
		}

		// 如果存在imgs，抽取 text(key)和img(value)信息组成Map，放入imgList（保留提供给挖掘的格式）
		if (domSearch.hasImgs() && rmap.containsKey("imgs")) {
			List<Map> imgs = (List) rmap.get("imgs");
			List<Map> imgList = new ArrayList<Map>();
			for (int i = 0; i < imgs.size(); i++) {
				Map<String, Object> valueMap = new HashMap<String, Object>();
				Map<String, Object> imgMap = imgs.get(i);
				String text = (String) imgMap.get("imgtag");
				String img = (String) imgMap.get("img");
				if (StringUtils.isEmpty(text)) {
					text = "" + (i + 1);
				}
				valueMap.put(text, StringUtils.isEmpty(img) ? "" : img);
				imgList.add(valueMap);
			}
			if (imgList.size() > 0) {
				rmap.put("imglist", imgList);
			}
		}
	}

	/**
	 * 检查模板，是否包含指定类型
	 * 
	 * @param tpl
	 * @param type
	 * @return
	 */
	private boolean checkDomTemplate(DomTemplate tpl, String type) {
		if (tpl == null) {
			return false;
		} else {
			ArrayList<DomCFGTree> templates = tpl.getTemplates();
			if (templates.size() == 0)
				return false;
			for (DomCFGTree tree : templates) {
				if (tree.getType().equalsIgnoreCase(type))
					return true;
			}
			return false;
		}
	}

	/**
	 * 处理下一页.
	 * 
	 * @param pageno
	 * @param url
	 * @param cid
	 * @param obj
	 * @return
	 */
	public String handleNextPage(int pageno, Object objs, String cid,
			String type, String url) {
		String nextpage = "";
		if (objs != null && objs instanceof List) {
			List pages = (List) objs;
			String sTotalNum = "0";
			for (Object obj : pages) { // 找到link
				if (obj instanceof String) {
					Matcher matcher = totalNumReges
							.matcher(String.valueOf(obj));
					if (matcher.find()) {
						sTotalNum = matcher.group(1);
						break;
					}
				}
				if (obj instanceof Map) {
					Map page = (Map) obj;
					String text = (String) page.get("text");
					Matcher matcher = totalNumReges.matcher(text);
					if (matcher.find()) {
						sTotalNum = matcher.group(1);
						break;
					}
				}
			}
			if (StringUtils.isNumeric(sTotalNum)) {
				int totalNum = Integer.valueOf(sTotalNum);
				if (totalNum > 0 && totalNum <= pageno) {
					nextpage = "";
					LOG.debug(workName + " current pagenum reached totalNum ");
					return nextpage;
				}
			}
			for (Object obj : pages) { // 找到link, 匹配数字下一页
				if (obj instanceof Map) {
					Map page = (Map) obj;
					String text = (String) page.get("text");
					text = filterCnt(text);
					if (text.trim().equals("" + (pageno + 1))) {
						nextpage = (String) page.get("link");
						if (LOG.isDebugEnabled())
							LOG.debug(workName
									+ " guess nextpage by pageno, nextpage="
									+ nextpage);
						break;
					}
				}
			}
			if (StringUtils.isEmpty(nextpage)) {
				boolean flag = false;
				for (Object obj : pages) { // 找到link, 匹配数字下一页
					if (obj instanceof String) {
						String text = filterCnt((String) obj);
						if (flag) {
							break;
						}
						if (text.trim().equals("" + pageno)) {
							flag = true;
						}
					}
					if (obj instanceof Map) {
						Map page = (Map) obj;
						if (flag) {
							nextpage = (String) page.get("link");
							LOG.debug(workName
									+ " guess nextpage by next button of current pageno button, nextpage="
									+ nextpage);
							break;
						}
						String text = (String) page.get("text");
						text = filterCnt(text);
						if (text.trim().equals("" + pageno)) {
							nextpage = (String) page.get("link");
							flag = true;
						}
					}
				}
			}
		} else if (objs != null && objs instanceof Map) {
			nextpage = (String) ((Map) objs).get("link");
			if (LOG.isDebugEnabled())
				LOG.debug("Got real nextpage=" + nextpage);
		}
		if (StringUtils.isNotEmpty(nextpage)) {
			//TODO 归一化的第一个参数cid可以改为siteId吗？
			nextpage = normalizerClient.normalize(cid, type, nextpage, url,
					true);
		}
		if (nextpage == null) {
			nextpage = "";
			LOG.info(workName + " got nextpage failed, will use Empty String.");
		}
		return nextpage;
	}

	private String filterCnt(String text) {
		if (StringUtils.isNotEmpty(text) && !StringUtils.isNumeric(text)) {
			Matcher matcher = cntFilter.matcher(text);
			if (matcher.find()) {
				text = matcher.group(0);
			}
		}
		return text;
	}

	/**
	 * 解析文件
	 * 
	 * @param url
	 * @param bytes
	 * @param bid
	 * @param type
	 * @param charset
	 * @return
	 */
	public Map<String, Object> parseFile(String url, byte[] bytes, String bid,
			String type, String charset) {
		// 标签补全
		bytes = TextUtil.balanceTag(bytes, charset);
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取解析模板
		DomTemplate tpl = getDomTemplate(bid);
		if (tpl == null) {
			return result;
		}
		// 获取页面编码
		if (StringUtils.isEmpty(charset))
			charset = EncodeUtil.getHtmlEncode(bytes, charset);

		InputSource input = new InputSource(new ByteArrayInputStream(bytes));
		input.setEncoding(charset);
		DocumentFragment doc = null;
		try {
			doc = DomParser.parse2Html(input, charset);
		} catch (Exception e) {
			LOG.warn("parse2Html failed, url=" + url, e);
		}

		// item类型判定上下架状态
		String itemStatus = null;
		itemStatus = judgeSellStatus(bid, url, type, bytes, charset);
		if (itemStatus == null) {
			LOG.info("bid=" + bid + ", url" + url + ", onshelf is null.");
		}
		
		Map<String, Object> errMsg = new HashMap<String, Object>();
		// 解析DOM数据
		Map<String, Object> rmap = parseData(type, bid, url, doc, tpl, charset,errMsg);
		if (rmap == null) {
			return result;
		} else if (itemStatus != null) {
			rmap.put("onshelf", itemStatus);
		}
		return rmap;
	}

	public Map<String, Object> parseData(String pageTyepId, String cid, String url,
			DocumentFragment doc, DomTemplate tpl, String charset,Map<String, Object> errMsg) {
		Map<String, Object> rmap = null;
//		boolean bOK = domSearch.executeTemplateParse(doc, tpl, pageTyepId, url,charset);
		boolean bOK = true;
//				
		
		if (!bOK) {
			String err = domSearch.getTreeString();
			String errMsgStr = " NO PARSE RESULT, cid=" + cid + ", pageTyepId="
					+ pageTyepId + ", url=" + url + ", Err:\n"
					+ (err.length() > 200 ? err.substring(0, 190) : err);
			// TODO 放入数据库。
			LOG.info(workName + errMsgStr);
			errMsg.put(Constants.errMsg, errMsgStr);
			return rmap;
		}
		rmap = domSearch.getParseResult();
		int gotNum = rmap.keySet().size();
		LOG.info(workName + " parse ok, got " + gotNum
				+ " items, template idx=" + domSearch.getMatchTmplIDX()
				+ ", cid=" + cid + ", pageTyepId=" + pageTyepId + ", url=" + url);
		return rmap;
	}

	/**
	 * 判定上下架信息
	 * 
	 * @param bid
	 * @param url
	 * @param bytes
	 * @param charset
	 * @return
	 */
	public String judgeSellStatus(String bid, String url, String type,
			byte[] bytes, String charset) {
		LOG.info("execute judgeSellStatus!");
		List<JudgeRule> rules = JudgeRuleConfig.getInstance().getJudgeRules(
				bid, type);
//		if("Tjd".equals(bid)){
//			LOG.info("cid:"+bid+". judgerules is "+JsonUtil.toJSONString(rules));
//		}
		String itemStatus = null;
		if (rules != null) {
			try {
				itemStatus = new JudgeStatue().judgeStatus(url, new String(
						bytes, charset), rules);
				if (StringUtils.isNotEmpty(itemStatus))
					LOG.info(workName + " got onshelf result=" + itemStatus
							+ ", biz=" + bid + ", url=" + url);
			} catch (Exception e) {
				LOG.warn("Judge sell status exception, url=" + url
						+ ", charset=" + charset, e);
			}
		}
		return itemStatus;
	}

	public DomSearch getDomSearch() {
		return domSearch;
	}

	public DomTemplate getDomTemplate(String siteId) {
		return DomConfig.getInstance().getBySiteId(siteId);
	}

	public List<DomCFGTree> getDomTemplate(String bid, String type) {
		return DomConfig.getInstance().get(bid, type);
	}

	public static DocumentFragment parse2Html(byte[] data, String charset)
			throws Exception {
		return parse2Html(
				new InputSource(new ByteArrayInputStream(new String(data,
						charset).replace((char) 26, (char) 32)
						.getBytes(charset))), charset);
	}

	public static DocumentFragment parse2Html(InputSource input, String encoding)
			throws Exception {
		DOMFragmentParser parser = new DOMFragmentParser();
		try {
			parser.setFeature(
					"http://cyberneko.org/html/features/augmentations", true);
			parser.setProperty(
					"http://cyberneko.org/html/properties/default-encoding",
					encoding);
			parser.setFeature(
					"http://cyberneko.org/html/features/scanner/ignore-specified-charset",
					true);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
					false);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/document-fragment",
					true);
			parser.setFeature(
					"http://cyberneko.org/html/features/report-errors", false);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment res = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		try {
			parser.parse(input, frag);
		} catch (Exception e) {
			throw e;
		}
		res.appendChild(frag);
		try {
			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				res.appendChild(frag);
			}
		} catch (Exception e) {
			throw e;
		}
		return res;
	}

	public static DocumentFragment parse2Html(String data, String encoding)
			throws Exception {
		return parse2Html(
				new InputSource(new ByteArrayInputStream(data.getBytes())),
				encoding);
	}

	public static DocumentFragment parse2Xml(byte[] bytes, String encoding) {
		try {
			InputSource input = new InputSource(new ByteArrayInputStream(bytes));
			input.setEncoding(encoding);
			return parse2Html(input, encoding);
		} catch (Exception e) {
			LOG.warn("Parsing to XML Exception, Err", e);
		}
		return null;
	}

	public static DocumentFragment parse2Xml(String content, String encoding) {
		try {
			InputSource input = new InputSource(new ByteArrayInputStream(
					content.getBytes()));
			input.setEncoding(encoding);
			return parse2Html(input, encoding);
		} catch (Exception e) {
			LOG.warn("Parsing to XML Exception, Err", e);
		}
		return null;
	}

	private static void testTemplateByHtml(String uri, String cid,
			String encoding, String dns, boolean isFromNet,String type) {
		DomParser domParser = new DomParser("parser-1",
				new URLNormalizerClient());
		DomTemplate tpl = domParser.getDomTemplate(cid);
//		System.out.println("template:"+JsonUtil.toJSONString(tpl));
		String string = "";
		if (isFromNet) {
			string = getHtmlFromNet(uri, encoding);
		} else {
			string = getHtmlFromFile(uri, encoding);
		}
		System.out.println(string);
		DocumentFragment doc = null;
		try {
			doc = parse2Html(string, encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> errMsg = new HashMap<String, Object>();
		Map<String, Object> rs = domParser.parseData(type, cid, dns, doc,
				tpl, encoding,errMsg);
		System.out.println(JsonUtil.toJSONString(rs));
	}

	private static String getHtmlFromFile(String fname, String encoding) {
		File file = new File(fname);
		byte[] bytes = new byte[(int) file.length()];
		DataInputStream in;
		try {
			in = new DataInputStream(new FileInputStream(file));
			in.readFully(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String string = null;
		try {
			string = new String(bytes, encoding);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return string;
	}

	public static String getHtmlFromNet(String url, String encoding) {
		DownloadClient crawler = new DownloadClient();
//		MyCrawler crawler = new MyCrawler();
		String string = crawler.getPageData(url);
//		String string = crawler.get(url)[1];
		System.out.println("html:" + string);
		return string;
	}

}
