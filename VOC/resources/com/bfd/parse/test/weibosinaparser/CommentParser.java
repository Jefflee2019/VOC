package com.bfd.parse.test.weibosinaparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.utils.MyStringUtil;
import com.bfd.crawler.utils.ParserException;
import com.bfd.crawler.utils.StringUtil;
import com.bfd.crawler.utils.htmlcleaner.HtmlCleanerUtil;
import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.MyDateUtil;



public class CommentParser extends AWeiBoParser{
	private static Pattern GETWEIBOID = Pattern.compile("id=(\\d+)");
	@Override
	public void executeParseAjax(String jsonDatas, Task task, Map rs,JsonData jsonData) throws Exception {

//		try {
			Map json = (Map)JsonUtil.parseObject(jsonDatas);
			Map<String, Object> data = (Map<String, Object>) json.get("data");
			String html = data.get("html").toString();
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(html);
//			String wid = (String)rs.get("commentwid");
			String wid = getWeiboId(jsonData.getUrl());
			LOG.debug("url:"+task.getUrl()+" ajaxurl:"+jsonData.getUrl()+" get wid:"+wid);
			List list = (List)parseComment(root,task).get(Constants.COMMENTS);
			List listnew =  new ArrayList();
			if(wid !=null){
				for(int i = 0; i < list.size(); i++ ){
					Map<String, Object> map = (Map<String, Object>)list.get(i);
					map.put("wid", wid);
					listnew.add(map);
				}
//				rs.remove("commentwid");
				list = listnew;
			}
			((List)rs.get(Constants.COMMENTS)).addAll(list);
	
	}
	
	public String getWeiboId(String url ){
		String wid = "";
		wid = StringUtil.getRegexGroup(GETWEIBOID, url, 1);
		return wid;
	}

