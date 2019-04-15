package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：京东
 * 
 * 主要功能： 1、从商品名称中提取是否京东超市
 * 2、处理商品路径
 * 3、提取品牌
 * 4、获取价格
 * 
 * @author lth
 *
 */
public class Ejd_wlmContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String,Map<String,String>> contentimgs = arg2.getDomParser().getDomSearch().getContentimgs();
			boolean isMarket = false;
			// 是否京东超市
			/*if (itemname.contains("【京东超市】")) {
				isMarket = true;
			}*//*京东超市字样改版成图片-2018-05-24*/ 
			if(contentimgs.containsKey("img_0")) {
				Map<String,String> imgMap = (Map<String, String>) contentimgs.get("img_0");
					String img = imgMap.get("img").toString();
					//京东超市 https://img10.360buyimg.com/img/jfs/t8485/356/1281159143/15432/343a6ec9/59b73dbaN9c878bcc.png
					//京东物流https://img14.360buyimg.com/devfe/jfs/t15784/22/2413631489/1215/d3a17885/5aa5db0bNf79d323c.png
					//京东精选https://img10.360buyimg.com/devfe/jfs/t3235/8/5095608208/4769/3409ca1c/5860da3eNe40e081e.png
					if(img.contains("img10")&&img.contains("t8485")) {
						isMarket = true;
					}
					resultData.put("isMarket", isMarket);
			}

		// 品牌
		if (resultData.containsKey("parameter")) {
			String parameter = resultData.get("parameter").toString();
			String regex = "品牌：\\s*(\\S*)";
			Matcher match = Pattern.compile(regex).matcher(parameter);
			if (match.find()) {
				String brand = match.group(1);
				resultData.put("brand", brand);
			}
		}

		/**
		 * function:处理cate,将cate分成目录、品牌、产品三部分,并且品牌只取第一个
		 * 处理前："cate":["食品饮料",">","休闲食品"
		 * ,">","糖果/巧克力",">","金冠",">","金冠 黑糖话梅糖160g..."]
		 * 处理后："cate":["食品饮料$休闲食品$糖果/巧克力","金冠","金冠 黑糖话梅糖160g..."
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cateList = (List<String>) resultData.get(Constants.CATE);
			if (cateList != null && !cateList.isEmpty()) {
				// 去除">"
				Iterator<String> it = cateList.iterator();
				while (it.hasNext()) {
					String st = (String) it.next();
					if (st.equals(">")) {
						it.remove();
					}
				}
				// 将cate分成目录、品牌、产品三部分
				List<String> cate = new ArrayList<String>();
				StringBuffer sb = new StringBuffer();
				// "cate": ["", "","施华蔻","施华蔻男士洗发水薄荷活力洗发露450/200ml控油补水洗头.."]
				// cate 存在如上前部分为空的情况
				if (cateList.size() - 2 > 0) {
					for (int i = 0; i < cateList.size() - 2; i++) {
						if (cateList.get(i).equals("")) {
							continue;
						} else {
							sb.append(cateList.get(i)).append("$");
						}
					}
					if (sb.length() != 0) {
						sb = sb.deleteCharAt(sb.length() - 1);
						cate.add(sb.toString());
					}
					// 重新组装cate
					String brand = cateList.get(cateList.size() - 2);
					String[] brandarr = brand.split("	");
					// //品牌只取第一个
					if (brandarr.length > 1) {
						brand = brandarr[0];
					}
					cate.add(brand);
					cate.add(cateList.get(cateList.size() - 1));
					resultData.put(Constants.CATE, cate);
				}
			}
		}
		
		//处理价格
		//https://item.jd.com/12045875084.html?price=29.80
		/**
		 * @function 处理价格
		 * @note 如果json插件中获取到价格这儿就不做处理；如果不能获取，就从url获取
		 * 价格从列表页mysql表中关联取得，不再请求京东价格借口和拼接url获取--2017/10/26
		 */
/*		if (!resultData.containsKey("sz_price")) {
			String url = unit.getUrl();
			Matcher match = Pattern.compile("\\?price=(\\S*)").matcher(url);
			if (match.find()) {
				Double price = Double.parseDouble(match.group(1));
				resultData.put("sz_price", price);
				resultData.put("cd_price", price);
				resultData.put("sh_price", price);
				resultData.put("bj_price", price);
			}
		}*/

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
