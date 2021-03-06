/*
 *author:olunx
 *date:2009-10-12
 */

package com.olunx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;

import com.olunx.MainActivity;
import com.olunx.db.CsvHelper;
import com.olunx.db.RememberHelper;
import com.olunx.option.mandict.GetCsvInfo;
import com.olunx.option.mandict.GetDictList;
import com.olunx.option.mandict.GetStarDictInfo;

public class Config {

	private static Config config = null;

	public static final String DICTTYPE = "dict_type";
	public static final String DICTTYPE_CSV = "csv";
	public static final String DICTTYPE_STARDICT = "ifo";

	public static final String SELECTTYPE = "select_type";
	public static final String SELECT_DICTDIR = "dict_dir";
	public static final String SELECT_SOUNDDIR = "sound_dir";

	public static final String SPEECHTYPE = "speech_type";
	public static final int SPEECH_TTS = 1;
	public static final int SPEECH_REAL = 2;

	private static final String SDCARD_PATH = "/sdcard/iremember/";
	public static final String SDCARD_SOUND_PATH = "/sdcard/iremember/sound/";
	private static final String SDCARD_STARDICT_PATH = "/sdcard/iremember/stardict-dicts-all/";
	private static final String SDCARD_BACKUP_PATH = "/sdcard/iremember/backup/";

	public static final String DATABASE_FILE = "data.db";
	public static final String FILE_SDCARD_DATABASE = SDCARD_PATH + DATABASE_FILE;
	public static final String BACKUP_FILE_SDCARD_DATABASE = SDCARD_BACKUP_PATH + DATABASE_FILE;

	public static final String CONFIG_FILE = "config.propertites";
	public static final String FILE_SDCARD_CONFIG = SDCARD_PATH + CONFIG_FILE;
	public static final String BACKUP_FILE_SDCARD_CONFIG = SDCARD_BACKUP_PATH + CONFIG_FILE;

	public static String FONT_KINGSOFT_PATH = "font/KingSoft-Phonetic-Android.ttf";

	public static Properties p;

	private static Context context = MainActivity.context;