	private static final Log LOG = LogFactory.getLog(CommentParser.class);
	private static Pattern pDay = Pattern.compile("(\\d+)天");
	private static Pattern pHour = Pattern.compile("(\\d+)小时");
	private static Pattern pMinute = Pattern.compile("(\\d+)分钟");
	private static Pattern pMonth = Pattern.compile("(\\d+)月");
	private static Pattern pdate = Pattern.compile("(\\d+)日");
	private static Pattern pminutestr = Pattern.compile("(\\d+:\\d+)");
	
	
	public Map<String, Object> parseComment(TagNode root,Task task) throws XPatherException{

		Map<String, Object> mapRs = new HashMap<String, Object>();
		List<Map<String, Object>> commens = new ArrayList<Map<String, Object>>();
		Object[] divs;
		Object[] divnews;
//		try {
			//得到回复信息
			divs = root.evaluateXPath("//dl[@class='comment_list S_line1']");
			divnews = root.evaluateXPath("//div[@class='list_li S_line1 clearfix']");
			if(divs.length>0){
				for(int i=0;i<divs.length;i++){
					Map<String, Object> map = new HashMap<String, Object>();
					TagNode div = (TagNode)divs[i];
					map.put("wid", div.getAttributeByName("mid"));
					Object[] cids = div.evaluateXPath("//div[@class='info']/em[@class='hover']/a[1]");
					if(cids.length>0){
						String rids = ((TagNode)cids[0]).getAttributeByName("onclick");
						String rid;
						try {
							rid = MyStringUtil.getRegexGroup("rid=(\\d+)&", rids, 1);
							if(rid.length()>0){
								map.put("mid", rid);
							}
						} catch (ParserException e) {
							e.printStackTrace();
						}
					}
	//				TagNode a = (TagNode)div.evaluateXPath("div[1]/a[1]")[0];
	//				userUrl = formatUserParsedUrl(a.getAttributeByName("href"));
	//				map.put(Constants.USER_URL, userUrl);
	//				LOG.info(getUrl(task)+" user url :"+userUrl);
					TagNode dd = (TagNode)div.evaluateXPath("dd[1]")[0];
	//				LOG.info("content:"+dd.getText().toString());
	//				List<HtmlNode> children = (List<HtmlNode>)dd.getAllChildren();
	//				LOG.info("-------content----------------");
					String content = "";
	//				content = dd.getText().toString().trim();
					//评论内容中增加  @** 的信息
					Object[] aObjs = dd.evaluateXPath("a");
					StringBuffer asb = new StringBuffer();
					for(Object o : aObjs){
						TagNode a = (TagNode)o;
						asb.append(" "+a.getText());
					}
					content = HtmlCleanerUtil.getContentByTagNode(dd);
					content = asb+" "+content;
	//				for(int j=0;j<children.size();j++){
	//					if(children.get(j) instanceof ContentNode){
	//						temp = ((ContentNode)children.get(j)).getContent();
	//						
	//						if(temp.trim().length()!=0){
	//							content +=temp;
	////							LOG.info(temp);
	//						}
	//						
	//					}
	//				}
					Object[] imgs = div.evaluateXPath("dd[1]/img");
					for(Object img:imgs){
						TagNode imgNode = (TagNode)img;
						content+=imgNode.getAttributeByName("title");
					}
					map.put(Constants.CONTENT, content);
	//				LOG.info(getUrl(task)+" content:"+content);
					TagNode time = (TagNode)div.evaluateXPath("//span[@class='S_txt2']")[0];
					long dateTime = formatTime(time.getText().toString(),task);
					String timeStr = getSecodeTime(dateTime+"");
	//				LOG.info(getUrl(task)+" time:"+timeStr);
					map.put(Constants.TIME, timeStr);
					Object[] objs = div.evaluateXPath("dt[1]/a[1]");{
						if(objs.length>0){
							TagNode user = (TagNode)objs[0];
							String username = user.getAttributeByName("title");
							String url = user.getAttributeByName("href");
							url = "http://www.weibo.com/u"+url;
							map.put(Constants.USER_URL, url);
							map.put(Constants.username, username);
						}
					}
					Object[] as = div.evaluateXPath("dd[1]/a");
					getUserInfoFromCommRepost(as, map);
	//				LOG.info(getUrl(task)+" 时间:"+time.getText().toString());
					commens.add(map);
				}
			}
			if(divnews.length>0){
				for(int i=0;i<divnews.length;i++){
					Map<String, Object> map = new HashMap<String, Object>();
					TagNode div = (TagNode)divnews[i];
					map.put("mid", div.getAttributeByName("comment_id"));
					//用户ID
//					Object[] cids = div.evaluateXPath("//div[@class='WB_face W_fl']/a[1]/img[1]");
//					if(cids.length>0){
//						String rids = ((TagNode)cids[0]).getAttributeByName("usercard");
//						String rid;
//						try {
//							rid = MyStringUtil.getRegexGroup("id=(\\d+)", rids, 1);
//							if(rid.length()>0){
//								map.put("userid", rid);
//							}
//						} catch (ParserException e) {
//							e.printStackTrace();
//						}
//					}
					TagNode dd = (TagNode)div.evaluateXPath("//div[@class='list_con']/div[@class='WB_text']")[0];
					String content = "";
					//评论内容中增加  @** 的信息
					Object[] aObjs = dd.evaluateXPath("a");
					StringBuffer asb = new StringBuffer();
					for(Object o : aObjs){
						TagNode a = (TagNode)o;
						asb.append(" "+a.getText());
					}
					content = HtmlCleanerUtil.getContentByTagNode(dd);
					content = asb+" "+content;
					Object[] imgs = div.evaluateXPath("//div[@class='list_con']/div[@class='WB_text']/img");
					for(Object img:imgs){
						TagNode imgNode = (TagNode)img;
						content+=imgNode.getAttributeByName("title");
					}
					map.put(Constants.CONTENT, content);
					TagNode time = (TagNode)div.evaluateXPath("//div[@class='WB_from S_txt2']")[0];
					long dateTime = formatTime(time.getText().toString(),task);
					String timeStr = getSecodeTime(dateTime+"");
					map.put(Constants.TIME, timeStr);
					Object[] objs = div.evaluateXPath("//div[@class='WB_text']/a[1]");{
						if(objs.length>0){
							TagNode user = (TagNode)objs[0];
							String username = user.getText().toString();
							String url = user.getAttributeByName("href");
							url = "http://www.weibo.com/u"+url;
							map.put(Constants.USER_URL, url);
							map.put(Constants.username, username);
						}
					}
					map.put(Constants.CONTENT, map.get(Constants.CONTENT).toString().replace(map.get(Constants.username).toString(), ""));
					Object[] as = div.evaluateXPath("//div[@class='list_con']/div[@class='WB_text']/a");
					getUserInfoFromCommRepost(as, map);
					commens.add(map);
				}
			}
			mapRs.put(Constants.COMMENTS, commens);
			
			Object[] divwid;
			String wid = "";
			List<Map<String, Object>> commenswid = new ArrayList<Map<String, Object>>();
			try {
				//得到回复信息
				divwid = root.evaluateXPath("//div[@class='WB_cardwrap WB_feed_type S_bg2']");
				if(divwid.length>0){
					TagNode div = (TagNode)divwid[0];
					wid = div.getAttributeByName("mid");
				}
				//设置wid
				if(wid!=null&&!"".equals(wid)){
					mapRs.put("commentwid", wid);
				}
			} catch (XPatherException e) {
				e.printStackTrace();
			}
			
//		} catch (XPatherException e) {
//			e.printStackTrace();
//			LOG.error("error url is "+task.getUrl());
//		}
		LOG.info("rs:"+JsonUtil.toJSONString(mapRs));
		return mapRs;
	}

