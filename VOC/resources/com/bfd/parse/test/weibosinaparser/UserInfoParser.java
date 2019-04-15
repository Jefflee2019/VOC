package com.bfd.parse.test.weibosinaparser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.utils.htmlcleaner.HtmlCleanerUtil;
import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;


public class UserInfoParser implements IWeiBoparser {
	private static final Log LOG = LogFactory.getLog(UserInfoParser.class);
	public static final String USER_FLAG = "Pl_Official_Header__1";
	public static final String COMPANY_FLAG = "Pl_Core_Header__1";
	
	@Override
	public Map<String, Object> parseHtml(String html, Task task) throws Exception {
//		LOG.info("html:"+html);
		Matcher m = getAllHtmlJson.matcher(html);
		String content = "";
//		System.out.println("--------------------------html end!------------------");
		boolean isUser = false;
		
		Map<String, Object> rs = new HashMap<String, Object>();
		int total = 0;
		while (m.find()) {
			total++;
			
			content = m.group(1);
//			LOG.info("第"+total+" 个 content:"+content);
			// 内容
			//评论转发
			if (content.indexOf("\"domid\":\"Pl_Core_T8CustomTriColumn__3\"") > 0) {
				
				
				String htmlstr = Utils.getHtml(content);
				LOG.info("评论转发 fragment is :"+htmlstr);
				isUser = true;
				
				HtmlCleaner cleaner = new HtmlCleaner();
				TagNode root = cleaner.clean(htmlstr);
				// 关注，粉丝，微博
				getNum(root, rs, task);
				// break;
			}
			//用户认证
			if (content.indexOf("\"domid\":\"Pl_Core_UserInfo__5\"") > 0) {
				
				String htmlstr = Utils.getHtml(content);
				isUser = true;
				rs.put(Constants.userType, 1);
				HtmlCleaner cleaner = new HtmlCleaner();
				LOG.info("用户认证 fragment is :"+htmlstr);
				TagNode root = cleaner.clean(htmlstr);
				getAuthentication(root,rs,task);
				String cityxpath1 = "/body/div/div/div[2]/div/div/ul/li[1]/span[2]";
				String cityxpath2 = "/body/div/div/div/div/div/ul/li[2]/span[2]";
				String city = HtmlCleanerUtil.getValueByXpath(root, cityxpath1, "", "text");
				if(city.length()==0){
					city = HtmlCleanerUtil.getValueByXpath(root, cityxpath2, "", "text");
				}
				if(city.length()==0){
					LOG.error("url:"+task.getUrl()+" can not get city");
				}
//				System.out.println("city:"+city);
				rs.put("city", city);

				/**
				 * Author tsg this is to get user's birthday
				 */
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>---------birthday---------------------------

				TagNode constellationNode = HtmlCleanerUtil.getNodeByXpath(
						root,
						"//em[@class='W_ficon ficon_constellation S_ficon']");
				if (constellationNode != null) {
					TagNode birthNode = constellationNode.getParent()
							.getParent().getChildren().get(1);
					String birth = birthNode.getText().toString().trim();
					rs.put(Constants.birth, birth);
				}

				// following-sibling::*[1]
				// <<<<<<<<<<<<<<<<<<<<<<<<<<<--------birthday----------------------------
				// break;
			}
			//企业认证
			if (content.indexOf("\"domid\":\"Pl_Core_UserInfo__4\"") > 0) {
				
				String htmlstr = Utils.getHtml(content);
				isUser = true;
				rs.put(Constants.userType, 2);
				HtmlCleaner cleaner = new HtmlCleaner();
				LOG.info("企业认证 fragment is :"+htmlstr);
				TagNode root = cleaner.clean(htmlstr);
				getAuthentication(root,rs,task);
//				break;
			}
			// 用户名&&性别
			if (content.indexOf("\"domid\":\"Pl_Official_Headerv6__1\"") > 0) {
				
				String htmlstr = Utils.getHtml(content);
				isUser = true;
				LOG.info("用户名 fragment is :"+htmlstr);
				HtmlCleaner cleaner = new HtmlCleaner();
				TagNode root = cleaner.clean(htmlstr);
				String username = HtmlCleanerUtil.getValueByXpath(root, "//h1[@class='username']", "", "text");
				rs.put(Constants.username, username);
				/**
				 * @author tsg
				 * @description this node is for fetching uid;
				 */
				// >>>>>>>>>>>>>>>>>>>--------------uid----------------------------------------
				TagNode uidNode = HtmlCleanerUtil.getNodeByXpath(root,
						"//div[@class='btn_bed W_fl']");
				String uid="";
			if(uidNode!=null){
					String actionAttr=uidNode.getAttributeByName("action-data");
					if(actionAttr.length()>=14){
				 uid= uidNode.getAttributeByName("action-data")
						.substring(4, 14);
					}
				}
				rs.put(Constants.userid, uid);
				// <<<<<<<<<<<<<<<<<<<--------------uid----------------------------------------
				/**
				 * @author tsg
				 * @description this is to get user's sex
				 */
				// >>>>>>>>>>>>>>>>>>>--------------sex----------------------------------------
				String sex = "";
				TagNode sexNode = HtmlCleanerUtil.getNodeByXpath(root,
						"//i[@class='W_icon icon_pf_male']");
				if (sexNode != null) {
					sex = "m";
				} else {
					sexNode = HtmlCleanerUtil.getNodeByXpath(root,
							"//i[@class='W_icon icon_pf_female']");
					if (sexNode != null) {
						sex = "f";
					}
				}
				rs.put(Constants.sex, sex);
				// <<<<<<<<<<<<<<<<<<<--------------sex----------------------------------------
				// break;
			}

		}
		
		Utils.addAttr(rs, task);
		LOG.info(task.getTaskdata().get("url")+".rs1:"+JsonUtil.toJSONString(rs));
		return rs;
	}