	public static Config init() {
		if (config == null) {
			config = new Config();
		}
		if (p == null) {
			Utils.init().createFileIfNotExist(FILE_SDCARD_CONFIG);
			p = new Properties();
			try {
				p.load(new BufferedInputStream(new FileInputStream(new File(FILE_SDCARD_CONFIG))));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}

	/**
	 * 设置配置
	 * 
	 * @param key
	 * @param value
	 */
	public void setCon(String key, String value) {
		p.setProperty(key, value);
		// 保存配置文件
		try {
			p.store(new BufferedOutputStream(new FileOutputStream(new File(Config.FILE_SDCARD_CONFIG))), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取配置
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getCon(String key, String defValue) {
		String value = p.getProperty(key);
		if (value == null) {
			return defValue;
		}
		return value;
	}

	public void copyOneAssetsFile(String assetName, String dest) {
		try {
			InputStream in = context.getAssets().open(assetName);
			Utils.init().copyFile(in, dest);			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 第一次运行，初始化数据。
	 */
	public void initInstall() {
		if (isFirstRun()) {
			// 安装词库
			String cet4File = "/sdcard/iremember/大学英语四级.csv";
			String cet6File = "/sdcard/iremember/大学英语六级.csv";
			String gaokaoFile = "/sdcard/iremember/高考英语词汇.csv";
			String tuofuFile = "/sdcard/iremember/tofel.csv";
			String yasiFile = "/sdcard/iremember/雅思词汇.csv";
			String greFile = "/sdcard/iremember/gre.csv";
			
			//目录说明
			String soundFile = SDCARD_SOUND_PATH + "读我.txt";
			String stardictFile = SDCARD_STARDICT_PATH + "读我.txt";
			
			String stardict_babylon_dir = SDCARD_STARDICT_PATH + "stardict-babylon-WordNet-2.4.2/";

			copyOneAssetsFile("dicts/cet4.csv", cet4File);
			copyOneAssetsFile("dicts/cet6.csv", cet6File);
			copyOneAssetsFile("dicts/gaokao.csv", gaokaoFile);
			copyOneAssetsFile("dicts/tofel.csv", tuofuFile);
			copyOneAssetsFile("dicts/yasi.csv", yasiFile);
			copyOneAssetsFile("dicts/gre.csv", greFile);

			copyOneAssetsFile("zh-cn/readme_sound.txt", soundFile);

			copyOneAssetsFile("zh-cn/readme_stardict.txt", stardictFile);

                        /*
			 *copyOneAssetsFile("dicts/stardict-babylon-WordNet-2.4.2/WordNet.syn", stardict_babylon_dir+"WordNet.syn");
			 *copyOneAssetsFile("dicts/stardict-babylon-WordNet-2.4.2/WordNet.ifo", stardict_babylon_dir+"WordNet.ifo");
			 *copyOneAssetsFile("dicts/stardict-babylon-WordNet-2.4.2/WordNet.idx", stardict_babylon_dir+"WordNet.idx");
			 *copyOneAssetsFile("dicts/stardict-babylon-WordNet-2.4.2/WordNet.idx.idx", stardict_babylon_dir+"WordNet.idx.idx");
			 *copyOneAssetsFile("dicts/stardict-babylon-WordNet-2.4.2/WordNet.dict.dz", stardict_babylon_dir+"WordNet.dict.dz");
                         */

			this.setDefaultConfig();
			this.setFirstRun("false");
		}
	}

	/**
	 * 设置是否第一次运行
	 */
	public void setFirstRun(String value) {
		this.setCon("first_run", value);
	}

	public boolean isFirstRun() {
		return Boolean.valueOf(getCon("first_run", "true"));
	}
	
	private void setDictCurrentUse(final String dictName) {
	    Config.init().setCurrentUseDictWordCount(new GetCsvInfo(Config.init().getDictPath(dictName)).getWordCount());
	    Config.init().setCurrentUseDictName(dictName);
	    Config.init().cleanRememberLine();
	}
	/**
	 * 复位配置文件
	 * 
	 * @param context
	 */
	public void setDefaultConfig() {
		this.setDictDir(SDCARD_PATH);
		this.setSoundDir(SDCARD_SOUND_PATH);
		this.setSpeechType(0);
		this.setEachLessonWordCount("12");// 每课单词数
		this.setLessonCount("0");// 课程数
		this.setCurrentUseDictName("gre.csv");// 记忆词库
		this.setCurrentUseTransDictName("");// 例句词典
		this.setCanGetTransDict(true);// 是否可用例句词典
		this.setDictStringArray("", Config.DICTTYPE_CSV);
		this.setDictStringArray("", Config.DICTTYPE_STARDICT);
		this.setDictCharset("UTF-8");
		this.setCanGetTransDict(false);
		this.setCurrentUseTransDictName("null");
		this.setCanSpeech(false);
		this.setFirstRun("true");
		this.cleanRememberLine();
		GetDictList gdl = new GetDictList();
		gdl.getList();
		this.setDictCurrentUse("gre");
	}

	/**
	 * 初记单词位置
	 * 
	 * @param lesson
	 * @param value
	 */
	public void setPreviewWordIndex(int lesson, int value) {
		this.setCon(getCurrentUseDictName() + "_preview_word_index_" + lesson, String.valueOf(value));
	}

	public int getPreviewWordIndex(int lesson) {
		return Integer.parseInt(getCon(getCurrentUseDictName() + "_preview_word_index_" + lesson, String.valueOf(0)));
	}

	/**
	 * 复习单词位置
	 * 
	 * @param lesson
	 * @param value
	 */
	public void setReviewWordIndex(int lesson, int value) {
		this.setCon(getCurrentUseDictName() + "_review_word_index_" + lesson, String.valueOf(value));
	}

	public int getReviewWordIndex(int lesson) {
		return Integer.parseInt(getCon(getCurrentUseDictName() + "_review_word_index_" + lesson, String.valueOf(0)));
	}

	/**
	 * 设置词典目录
	 * 
	 * @param dictDir
	 */
	public void setDictDir(String dictDir) {
		this.setCon("config_dict_dir", dictDir);
	}

	public String getDictDir() {
		return getCon("config_dict_dir", SDCARD_PATH);
	}

	/**
	 * 设置语音库目录
	 * 
	 * @param dictDir
	 */
	public void setSoundDir(String dictDir) {
		this.setCon("config_sound_dir", dictDir);
	}

	public String getSoundDir() {
		return getCon("config_sound_dir", SDCARD_SOUND_PATH);
	}

	/**
	 * 设置每组单词数
	 * 
	 * @param wordCount
	 */
	public void setEachLessonWordCount(String wordCount) {
		this.setCon("config_each_lesson_word_count", wordCount);
	}

	public String getEachLessonWordCount() {
		return this.getCon("config_each_lesson_word_count", "25");
	}

	/**
	 * 设置课程数
	 * 
	 * @param context
	 * @param lessonCount
	 */
	public void setLessonCount(String lessonCount) {
		this.setCon("config_lesson_count", lessonCount);
	}

	public String getLessonCount() {

		String wordCount = this.getCurrentUseDictWordCount();
		String eachLessonWordCount = this.getEachLessonWordCount();

		int lessonCount = 0;
		int temp;
		if (eachLessonWordCount != null && eachLessonWordCount != "" && wordCount != "" && wordCount != null) {
			if (Integer.parseInt(eachLessonWordCount) >= (temp = Integer.parseInt(wordCount)) && temp > 0) {// 如果每组数大于总词数，则设组数为1。
				lessonCount = 1;
			} else {
				int intWordCount = Integer.parseInt(wordCount);
				int intEachLessonWordCount = Integer.parseInt(eachLessonWordCount);
				if (intEachLessonWordCount > 0) {
					lessonCount = intWordCount / intEachLessonWordCount;
					if (intWordCount % intEachLessonWordCount > 0) {
						lessonCount++;
					}
				}
			}
		}
		String strLessonCount = String.valueOf(lessonCount);
		this.setLessonCount(strLessonCount);

		return strLessonCount;
	}

	/**
	 * 设置词典列表
	 * 
	 * @param dictPathList
	 * @param dictType
	 */
	public void setDictList(ArrayList<String> dictPathList, String dictType) {
		// 保存数据
		String dictsArray = "";// 保存词库名称

		int dictListSize = dictPathList.size();
		Log.i("dictListSize", String.valueOf(dictListSize));
		String dictPath = null;
		String dictSize = null;
		String dictName = null;

		if (dictType.equalsIgnoreCase(Config.DICTTYPE_CSV)) {
			GetCsvInfo gci = null;
			for (int i = 0; i < dictListSize; i++) {
				dictPath = dictPathList.get(i);
				gci = new GetCsvInfo(dictPath);
				dictSize = gci.getFileSize();
				dictName = gci.getDictName();
				dictsArray = dictsArray + dictName + "|";// 将词库名称作为数组，方便获取

				this.setDictPath(dictName, dictPath);
				this.setDictType(dictName, dictType);
				this.setDictDesc(dictName, "大小：" + dictSize + "   类型：csv");
			}
		} else if (dictType.equalsIgnoreCase(Config.DICTTYPE_STARDICT)) {
			GetStarDictInfo gsi = null;
			for (int i = 0; i < dictListSize; i++) {
				dictPath = dictPathList.get(i);
				gsi = new GetStarDictInfo(dictPath);

				dictName = gsi.getDictName();
				dictsArray = dictsArray + dictName + "|";// 将词库名称作为数组，方便获取

				this.setDictPath(dictName, dictPath);
				this.setDictType(dictName, dictType);
				this.setDictDesc(dictName, "词数：" + gsi.getWordCount() + "   类型：StarDict");
			}
		}

		this.setDictStringArray(dictsArray, dictType);
	}

	public ArrayList<HashMap<String, Object>> getDictList(String dictType) {
		String dictListArray = "";
		if (dictType == null) {// 如果类型为空，则获取所有词典列表。
			dictType = Config.DICTTYPE_CSV;
			dictListArray = getDictStringArray(Config.DICTTYPE_STARDICT);
		}

		ArrayList<HashMap<String, Object>> resultItems = new ArrayList<HashMap<String, Object>>();
		// 获取词典字符串
		dictListArray += getDictStringArray(dictType);

		if (dictListArray != "" && dictListArray != null) {
			String[] dictNameList = dictListArray.split("\\|");

			HashMap<String, Object> result = null;
			String title = "标题";
			String des = "描述";
			for (int i = 0; i < dictNameList.length; i++) {
				Log.i("split", dictNameList[i]);
				result = new HashMap<String, Object>();
				result.put(title, dictNameList[i]);
				result.put(des, getDictDesc(dictNameList[i]));
				resultItems.add(result);
			}
			Log.i("dictList.length", String.valueOf(dictNameList.length));
		}
		return resultItems;
	}

	/**
	 * 设置词典字符串数组
	 * 
	 * @param dictsArray
	 * @param dictType
	 */
	public void setDictStringArray(String dictsArray, String dictType) {
		this.setCon(dictType + "_dicts_string_array", dictsArray);
	}

	public String getDictStringArray(String dictType) {
		return this.getCon(dictType + "_dicts_string_array", "");
	}

	/**
	 * 设置词典路径
	 * 
	 * @param dictName
	 * @param dictPath
	 */
	public void setDictPath(String dictName, String dictPath) {
		this.setCon(dictName + "_dict_path", dictPath);
	}

	public String getDictPath(String dictPath) {
		return this.getCon(dictPath + "_dict_path", "null");
	}

	/**
	 * 设置词典描述
	 * 
	 * @param dictFileName
	 * @param dictDesc
	 */
	public void setDictDesc(String dictFileName, String dictDesc) {
		this.setCon(dictFileName + "_dict_desc", dictDesc);
	}

	public String getDictDesc(String dictFileName) {
		return this.getCon(dictFileName + "_dict_desc", "");
	}

	/**
	 * 设置词典类型
	 * 
	 * @param dictName
	 * @param dictType
	 */
	public void setDictType(String dictName, String dictType) {
		this.setCon(dictName + "_dict_type", dictType);
	}

	public String getDictType(String dictName) {
		return this.getCon(dictName + "_dict_type", "");
	}

	// /**
	// * 设置词典文件名
	// *
	// * @param dictName
	// * @param dictFileName
	// */
	// public void setDictFileName(String dictName, String dictFileName) {
	// this.setCon(dictName + "_dict_file_name", dictFileName);
	// }
	//
	// public String getDictFileName(String dictPath) {
	// return this.getCon(dictPath + "_dict_file_name", "");
	// }

	/**
	 * 设置当前记忆词典名称
	 * 
	 * @param dictName
	 */
	public void setCurrentUseDictName(String dictName) {
		this.setCon("current_use_dict_name", dictName);
	}

	public String getCurrentUseDictName() {
		return this.getCon("current_use_dict_name", "");
	}

	/**
	 * 设置当前使用的例句词典
	 * 
	 * @param dictName
	 */
	public void setCurrentUseTransDictName(String dictName) {
		this.setCon("current_use_trans_dict_name", dictName);
	}

	public String getCurrentUseTransDictName() {
		return this.getCon("current_use_trans_dict_name", "null");
	}

	/**
	 * 设置是否可以使用例句词典
	 * 
	 * @param value
	 */
	public void setCanGetTransDict(Boolean value) {
		this.setCon("can_get_trans_dict", String.valueOf(value));
	}

	public boolean isCanGetTransDict() {
		return Boolean.parseBoolean(this.getCon("can_get_trans_dict", "false"));
	}

	/**
	 * 当前使用词典单词数
	 * 
	 * @return
	 */
	public void setCurrentUseDictWordCount(String wordCount) {
		this.setCon("current_dict_count", wordCount);
	}

	public String getCurrentUseDictWordCount() {
		return this.getCon("current_dict_count", "0");
	}

	/**
	 * 当前使用词典文件路径
	 * 
	 * @return
	 */
	public String getCurrentUseDictPath() {
		return this.getDictPath(this.getCurrentUseDictName());
	}

	/**
	 * 当前使用词典文件类型
	 * 
	 * @return
	 */
	public String getCurrentUseDictType() {
		return this.getDictType(this.getCurrentUseDictName());
	}

	/**
	 * 设置当前背诵完成的课程号
	 * 
	 * @param lessonNo
	 */
	public void setNextStudyLesson(int lessonNo) {
		this.setCon("next_study_lesson", String.valueOf(lessonNo));
	}

	public int getNextStudyLesson() {
		return Integer.parseInt(this.getCon("next_study_lesson", "0"));
	}

	/**
	 * 分组信息描述
	 * 
	 * @return
	 */
	public String getEachLessonWordCountDes() {
		return "每组: " + this.getEachLessonWordCount() + "   共词数: " + this.getCurrentUseDictWordCount() + "，组数: " + this.getLessonCount()
				+ " ";
	}

	/**
	 * 清空记忆曲线的相关数据
	 * 
	 */
	public void cleanRememberLine() {
		this.setNextStudyLesson(0);

		RememberHelper helper = new RememberHelper();
		helper.dropTable();
		helper.close();
	}

	/**
	 * 读取assets的文件内容
	 * 
	 * @param filePath
	 * @return
	 */
	public String getDataFromAssets(String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * 更新记忆曲线
	 * 
	 * @param lessonNo
	 * @param ignoreWords
	 *            不再记忆的单词编号
	 * @param start
	 *            是否从头开始记忆
	 * 
	 */
	public void setRememberLine(int lessonNo, String ignoreWords) {
		setRememberLine(lessonNo, ignoreWords, false);
	}

	public void setRememberLine(int lessonNo, String ignoreWords, boolean start) {
		RememberHelper helper = new RememberHelper();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));// 获取当前时间
		// 判断此课程是否存在
		if (helper.isExistsLessonNo(lessonNo)) {// 存在

			// 重新开始学习
			if (start) {
				String studyTime = sdf.format(cal.getTime());
				helper.deleteRecord(lessonNo);
				helper.addRecord(lessonNo, studyTime, studyTime, 0, ignoreWords);
				return;
			}

			try {
				Calendar oldTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
				oldTime.setTime(sdf.parse(helper.getStudyTimeByLessonNo(lessonNo)));

				int times = helper.getTimesByLessonNo(lessonNo);
				switch (times) {
				case 0:
					cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 5);// 第一个记忆周期5分钟
					break;
				case 1:
					cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 30);// 第二个记忆周期30分钟
					break;
				case 2:
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 12);// 第三个记忆周期12小时
					break;
				case 3:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);// 第四个记忆周期1天
					break;
				case 4:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);// 第四五个记忆周期2天
					break;
				case 5:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 4);// 第六个记忆周期4天
					break;
				case 6:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);// 第七个记忆周期7天
					break;
				case 7:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 15);// 第八个记忆周期15天
					break;
				default:
					cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 3);// 不在记忆周期内三个月后再记
					break;
				}
				helper.deleteRecord(lessonNo);
				helper.addRecord(lessonNo, sdf.format(oldTime.getTime()), sdf.format(cal.getTime()), ++times, ignoreWords);

			} catch (ParseException e) {
				e.printStackTrace();
			}

		} else {// 不存在
			String studyTime = sdf.format(cal.getTime());
			helper.addRecord(lessonNo, studyTime, studyTime, 0, ignoreWords);
		}
		helper.close();
	}

	public void setRememberLine(int lessonNo) {
		this.setRememberLine(lessonNo, this.getIgnoreWordsStr(lessonNo));
	}

	// 返回不需要再次记忆的单词编号
	public String getIgnoreWordsStr(int lessonNo) {
		RememberHelper helper = new RememberHelper();
		String ignoreWordsStr = helper.getIgnoreWords(lessonNo);
		helper.close();
		Log.i("ignoreWordsStr", ignoreWordsStr);
		return ignoreWordsStr.toLowerCase();
	}

	/**
	 * 设置发音类型
	 * 
	 * @param which
	 */
	public void setSpeechType(int which) {
		this.setCon("speech_type", String.valueOf(which));
	}

	public int getSpeechType() {
		return Integer.parseInt(getCon("speech_type", "0"));
	}

	/**
	 * 是否可发音
	 * 
	 * @param flag
	 */
	public void setCanSpeech(boolean flag) {
		this.setCon("is_can_speech", String.valueOf(flag));
	}

	public boolean isCanSpeech() {
		return Boolean.parseBoolean(this.getCon("is_can_speech", "false"));
	}

	/**
	 * 读取词库数据
	 * 
	 * @param currentLessonNo
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> getWordsFromFileByLessonNo(int currentLessonNo) {
		ArrayList<HashMap<String, Object>> wordList = null;

		// 计算偏移量和单词数
		String strEachLessonWordCount = getEachLessonWordCount();
		int eachLessonWordCount = 0;// 每课单词数
		if (strEachLessonWordCount != null && !strEachLessonWordCount.equals("")) {
			eachLessonWordCount = Integer.parseInt(strEachLessonWordCount);
		}
		int index = currentLessonNo * eachLessonWordCount;// 偏移量
		String dictType = getCurrentUseDictType();
		if (dictType.equalsIgnoreCase("csv")) {
			CsvHelper helper = new CsvHelper();
			wordList = helper.getWords(getCurrentUseDictPath(), getDictCharset(), index, eachLessonWordCount);
		}

		// 处理不再记忆的单词
		String ignoreWords = getIgnoreWordsStr(currentLessonNo);
		if (ignoreWords != null && !ignoreWords.equals("")) {
			int length = wordList.size();
			String ignoreWord;
			ignoreWords = ignoreWords.toLowerCase();
			Log.i("ignoreWords", ignoreWords);
			for (int i = 0; i < length; i++) {
				ignoreWord = (String) wordList.get(i).get("单词");
				// Log.i("ignoreWord", ignoreWord);
				if (ignoreWords.contains(ignoreWord.toLowerCase())) {
					Log.i("remove", String.valueOf(i));
					wordList.remove(i);
					length = wordList.size();
				}
			}
		}

		return wordList;
	}

	/**
	 * 设置词库文件编码
	 * 
	 * @param charset
	 */
	public void setDictCharset(String charset) {
		this.setCon("config_dict_charset", charset);
	}

	public String getDictCharset() {
		return getCon("config_dict_charset", "UTF-8");
	}
}

