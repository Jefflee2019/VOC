package com.bfd.parse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.reprocess.ReProcessorFactory;
import com.bfd.parse.task.ParseJob;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * key: length bid charset
 * 
 * @author yanhui.ji
 * 
 */
public class FileParser {

	private static final Log LOG = LogFactory.getLog(FileParser.class);

	public static final String url = "url";
	public static final String charset = "charset";
	public static final String type = "type";
	public static final String cid = "cid";
	public static final String length = "length";
	private static Set<String> requireds;
	static {
		requireds = new HashSet<String>();
		requireds.add(url);
		requireds.add(cid);
		requireds.add(length);
	}

	private FileWriter fileWriter = null;

	private DomParser domParser;

	private Map<String, String> outputFlds = null;

	public FileParser() {
		domParser = new DomParser();
	}

	public void parse(ParseJob.FileParseUnit unit) {
		outputFlds = unit.getOutPutFlds();
		if (outputFlds == null) {
			outputFlds = defaultOutputs();
		} else if (!outputFlds.values().containsAll(requireds)) { //
			LOG.info("Request params don't contain all requirements");
			return;
		}
		handleFiles(unit.getFilename(), unit.getResfile(), unit.getPrefix(), unit.getBid(), unit.getCharset(),
				unit.getType());
	}

	private void handleFiles(String inPath, String outPath, final String prefix, String cid, String charset, String type) {
		File inFile = new File(inPath);
		if (inFile.exists() && inFile.isFile() && inFile.canRead()) {
			handleFile(inFile, outPath, cid, charset, type);
		} else if (inFile.exists() && inFile.isDirectory()) {
			File[] listFiles = null;
			if (StringUtils.isNotEmpty(prefix)) {
				listFiles = inFile.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.getName().startsWith(prefix))
							return true;
						return false;
					}

				});
			} else {
				listFiles = inFile.listFiles();
			}
			for (File file : listFiles) {
				if (file.isFile() && file.canRead()) {
					handleFile(file, outPath, cid, charset, type);
				}
			}
		}
	}

	private Map<String, String> defaultOutputs() {
		Map<String, String> map = new HashMap<String, String>();
		for (String required : requireds) {
			map.put(required, required);
		}
		return map;
	}

	private void handleFile(File file, String outPath, String aCid, String aCharset, String aType) {
		outPath = createFile(file.getName(), outPath);
		if (outPath == null) {
			LOG.error("creating file=" + outPath + " failed. ");
			return;
		}
		StringBuilder sBuilder = new StringBuilder();
		RandomAccessFile raf = null;
		int total = 0;
		int success = 0;
		try {
			raf = new RandomAccessFile(file, "r");
			while (true) {
				String _type = null, _cid = null, _url = null, _charset = null;
				int len = 0;
				Map<String, Object> parseRes = new HashMap<String, Object>();
				String line = null;
				while ((line = raf.readLine()) != null) {
					if (line.length() == 0) {
						continue;
					}
					int index = line.indexOf(":");
					if (index + 1 > line.length()) { // TODO
						LOG.error("有问题的文件" + "|" + "inputPath" + file);
						break;
					}
					String value = line.substring(index + 1);
					String _key = line.substring(0, index);

					Collection<String> keys = outputFlds.keySet();
					if (keys.contains(_key)) {
						String key = outputFlds.get(_key);
						parseRes.put(key, DataUtil.decode(value));
						if (type.equals(key)) {
							_type = value;
							if (!_type.equalsIgnoreCase("item") || !_type.equalsIgnoreCase("list")
									|| !_type.equalsIgnoreCase("info"))
								continue;
						}
						if (cid.equals(key)) {
							_cid = value;
						}
						if (url.equals(key)) {
							_url = value;
						}
						if (charset.equals(key)) {
							_charset = value;
						}
						if (length.equals(key)) {
							len = Integer.parseInt(value);
							break;
						}
					}
				}
				total++;
				if (len == 0) {
					break;
				}
				if (_charset == null) {
					_charset = aCharset;
				}
				if (_type == null) {
					_type = aType;
				}
				// 强制bid
				if (StringUtils.isNotEmpty(aCid)) {
					_cid = aCid;
				}

				byte[] bs = new byte[len];
				raf.read(bs);

				LOG.info("begin inPath=" + file + ", url=" + _url);
				Map<String, Object> map = domParser.parseFile(_url, bs, _cid, _type, _charset);
				if (map == null || map.size() < 1) {
					LOG.debug("parse file failed, url=" + _url);
					continue;
				}
				success++;
				LOG.info("after inPath=" + file + ", url=" + _url);
				ReProcessor processor = ReProcessorFactory.getReProcessor(_cid, _type);
				parseRes.putAll(map);
				String jsonRes = createJson(outPath, parseRes);
				if (StringUtils.isNotEmpty(jsonRes) && !"{}".equals(jsonRes)) {
					sBuilder.append(jsonRes).append("\r\n");
				}
				if (sBuilder.length() > 0) {
					appendSave(sBuilder.toString(), outPath);
					sBuilder = new StringBuilder();
				}
			}
			LOG.info("parse task finished, inFile=" + file + ", outFile=" + outPath);
		} catch (Exception e) {
			LOG.error("handleFile exception:", e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					LOG.error("handleFile exception:", e);
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					LOG.error("handleFile exception:", e);
				} finally {
					fileWriter = null;
				}
			}
		}
		LOG.info("Parse finished, parse item count total=" + total + ", success=" + success + " inPath:" + file);
	}

	public String createFile(String inFileName, String outPath) {
		File file = new File(outPath);
		try {
			if (file.exists() && file.isFile()) { // 目标文件已存在
				LOG.info("output file is already exist. file=" + outPath);
				return outPath;
			} else if (file.exists() && file.isDirectory()) {
				String out = outPath + "parse_" + inFileName;
				file = new File(out);
				if (!file.exists()) {
					file.createNewFile();
				}
				return out;
			} else if (!file.exists() && outPath.endsWith(File.separator)) {
				outPath = outPath + "parse_" + inFileName;
				file = new File(outPath);
				if (file.getParentFile().mkdirs() && file.createNewFile()) {
					return outPath;
				}
				return null;
			} else if (!file.exists()) {
				if (file.getParentFile().mkdirs() && file.createNewFile()) {
					return outPath;
				}
				return null;
			}
		} catch (Exception e) {
			LOG.error("createFile exception:", e);
		}
		return null;
	}

	public String createJson(String path, Map<String, Object> map) {
		ObjectMapper om = new ObjectMapper();
		String jres = null;
		try {
			jres = om.writeValueAsString(map);
		} catch (Exception x) {
			LOG.info("json dump exception ", x);
		}
		return jres;
	}

	public boolean appendSave(String strXml, String outPath) {
		if (StringUtils.isEmpty(strXml)) {
			return false;
		}
		try {
			if (fileWriter == null) {
				File file = new File(outPath);
				if (file.exists() && file.canWrite()) {
					fileWriter = new FileWriter(file, true);
				} else {
					LOG.warn("file doesnt exist or cannt be writed, file is " + outPath);
					return false;
				}
			}
			fileWriter.write(strXml);
			return true;
		} catch (Exception e) {
			LOG.warn("IOException while writing json to file:", e);
		}
		return false;
	}

}