	private static void getNum(TagNode root,Map rs,Task task) {
		try {
			Object[] lis = root
					.evaluateXPath("//td[@class='S_line1']/strong");
			if(lis.length==0){
				lis = root.evaluateXPath("//td[@class='S_line1']/a/strong");
			}
//			 LOG.info(task.getTaskData().get("url")+" num:"+lis.length);
			int total = 0;
			for (Object li : lis) {

				TagNode liO = (TagNode) li;
				int num = Integer.parseInt(liO.getText().toString());
				// LOG.info(liO.getText());
				if (total == 0) {
					rs.put(Constants.FOLLOWS, num);
//					LOG.info("关注");
				} else if (total == 1) {
					rs.put(Constants.FANS, num);
//					LOG.info("粉丝");
				} else {
					rs.put(Constants.posts, num);
//					LOG.info("微博");
				}
//				LOG.info(num);
				total++;
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
	}

//	private static void getCompanyAuthentication(TagNode root,Map<String, Object> rs,Task task) {
//		System.out.println();
//		try {
//
//			Object[] objs = root.evaluateXPath("//span[@class='icon_bed W_fl']/a[@class='W_icon icon_verify_co_v']");
//			if(objs.length>0){
//				rs.put(Constants.isChecked, 1);
//			}
//			Object[] objs2 = root.evaluateXPath("//span[@class='icon_group S_line1 W_fl']/a[1]");
//			if(objs2.length>0){
//				TagNode a = (TagNode)objs2[0];
//				rs.put(Constants.level, AWeiBoParser.getNums(a.getAttributeByName("title"),task));
//			}
//			rs.put(Constants.url, task.getTaskData().get("url"));
//		} catch (XPatherException e) {
//			e.printStackTrace();
//		}
//
//	}
	
	
	private static void getAuthentication(TagNode root,Map<String, Object> rs,Task task) {
//		System.out.println();
		try {
//			TagNode username = (TagNode)root.evaluateXPath("//div[@class='pf_name bsp clearfix']/span[1]")[0];
//			LOG.info(task.getTaskData().get("url")+":username:"+username.getText().toString());
//			rs.put(Constants.username, username.getText().toString());
			Object[] objs = root.evaluateXPath("//span[@class='icon_bed W_fl']/a[@class='W_icon icon_verify_v']");
			if(objs.length==0){
				objs = root.evaluateXPath("//span[@class='icon_bed W_fl']/a[@class='W_icon icon_verify_co_v']");
			}
			if(objs.length>0){
				rs.put(Constants.isChecked, 1);
			}
			Object[] objs2 = root.evaluateXPath("//span[@class='icon_group S_line1 W_fl']/a[1]");
			Object[] objs3 = root.evaluateXPath("//span[@class='item_text W_fl']/a[1]");//特殊情况
			if(objs2.length>0){
				TagNode a = (TagNode)objs2[0];
				rs.put(Constants.level, AWeiBoParser.getNums(a.getAttributeByName("title"),task));
			}
			if(objs3.length>0){
				TagNode a = (TagNode)objs3[0];
				if(a.getAttributeByName("title")!=null){
					rs.put(Constants.level, AWeiBoParser.getNums(a.getAttributeByName("title"),task));
				}
			}
			rs.put(Constants.url, task.getTaskdata().get("url"));
//			for(int i=0;i<objs.length;i++){
//				TagNode a = (TagNode)objs[i];
//				Object[] is = a.evaluateXPath("i");
//				if(is.length>0){
//					TagNode iTag = (TagNode)is[0];
//					if(iTag.getAttributeByName("class").equals("W_ico16 approve")){
////						LOG.info(task.getTaskData().get("url")+" is yellow check");
//						rs.put(Constants.isChecked, 1);
//						continue;
//					}else if(iTag.getAttributeByName("class").equals("W_ico16 ico_member6")){
////						LOG.info(task.getTaskData().get("url")+" is 微博会员");
//						rs.put(Constants.isVIP, 1);
//						continue;
//					}
//				}
//				Object[] spans = a.evaluateXPath("span[1]/span[1]");
//				if(spans.length>0){
//					TagNode span = (TagNode)spans[0];
////					LOG.info(task.getTaskData().get("url")+" 等级："+span.getAttributeByName("title"));
//					rs.put(Constants.level, AWeiBoParser.getNums(span.getAttributeByName("title"),task));
//					continue;
//				}
//				LOG.info(task.getTaskData().get("url")+" url:"+a.getText().toString());
//				rs.put(Constants.url, task.getUrl());
//			}
			

		} catch (XPatherException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
		// 个人用户信息url
		String url = "http://weibo.com/u/5304136460";
//		String url = "http://weibo.com/tcljituan";
		String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
//		String cookie = "SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=6932266778863.946.1411904891096; ULV=1422237427131:35:10:2:3114598317843.8784.1422237426834:1422165428612; UOR=,,www.huafans.cn; SUHB=0D-Eek6iSMM-fn; wvr=6; ALF=1453773411; YF-Page-G0=1ac418838b431e81ff2d99457147068c; SUS=SID-1661429500-1422237413-GZ-ye12h-0d9388dc513aa67c0ae9aa71c31582b6; SUE=es%3De93b38f2e6ce011b99491c53a500a311%26ev%3Dv1%26es2%3De75c889249c88e0bf67d7b3f712439c1%26rs0%3DGlrtR7IqPmfjVjS1aZAHycv2frjjm8vC8%252Bu2cqa9dXyA6mjPFUa3QYG9R6cQyyvJQdC4AT2WPhpFWK4Gd0XeCYhw0%252FKcyhw7HuaDn5HMVmmvqb3bDOICxUb7LRZYaLtdllE8YbHs5F0pfKX6S9RCnBa2f4k8F3EsHeq%252Ffdt%252B%252FOc%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1422237413%26et%3D1422323813%26d%3Dc909%26i%3D82b6%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D2%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2014-09-14%252019%253A14%253A24; SUB=_2A255we61DeTxGedI7VMV8ifJyzyIHXVat0d9rDV8PUNbvtBeLU7HkW8WJeZbxQkbq079e0PHljTYHsk-Zw..; SSOLoginState=1422237413; _s_tentry=login.sina.com.cn; Apache=3114598317843.8784.1422237426834; YF-V5-G0=654e6fc483798450a4af0cd83e3ea951; YF-Ugrow-G0=06497fcc8b94aee69ae834868fe61846";
		String html = client.getPageData(url, "0","Csina","item", refer, AWeiBoParser.cookie,"");
//		String decodehtml = MyStringUtil.loadConvert(html);
//		LOG.info("html:"+html);
		Task task = AWeiBoParser.getTestTask(url);
		try {
			new UserInfoParser().parseHtml(html, task);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.exit(0);
	}
}
