package com.bfd.parse.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DocumentFragment;

import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.JacksonUtils;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParseResult.ParseData;
import com.bfd.parse.ParserFace;
import com.bfd.parse.DomParser;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.config.dom.DomTemplate;
import com.bfd.parse.config.fldmap.ItemFldMapConfig;
import com.bfd.parse.config.fldmap.ItemInfoParser;
import com.bfd.parse.entity.PagedefineEntity;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class DomTmplTester implements Tester {

	private static final Log LOG = LogFactory.getLog(DomTmplTester.class);

	private static final String WORKNAME = "worker-t";

	private DomParser domParser;
	private ParserFace parserFace;

	public DomTmplTester(URLNormalizerClient normalizer) {
		domParser = new DomParser(WORKNAME, normalizer);
//		parserFace = new ParserFace(WORKNAME, normalizer, true);
		parserFace = ParserFace.getInstance();
	}

	@Override
	public TestResponse test(TestRequest req) {
		
		LOG.info(req.getUrl()+" execute "+this.getClass().getName()+" test method! "
				+req.getReqMap().containsKey("dompath")+" "+req.getReqMap().containsKey("parseInfo"));
		if (req.getReqMap().containsKey("dompath")) {
			return testDomPath(req);
		} else if (req.getReqMap().containsKey("parseInfo")) {
			return testParseInfo(req);
		}
		return TestResponse.create();
	}

	private TestResponse testDomPath(TestRequest req) {
		byte[] data = req.getPage();
		String charset = "utf-8";
		if (req.getEncode() == null || "".equals(req.getEncode())) {
			charset = EncodeUtil.getHtmlEncode(data);
		} else {
			charset= req.getEncode();
		}
		
		data = TextUtil.balanceTag(data, charset);
		Map<String, Object> pathMap = req.getReqMap();
		Map<String, Object> param = (Map<String, Object>) pathMap.get("dompath");
		String url = pathMap.get("url")==null?"":pathMap.get("url").toString();
		ParsetemplateEntity template = ParsetemplateEntity.fromMap(JacksonUtils.compressMap(param));
		DomTemplate tmpl = new DomTemplate(template);
		LOG.info("testurl:"+url+",template:"+JsonUtil.toJSONString(tmpl));
		//
		LOG.debug("guess page charset=" + charset);
		DocumentFragment doc = null;
		try {
			doc = DomParser.parse2Html(data, charset);
		} catch (Exception e) {
			LOG.warn("parse to html exception, while parseTest by tmpl", e);
		}
		domParser.getDomSearch().setMaxPrintLevel(15);
		domParser.getDomSearch().setPrintMode(2);
		Map<String, Object> errMsg = new HashMap<String, Object>();
		Map<String, Object> rmap = domParser.parseData(template.getPagetypeid()+"", template.getSiteid()+"", url,
				doc, tmpl, charset,errMsg);

		if (rmap != null) {
//			if (ItemFldMapConfig.getInstance().getFldMapRule(template.getCid()) != null) {
//				Map<String, Object> tmp = new HashMap<String, Object>();
//				try {
//					if (ItemInfoParser.getBfdItemInfo(new String(data, charset), template.getCid(),
//							(String) pathMap.get("url"), tmp))
//						rmap.putAll(tmp);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
			String pageidx = null;
			int iPageidx = 1;
			if (pathMap.containsKey("pageidx")) {
				pageidx = (String) pathMap.get("pageidx");
				if (StringUtils.isNotEmpty(pageidx) && StringUtils.isNumeric(pageidx)) {
					iPageidx = Integer.valueOf(pageidx);
				}
			}
			PagedefineEntity pageDefine = PageDefineConfig.getInstance().getPageDefine(template.getPagetypeid()+"");
			handleExtraParser(data, pageDefine.getPagenameen(), template.getSiteid()+"", rmap, iPageidx, (String) pathMap.get("url"));
			LOG.info("testurl:"+url+" rsmap:"+JsonUtil.toJSONString(rmap));
			return TestResponse.create(0).putAll(rmap);
		}
		LOG.info("testurl:"+url+" parse fail");
		return TestResponse.create(1).put("errtree", domParser.getDomSearch().getTreeString());
	}

	private TestResponse testParseInfo(TestRequest req) {
		if (!checkParseInfoRequest(req)) {
			return TestResponse.create();
		}
		Map<String, Object> reqMap = req.getReqMap();
		Map task = (Map) reqMap.get("parseInfo");
		Object osave = (Object) req.getReqMap().get("save");
		boolean save = osave == null ? false : (Boolean) osave;
		((Map) task.get("spiderdata")).put("data", req.getPageData());
		//TODO 这个逻辑应该可以去掉了
//		((Map) task.get("taskdata")).put("parsetype", WebsiteMap.INSTANCE.get(req.getCid()).getParsetype());
		ParseUnit unit = ParseUnit.fromMap(task, System.currentTimeMillis());
		ParseResult result = parserFace.parse(unit, true);
		ParseData parsedata = result.getParsedata();
		if (parsedata.getParsecode() == ParseResult.SUCCESS) {
			if (save)
				parserFace.getDataSaver().saveData(unit, result);
			return TestResponse.create(0).putAll(parsedata.getData());
		}
		if (parsedata.getParsecode() == ParseResult.OFF_SHELF) {
			if (save)
				parserFace.getDataSaver().saveData(unit, result);
			return TestResponse.create(0).put("onshelf", "off");
		}
		LOG.warn(WORKNAME + " parse failed! Taskdata -> " + JsonUtil.toJSONString(task.get("taskdata")));
		return TestResponse.create();
	}

	private boolean checkParseInfoRequest(TestRequest req) {
		if (StringUtils.isEmpty(req.getCid()) || StringUtils.isEmpty(req.getUrl())
				|| StringUtils.isEmpty((String) req.getReqMap().get("type"))
				|| req.getReqMap().get("parseInfo") == null) {
			return false;
		}
		return true;
	}

	private void handleExtraParser(byte[] page, String type, String siteId, Map<String, Object> rmap, int pageidx,
			String url) {
		// item类型判定上下架状态
		String itemStatus = null;
//		if ("item".equalsIgnoreCase(type)) {
//			itemStatus = domParser.judgeSellStatus(siteId, "", type, page, EncodeUtil.getHtmlEncode(page));
//		}
		if (itemStatus != null)
			rmap.put("onshelf", itemStatus);
		String nextpage = "";
		if (rmap.containsKey("nextpage")) {
			Object obj = rmap.get("nextpage");
			rmap.put("nextpage_", obj);
			nextpage = domParser.handleNextPage(pageidx, obj, siteId, type, "");
			if (url.equalsIgnoreCase(nextpage))
				nextpage = "";
			rmap.put("nextpage", nextpage);
		}

		// 如果存在imgs，抽取 text(key)和img(value)信息组成Map，放入imgList
		if (domParser.getDomSearch().hasImgs() && rmap.containsKey("imgs")) {
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

}