	private long formatTime(String time,Task task){
		long downloadTime = (long)Double.parseDouble(task.getSpiderdata().get("dbegintime").toString());
		LOG.debug("downloadtime:"+downloadTime);;
		String day = StringUtil.getRegexGroup(pDay, time, 1);
		String hour = StringUtil.getRegexGroup(pHour, time, 1);
		String minute = StringUtil.getRegexGroup(pMinute, time, 1);
		long deltatime = 0;
		if(!day.equals("")||!hour.equals("")||!minute.equals("")){
			if(!day.equals("")){
				deltatime += Integer.parseInt(day)*86400;
			}
			if(!hour.equals("")){
				deltatime += Integer.parseInt(hour)*3600;
			}
			if(!minute.equals("")){
				deltatime += Integer.parseInt(minute)*60;
			}
			if(deltatime == 0 ){
				deltatime = 86400;
			}
//			deltatime += downloadTime;
			deltatime = downloadTime-deltatime;
			return deltatime*1000;
		}
		String date = StringUtil.getRegexGroup(pdate, time, 1);
		String month = StringUtil.getRegexGroup(pMonth, time, 1);
		String hourMinute = StringUtil.getRegexGroup(pminutestr, time, 1);
		String dateStr = "";
		int[] yearMonthDay = MyDateUtil.getYearMonthDay();
		if(!date.equals("")&&!month.equals("")){
			dateStr = yearMonthDay[0]+"-"+month+"-"+date+" "+hourMinute;
		}else if(time.indexOf("今天")>=0){
			dateStr = yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+" "+hourMinute;
		}else{
			dateStr = time.trim();
		}
//		LOG.info( getUrl(task)+" dateStr:"+dateStr);
		Date dd = MyDateUtil.getDate(dateStr, MyDateUtil.DATE_SHORT_TIME_FORMAT);
		
		long timeNum = dd.getTime();
		if(dateStr.indexOf("秒")>0){
			timeNum = new Date().getTime();
			return timeNum;
		}
//		LOG.info(getUrl(task)+" dd:"+dd+".time:"+timeNum);
//		LOG.info(getUrl(task)+" timeNum:"+timeNum);
		return timeNum;
	}
	@Override
	public String getFlagStr() {
		
//		return "Pl_Official_LeftWeiboDetail__37";
		return "pl.content.weiboDetail.index";
	}

	@Override
	public Map<String, Object> executeParse(TagNode root,Task task) throws XPatherException {
		return parseComment(root,task);
		
	}
	
	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
		// 个人用户信息url
//		 String url = "http://weibo.com/p/1006061957258370/weibo";
//		String url = "http://weibo.com/tclmobile";
		//微博
//		 String url = "http://weibo.com/p/1005051172294045/weibo";
		// 粉丝url
		 String url =
//		 "http://weibo.com/p/1006061812511057/follow?relate=fans";
//		 "http://weibo.com/2608693591/Bxgz7wf1x?type=comment";
		"http://weibo.com/aj/comment/big?ajwvr=6&id=3834861489595433&__rnd=1430210206178";
		String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
//		String cookie = "	UOR=t.58.com,widget.weibo.com,login.sina.com.cn; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5K2t; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1410502817499:22:9:6:9119966039234.223.1410502817495:1410502470669; SWB=usrmd1081; WBStore=b9261755eac1ef77|undefined; _s_tentry=s.weibo.com; Apache=9119966039234.223.1410502817495; WBtopGlobal_register_version=b40d2924ae85ed0f; SUS=SID-1661429500-1410503257-GZ-iwyv4-827bdccebdde60e78aae66140f088657; SUE=es%3D6f89208ba35f7d965d86f50fe40a0142%26ev%3Dv1%26es2%3D57cb8a761c44ce9b27fdf26399fed6ce%26rs0%3DMJTtIoUui49AoC0SXimOjxVGPjdKVA%252BJVW4nbjg64pey4Yy1GghgKxMxXBmHxoScGsI7bEmaL1pj9%252BfB%252BrwUiaAcfA5UVHxj2I%252BI6Fy9%252BiBbwRJES1hMajpCjZBClZgeuW6MqIheJPyN6JzU5tYWeFlpcrwvfEST%252Fv99HHpSOhY%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1410503257%26et%3D1410589657%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjTh1ua8NlrAJXnvkRzmLnbI1H-jyQnNOYAn7uJhIyHRgv7nI2qSUShDIOiUDfElSixwIf2T8BzwUL3g..; ALF=1442039257; SSOLoginState=1410503257; un=15110295667";
//		String html = client.getPageData(url, "", refer, cookie);
//		MyCrawler crawler = new MyCrawler();
//		String htmls[] = crawler.get(url,
//				"http://d.weibo.com/?topnav=1&mod=logo&wvr=6", cookie);
//		String html = "";
		Task task = getTestTask(url);
		try {
//			Map<String, Object> map = (Map<String, Object>) JsonUtil.parseObject(htmls[1]);
//			Map<String, Object> htmlMap = (Map<String, Object>) map.get("data");
//			html = htmlMap.get("html").toString();
//			System.out.println(html);
//			TagNode root = new HtmlCleaner().clean(html);
//			new CommentParser().parseComment(root, task);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		new CommentParser().parseHtml(htmls[1], task);
		System.out.println(new CommentParser().getWeiboId("http://weibo.com/aj/v6/comment/big?ajwvr=6&id=3909178319935973&__rnd=1447830367929"));
	}

}
