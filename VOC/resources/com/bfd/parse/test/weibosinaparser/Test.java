package com.bfd.parse.test.weibosinaparser;

import java.util.regex.Matcher;

import com.bfd.parse.client.DownloadClient;

public class Test {

	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
		String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
		String cookie = "SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=6932266778863.946.1411904891096; ULV=1422237427131:35:10:2:3114598317843.8784.1422237426834:1422165428612; UOR=,,www.huafans.cn; SUHB=0D-Eek6iSMM-fn; wvr=6; ALF=1453773411; YF-Page-G0=1ac418838b431e81ff2d99457147068c; SUS=SID-1661429500-1422237413-GZ-ye12h-0d9388dc513aa67c0ae9aa71c31582b6; SUE=es%3De93b38f2e6ce011b99491c53a500a311%26ev%3Dv1%26es2%3De75c889249c88e0bf67d7b3f712439c1%26rs0%3DGlrtR7IqPmfjVjS1aZAHycv2frjjm8vC8%252Bu2cqa9dXyA6mjPFUa3QYG9R6cQyyvJQdC4AT2WPhpFWK4Gd0XeCYhw0%252FKcyhw7HuaDn5HMVmmvqb3bDOICxUb7LRZYaLtdllE8YbHs5F0pfKX6S9RCnBa2f4k8F3EsHeq%252Ffdt%252B%252FOc%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1422237413%26et%3D1422323813%26d%3Dc909%26i%3D82b6%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D2%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2014-09-14%252019%253A14%253A24; SUB=_2A255we61DeTxGedI7VMV8ifJyzyIHXVat0d9rDV8PUNbvtBeLU7HkW8WJeZbxQkbq079e0PHljTYHsk-Zw..; SSOLoginState=1422237413; _s_tentry=login.sina.com.cn; Apache=3114598317843.8784.1422237426834; YF-V5-G0=654e6fc483798450a4af0cd83e3ea951; YF-Ugrow-G0=06497fcc8b94aee69ae834868fe61846";
		String url = "http://weibo.com/p/1006051172294045/home";
		String html = client.getPageData(url, "0","Csina","item", refer, cookie,"");
		Matcher m = IWeiBoparser.getAllHtmlJson.matcher(html);
		int total = 0;
		while(m.find()){
			total++;
			System.out.println(total+"   "+m.group(1));
		}
		System.out.println(total);
		
	}

}
