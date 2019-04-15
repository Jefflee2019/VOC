package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：手机之家 功能：新闻评论页后处理时间
 * 
 * @author bfd_05
 */

public class NzolCommentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NzolCommentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.COMMENTS)) {
			List<Map<String, Object>> comms = (List<Map<String, Object>>) resultData.get(Constants.COMMENTS);
			if (comms != null && !comms.isEmpty()) {
				for (Map<String, Object> comm : comms) {
					// 评论时间
					if (comm.containsKey(Constants.COMMENT_TIME)) {
						String time = comm.get(Constants.COMMENT_TIME).toString();
						comm.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(time));
					}

					// 评论点赞数
					if (comm.containsKey(Constants.UP_CNT)) {
						Pattern p = Pattern.compile("\\d+");
						String upCnt = (String) comm.get(Constants.UP_CNT);
						Matcher m = p.matcher(upCnt);
						if (m.find()) {
							comm.put(Constants.UP_CNT, m.group());
						} else {
							comm.put(Constants.UP_CNT, 0);
						}
					}
				}
			}
		}
		// 总评论数
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			String reply_cnt = resultData.get(Constants.REPLY_CNT).toString();
			if (reply_cnt.equals("")) {
				resultData.put(Constants.REPLY_CNT, 0);
			}
		}

		// 内容点赞数/点踩数动态数据,必须要带cookie才能下载，所以改在后处理插件处理
		String url = unit.getUrl();
		Matcher match = Pattern.compile("/(\\d+)_\\d+_\\d+_\\d+.html").matcher(url);
		if (match.find()) {
			String docId = match.group(1);
			String dynamicLink = new StringBuffer()
					.append("http://dynamic.zol.com.cn/channel/doc_view_2014.php?doc_id=").append(docId)
					.append("&callback=jQuery18304999241252704476").toString();
			httpDownloadForSupportCntNOpposeCnt(resultData, url, dynamicLink);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param url
	 * @param dynamicLink
	 * @function http带cookie下载新闻页点赞数/点踩数页面
	 */
	@SuppressWarnings("unchecked")
	private void httpDownloadForSupportCntNOpposeCnt(Map<String, Object> resultData, String url, String dynamicLink) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(dynamicLink);
		request.setHeader(
				"Cookie",
				"ip_ck=1Imu1o68v8EuMDQyMzI2LjE1MjQ1NTY2NDQ%3D; zol_userid=qq_7328156llyl4; zol_check=1798326617; zol_cipher=9feb586fbdbfb16d35a2764dfb7fc8c4; zol_sid=45781027; gr_user_id=8716f765-1cde-45b3-beaf-9ce18c692ac8; zol_bind_qq_7328156llyl4=1; vjuids=-19729d17c.164a0ef301a.0.f0843e0c7df26; vjlast=1531708387.1531708387.30; z_pro_city=s_provice%3Dguangdong%26s_city%3Dshenzhen; userProvinceId=30; userCityId=348; userCountyId=0; userLocationId=24; lv=1531810123; vn=11; zol_vest_no=qq_7328156llyl4; Hm_lvt_ae5edc2bc4fc71370807f6187f0a2dd0=1531735145,1531735204,1531793951,1531794883; Hm_lpvt_ae5edc2bc4fc71370807f6187f0a2dd0=1531810557; photourl=icon.zol-img.com.cn/group/detail_images/zoler.jpg; jd618Img=; dwhis=%22s%22%3A%2257%22%2C%22m%22%3A%22613%22%2C%22p%22%3A%221185759%22%2C%22function%20%28e%29%7Breturn%22%5Bobject%20Array%5D%22%3D%3D%3D%7B%7D.toString.call%28e%29%7D%22%3A%22%22; questionnaire_pv=1531785608; z_day=izol102029%3D3%26ixgo20%3D1%26izol101592%3D2; Adshow=4");
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpResponse response;
		try {
			response = client.execute(request);
			String data = EntityUtils.toString(response.getEntity(), "gbk");
			// 格式化动态数据
			if (data.indexOf("[") >= 0 && data.indexOf("]") >= 0 && (data.indexOf("[") < data.indexOf("{"))) {
				data = data.substring(data.indexOf("["), data.lastIndexOf("]") + 1);
			} else if (data.indexOf("{") >= 0 && data.indexOf("}") > 0) {
				data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
			}
			Object dataObject = JsonUtil.parseObject(data);
			if (dataObject instanceof Map) {
				Map<String, Object> dataMap = (Map<String, Object>) dataObject;
				// 内容点赞数 support_cnt
				if (dataMap.containsKey("like_hits")) {
					String supportCnt = dataMap.get("like_hits").toString();
					resultData.put(Constants.SUPPORT_CNT, Integer.parseInt(supportCnt));
				}

				// 点踩数 oppose_cnt
				if (dataMap.containsKey("dislike_hits")) {
					String opposeCnt = dataMap.get("dislike_hits").toString();
					resultData.put(Constants.OPPOSE_CNT, Integer.parseInt(opposeCnt));
				}
			}
		} catch (Exception e) {
			LOG.error("httprequest download failed" + url);
		}
	}
}
