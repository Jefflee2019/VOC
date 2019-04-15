package com.bfd.parse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.DateTime;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class DailydataToexcel {
	public static void main(String[] args) {
		String source = "nbusinesssohu | cmsnewscontent | 1745 | | nmbalib | cmsnewscontent | 20 | | ntonghuashun | cmsnewscontent | 2862 | | nfundeastmoney | cmsnewscontent | 683 | | nyicai | cmsnewscontent | 63 | | nfinanceyouth | cmsnewscontent | 107 | | njingjicyol | cmsnewscontent | 17 | | ncaijing | cmsnewscontent | 19 | | necontaiwan | cmsnewscontent | 13 | | neastmoney | cmsnewscontent | 11210 | | nwallstreet | cmsnewscontent | 104 | | ncngold | cmsnewscontent | 543 | | nsacnet | cmsnewscontent | 5 | | nyunzguba | cmsnewscontent | 675 | | nguminba | cmsnewscontent | 10 | | nfinancechina | cmsnewscontent | 724 | | nchinabgao | cmsnewscontent | 45 | | nfinanceqianlong | cmsnewscontent | 262 | | nsouthcn | cmsnewscontent | 40 | | ndahe | cmsnewscontent | 20 | | ndzwww | cmsnewscontent | 99 | | nfinancecqnews | cmsnewscontent | 19 | | ncnfol | cmsnewscontent | 1767 | | ngxnews | cmsnewscontent | 13 | | nfinanceeastday | cmsnewscontent | 64 | | ncrionline | cmsnewscontent | 1 | | nzhicheng | cmsnewscontent | 142 | | naskci | cmsnewscontent | 10 | | nstockeastmoney | cmsnewscontent | 1003 | | nfinancece | cmsnewscontent | 345 | | neconomygmw | cmsnewscontent | 18 | | nftchinese | cmsnewscontent | 80 | | neconomyjschina | cmsnewscontent | 154 | | nrong360 | cmsnewscontent | 435 | | ncaijingchinadaily | cmsnewscontent | 5 | | njrj | cmsnewscontent | 4794 | | npeoplefinance | cmsnewscontent | 12 | | nfinancejxnews | cmsnewscontent | 4 | | ncsrcgov | cmsnewscontent | 11 | | nquanjing | cmsnewscontent | 402 | | njingjicctv | cmsnewscontent | 79 | | nwwwocn | cmsnewscontent | 432 | | nsouthmoney | cmsnewscontent | 1091 | | nstockhx | cmsnewscontent | 83 | | nfinancecnr | cmsnewscontent | 33 | | nstockstar | cmsnewscontent | 1965 | | ncaixin | cmsnewscontent | 16 | | nstcn | cmsnewscontent | 138 | | nstockcngold | cmsnewscontent | 86 | | nchinanews | cmsnewscontent | 96 | | nqutoeastmoney | cmsnewscontent | 120 | | ncnstock | cmsnewscontent | 295 | | n21jingji | cmsnewscontent | 59 | | nhowbuy | cmsnewscontent | 936 | | ncailianpress | cmsnewscontent | 16 | | nccstock | cmsnewscontent | 89 | | ntmtpost | cmsnewscontent | 32 | | ntakungpao | cmsnewscontent | 14 | | ncbcom | cmsnewscontent | 7 | | nxinhuanet | cmsnewscontent | 91 | | nmocgov | cmsnewscontent | 72 | | nemoney | cmsnewscontent | 560 | | nxsbdzw | cmsnewscontent | 2 | | nhongzhoukan | cmsnewscontent | 149 | | nineeq | cmsnewscontent | 4 | | nwabei | cmsnewscontent | 20 | | ncaiguu | cmsnewscontent | 17 | | nmenet | cmsnewscontent | 5 | | nmysteel | cmsnewscontent | 253 | | nchem99 | cmsnewscontent | 5 | | ndcement | cmsnewscontent | 9 | | nglassinfo | cmsnewscontent | 2 | | n21so | cmsnewscontent | 2 | | nmutongzx | cmsnewscontent | 5 | | nzhiding | cmsnewscontent | 6 | | ncankaoxiaoxi | cmsnewscontent | 40 | | ncyzone | cmsnewscontent | 3 | | ntaoguba | cmsnewscontent | 5 | | nmeinet | cmsnewscontent | 9 | | nchem100 | cmsnewscontent | 18 | | nzyzhan | cmsnewscontent | 27 | | ncnlist | cmsnewscontent | 16 | | nchinaepe | cmsnewscontent | 1 | | n10000link | cmsnewscontent | 3 | | nccmn | cmsnewscontent | 42 | | n1718china | cmsnewscontent | 8 | | ncableabc | cmsnewscontent | 6 | | nnewseccn | cmsnewscontent | 9 | | ncpnn | cmsnewscontent | 4 | | nmmsonline | cmsnewscontent | 32 | | n21sun | cmsnewscontent | 48 | | ncnal | cmsnewscontent | 16 | | ncnmn | cmsnewscontent | 8 | | nncw365 | cmsnewscontent | 10 | | n100ppi | cmsnewscontent | 321 | | nsmn | cmsnewscontent | 86 | | nport | cmsnewscontent | 7 | | ncheaa | cmsnewscontent | 16 | | naweb | cmsnewscontent | 7 | | ncaam | cmsnewscontent | 2 | | ncsi | cmsnewscontent | 12 | | nwinshang | cmsnewscontent | 53 | | ncsia | cmsnewscontent | 24 | | nbjx | cmsnewscontent | 120 | | npowerpigs | cmsnewscontent | 25 | | nchemall | cmsnewscontent | 17 | | ngov | cmsnewscontent | 13 | | niresearch | cmsnewscontent | 3 | | nbioon | cmsnewscontent | 36 | | nccidcom | cmsnewscontent | 9 | | ngasinen | cmsnewscontent | 19 | | ncnii | cmsnewscontent | 3 | | npharmnet | cmsnewscontent | 10 | | nsogou | cmsnewscontent | 1475 | | nmicrobell | cmsnewscontent | 1800 ";

		// excel 文档输出路径
		String outputExcelFile = "G:\\360Downloads\\ribao\\数据量日报.xls";
		// String outputExcelFile = "C:\\User\\xxx-副本.xls";
		String[] sourceArray = source.split("\\| \\|");
		int sourcelength = source.length();
		// 组合所有的论坛数据
		Map<String, String> bbsMap = new TreeMap<String, String>();
		// 组合所有的新闻、电商数据
		Map<String, Map<String, String>> nesMap = new TreeMap<String, Map<String, String>>();
		String[] bbsSiteArray = { "Bmobile163", "Bhuafen", "Beastmoney" };
		String[] newsSiteArray = { "N21cn", "Nsina", "Nzhonghua", "Ntoutiao" };

		String[] ecweiboSiteArray = { "Ejd", "Esinamobile" };

		for (int i = 0; i < sourceArray.length; i++) {
			String[] subStr = sourceArray[i].split("\\|");
			String siteName = subStr[0];
			String pageType = subStr[1];
			String count = subStr[2];
			if (i == 0) {
				siteName = siteName.replace("[\\|]+", "");
			} else if (i == sourcelength - 1) {
				count = count.replace("[\\|]+", "");
			}
			// 转换首字母
			if (siteName.startsWith("b")) {
				siteName = 'B' + siteName.substring(1);
			} else if (siteName.startsWith("n")) {
				siteName = 'N' + siteName.substring(1);
			} else if (siteName.startsWith("e")) {
				siteName = 'E' + siteName.substring(1);
			}

			// 加入论坛数据
			if (pageType.equals("bbspost")) {
				bbsMap.put(siteName, count);
				continue;
			}
			// 加入新闻数据
			if (nesMap.containsKey(siteName)) {
				Map<String, String> subNESMap = nesMap.get(siteName);
				subNESMap.put(pageType, count);
			} else {
				Map<String, String> subNESMap = new HashMap<String, String>();
				subNESMap.put(pageType, count);
				nesMap.put(siteName, subNESMap);
			}
		}

		try {
			// 构建输出文档
			jxl.Workbook bookR = jxl.Workbook.getWorkbook(new FileInputStream(new File(outputExcelFile)));
			WritableWorkbook bookW = Workbook.createWorkbook(new File(outputExcelFile), bookR);
			// 设置单元格样式
			WritableFont font = new WritableFont(WritableFont.createFont("微软雅黑"), 11);
			WritableFont font2 = new WritableFont(WritableFont.createFont("微软雅黑"), 12);
			// DateFormat df = new DateFormat("yyyy/M/d");

			// 前三个sheet新加数据样式
			WritableCellFormat wcf0 = new WritableCellFormat();
			wcf0.setFont(font);
			wcf0.setAlignment(Alignment.RIGHT);
			wcf0.setBorder(Border.ALL, BorderLineStyle.THIN);

			// 前三个sheet背景色
			WritableCellFormat wcf1Left = new WritableCellFormat();
			wcf1Left.setFont(font);
			wcf1Left.setAlignment(Alignment.LEFT);
			wcf1Left.setVerticalAlignment(VerticalAlignment.CENTRE);
			wcf1Left.setBorder(Border.ALL, BorderLineStyle.THIN);
			wcf1Left.setBackground(Colour.LIGHT_TURQUOISE);

			// 前三个sheet背景色(右)
			WritableCellFormat wcf1Right = new WritableCellFormat();
			wcf1Right.setFont(font);
			wcf1Right.setAlignment(Alignment.RIGHT);
			wcf1Right.setBorder(Border.ALL, BorderLineStyle.THIN);
			wcf1Right.setBackground(Colour.LIGHT_TURQUOISE);

			// 后三个sheet新加数据样式
			WritableCellFormat wcf2 = new WritableCellFormat();
			wcf0.setFont(font);
			wcf0.setAlignment(Alignment.LEFT);
			wcf0.setBorder(Border.ALL, BorderLineStyle.THIN);

			// 前三个表头样式
			WritableCellFormat wcfHeadFirst3Sheet = new WritableCellFormat();
			wcfHeadFirst3Sheet.setFont(font2);
			wcfHeadFirst3Sheet.setAlignment(Alignment.CENTRE);
			wcfHeadFirst3Sheet.setVerticalAlignment(VerticalAlignment.CENTRE);
			wcfHeadFirst3Sheet.setBorder(Border.ALL, BorderLineStyle.THIN);
			wcfHeadFirst3Sheet.setBackground(Colour.LIGHT_GREEN);

			// 后三个表头样式
			WritableCellFormat wcfHeadEnd3Sheet = new WritableCellFormat();
			wcfHeadFirst3Sheet.setFont(font2);
			wcfHeadFirst3Sheet.setAlignment(Alignment.LEFT);
			wcfHeadFirst3Sheet.setBorder(Border.ALL, BorderLineStyle.THIN);
			wcfHeadFirst3Sheet.setBackground(Colour.LIGHT_GREEN);

			// 获取所有的sheet
			WritableSheet sheet0 = bookW.getSheet(0);
			WritableSheet sheet1 = bookW.getSheet(1);
			WritableSheet sheet2 = bookW.getSheet(2);
			WritableSheet sheet3 = bookW.getSheet(3);
			WritableSheet sheet4 = bookW.getSheet(4);
			WritableSheet sheet5 = bookW.getSheet(5);

			// 构造后三个sheet最后一列表头时间
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			Date theDayBefore = calendar.getTime();
			DateTime labeDT3 = new DateTime(8, 0, theDayBefore, wcfHeadEnd3Sheet);
			DateTime labeDT4 = new DateTime(8, 0, theDayBefore, wcfHeadEnd3Sheet);
			DateTime labeDT5 = new DateTime(8, 0, theDayBefore, wcfHeadEnd3Sheet);

			// 设置后三个sheet最后一列列宽
			CellView endColumnCV = new CellView();
			endColumnCV.setSize(12 * 256);

			// 初始化后三个sheet
			initSheet(sheet3, endColumnCV, labeDT3);
			initSheet(sheet4, endColumnCV, labeDT4);
			initSheet(sheet5, endColumnCV, labeDT5);

			// 写入论坛回帖数据
			for (int i = 0; i < bbsSiteArray.length; i++) {
				String bbsResult = bbsMap.get(bbsSiteArray[i]);
				int bbsNumber = 0;
				if (bbsResult != null) {
					bbsNumber = Integer.parseInt(bbsResult);
				}
				jxl.write.Number number = new jxl.write.Number(2, i + 5, bbsNumber, wcf0);
				jxl.write.Number numberForChart = new jxl.write.Number(8, i + 1, bbsNumber, wcf2);
				sheet0.addCell(number);
				sheet3.addCell(numberForChart);
			}

			// 写入新闻内容评论数量
			for (int i = 0; i < newsSiteArray.length; i++) {
				String newSiteName = newsSiteArray[i];
				Map<String, String> subNESMap = nesMap.get(newSiteName);
				int content = 0;
				int comment = 0;
				if (subNESMap != null) {
					String newscontent = subNESMap.get("newscontent");
					String newscomment = subNESMap.get("newscomment");
					if (newscontent == null) {
						newscontent = "0";
					}
					if (newscomment == null) {
						newscomment = "0";
					}
					content = Integer.parseInt(newscontent);
					comment = Integer.parseInt(newscomment);
				}

				jxl.write.Number numberContent = new jxl.write.Number(2, i + 5, content, wcf0);
				jxl.write.Number numberComment = new jxl.write.Number(3, i + 5, content, wcf0);
				jxl.write.Number numberForChart = new jxl.write.Number(8, i + 1, comment, wcf2);
				sheet1.addCell(numberContent);
				sheet1.addCell(numberComment);
				sheet4.addCell(numberForChart);
			}

			// 写入电商内容评论数量
			for (int i = 0; i < ecweiboSiteArray.length; i++) {
				String ecweiboSiteName = ecweiboSiteArray[i];
				Map<String, String> subNESMap = nesMap.get(ecweiboSiteName);
				if (ecweiboSiteName.startsWith("E")) {
					int content = 0;
					int comment = 0;
					if (subNESMap != null) {
						String eccontent = subNESMap.get("eccontent");
						String eccomment = subNESMap.get("eccomment");
						if (eccontent == null) {
							eccontent = "0";
						}
						if (eccomment == null) {
							eccomment = "0";
						}
						comment = Integer.parseInt(eccomment);
						content = Integer.parseInt(eccontent);
					}
					jxl.write.Number numberContent = new jxl.write.Number(3, i + 5, content, wcf0);
					jxl.write.Number numberComment = new jxl.write.Number(4, i + 5, comment, wcf0);
					jxl.write.Number numberForChart = new jxl.write.Number(8, i + 1, comment, wcf2);

					sheet2.addCell(numberContent);
					sheet2.addCell(numberComment);
					sheet5.addCell(numberForChart);
				}
			}

			// 改变sprint1论坛站点背景色
			changeBackColour(sheet0, wcf1Left, wcf1Right, 0, 5, 0, 22);
			// 改变sprint1新闻站点背景色
			changeBackColour(sheet1, wcf1Left, wcf1Right, 0, 6, 0, 34);
			// 改变sprint1电商站点背景色
			changeBackColour(sheet2, wcf1Left, wcf1Right, 0, 7, 0, 5);
			// 改变sprint1微博站点背景色
			changeBackColour(sheet2, wcf1Left, wcf1Right, 0, 8, 0, 12);

			// 关闭资源
			bookW.write();
			bookW.close();
			bookR.close();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public static void initSheet(WritableSheet sheet, CellView endColumCV, DateTime labeDT)
			throws RowsExceededException, WriteException {
		sheet.removeColumn(2);
		sheet.addCell(labeDT);
		sheet.setColumnView(8, endColumCV);
	}

	public static void changeBackColour(WritableSheet sheet, WritableCellFormat wcf1Left, WritableCellFormat wcf1Right,
			int columnStart, int columnEnd, int rowStart, int rowEnd) throws WriteException, RowsExceededException {
		for (int i = columnStart; i < columnEnd; i++) {
			for (int j = rowStart; j < rowEnd; j++) {
				Cell cell = sheet.getCell(i, j + 5);
				Colour colour = cell.getCellFormat().getBackgroundColour();
				String content = cell.getContents();
				// 192代表表格默认背景色
				if (colour.getValue() == 192 && content.matches("^\\d+$")) {
					jxl.write.Number number = new jxl.write.Number(i, j + 5, Integer.parseInt(sheet.getCell(i, j + 5)
							.getContents()), wcf1Right);
					sheet.addCell(number);
				}
			}
		}
	}
}
