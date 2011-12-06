package com.nhn.socialanalytics.androidmarket.collect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;

import com.gc.android.market.api.model.Market.Comment;
import com.nhn.socialanalytics.common.JobLogger;
import com.nhn.socialanalytics.common.collect.CollectHistoryBuffer;
import com.nhn.socialanalytics.common.collect.Collector;
import com.nhn.socialanalytics.common.util.DateUtil;
import com.nhn.socialanalytics.common.util.StringUtil;
import com.nhn.socialanalytics.miner.index.DetailDoc;
import com.nhn.socialanalytics.miner.index.DocIndexSearcher;
import com.nhn.socialanalytics.miner.index.DocIndexWriter;
import com.nhn.socialanalytics.miner.index.FieldConstants;
import com.nhn.socialanalytics.nlp.kr.morpheme.MorphemeAnalyzer;
import com.nhn.socialanalytics.nlp.kr.semantic.SemanticAnalyzer;
import com.nhn.socialanalytics.nlp.kr.semantic.SemanticClause;
import com.nhn.socialanalytics.nlp.kr.semantic.SemanticSentence;
import com.nhn.socialanalytics.nlp.kr.sentiment.SentimentAnalyzer;

public class AndroidMarketDataCollector extends Collector { 
	
	private static JobLogger logger = JobLogger.getLogger(AndroidMarketDataCollector.class, "androidmarket-collect.log");
	private static final String TARGET_SITE_NAME = "androidmarket";
	
	private AndroidMarketCrawler crawler;
	
	public AndroidMarketDataCollector(String loginAccount, String loginPasswd) {
		this(loginAccount, loginPasswd, null);
	}	
	
	public AndroidMarketDataCollector(String loginAccount, String loginPasswd, File spamFilterFile) {
		super(spamFilterFile);
		this.crawler = new AndroidMarketCrawler(loginAccount, loginPasswd);
	}
	
	public Map<Locale, List<Comment>> getAppCommentsByLocales(Set<Locale> locales, String appId, int maxPage) {
		Map<Locale, List<Comment>> commentMap = new HashMap<Locale, List<Comment>>();
		try {           	
        	logger.info("------------------------------------------------");
        	logger.info("appStores = " + locales + " appId: " + appId + " page: " + maxPage);
        	commentMap = crawler.getAppCommentsByLocales(locales, appId, maxPage);	            
	        logger.info("result map size [locales:" + locales + "] = " + commentMap.size());          
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		return commentMap;
	}
	
	public List<Comment> getAppComments(Set<Locale> locales, String appId, int maxPage) {
		List<Comment> commentList = new ArrayList<Comment>();
		try {           	
        	logger.info("------------------------------------------------");
        	logger.info("appStores = " + locales + " appId: " + appId + " page: " + maxPage);
        	commentList = crawler.getAppComments(locales, appId, maxPage);	            
	        logger.info("result size [locales:" + locales + "] = " + commentList.size());          
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		return commentList;
	}
	
	public void writeOutput(String dataDir, String indexDir, String liwcCatFile, String objectId, 
			Map<Locale, List<Comment>> commentsMap, Date collectDate, int historyBufferMaxRound) throws Exception {		
		
		String currentDatetime = DateUtil.convertDateToString("yyyyMMddHHmmss", new Date());	
		File docIndexDir = super.getDocIndexDir(indexDir, collectDate);
		File dataFile = super.getDataFile(dataDir, objectId, collectDate);
		
		// collect history buffer
		Set<String> idSet = new HashSet<String>();
		CollectHistoryBuffer history = new CollectHistoryBuffer(super.getCollectHistoryFile(dataDir, objectId), historyBufferMaxRound);
				
		MorphemeAnalyzer morph = MorphemeAnalyzer.getInstance();
		SemanticAnalyzer semantic = SemanticAnalyzer.getInstance();
		SentimentAnalyzer sentiment = SentimentAnalyzer.getInstance(liwcCatFile);
		
		DocIndexWriter indexWriter = new DocIndexWriter(docIndexDir);		
		DocIndexSearcher indexSearcher = new DocIndexSearcher(super.getDocumentIndexDirsToSearch(indexDir, collectDate));
		
		// output data file
		boolean existDataFile = false;
		
		if (dataFile.exists())
			existDataFile = true;
		
		BufferedWriter brData = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile.getPath(), true), "UTF-8"));		
	
		if (!existDataFile) {
			brData.write("site" + DELIMITER +
					"object_id" + DELIMITER +
					"locale" + DELIMITER +	
					"collect_date" + DELIMITER +
					"comment_id" + DELIMITER +	
					"create_date" + DELIMITER +	
					"author_id" + DELIMITER +		
					"author_name" + DELIMITER +	
					"rating" + DELIMITER +	
					"is_spam" + DELIMITER +						
					"text" + DELIMITER +		
					"text1" + DELIMITER +		
					"text2" + DELIMITER +		
					"subjectpredicate" + DELIMITER +		
					"subject" + DELIMITER +		
					"predicate" + DELIMITER +		
					"attribute" + DELIMITER +		
					"polarity" + DELIMITER +		
					"polarity_strength"
					);
			brData.newLine();			
		}
		
		for (Map.Entry<Locale, List<Comment>> entry : commentsMap.entrySet()) {
			Locale locale = entry.getKey();
			List<Comment> comments = entry.getValue();
			
			// comment
			for (Comment comment : comments) {
				String authorId  = comment.getAuthorId();
				String authorName = comment.getAuthorName();
				int rating = comment.getRating();
				String text = comment.getText();
				text = text.replaceAll("\t", " ").replaceAll("\n", " ");
				String createDate = DateUtil.convertLongToString("yyyyMMddHHmmss", comment.getCreationTime());
				String commentId = createDate + "-" + authorId;
				
				/////////////////////////////////
				// add new collected id into set
				idSet.add(commentId);
				/////////////////////////////////			
							
				// if no duplication, write collected data
				if (!history.checkDuplicate(commentId)) {
					boolean isSpam = super.isSpam(text);
					String textEmotiTagged = StringUtil.convertEmoticonToTag(text);
					
					String text1 = morph.extractTerms(textEmotiTagged);
					String text2 = morph.extractCoreTerms(textEmotiTagged);
					
					SemanticSentence semanticSentence = semantic.createSemanticSentence(textEmotiTagged);
					String subjectpredicate = semanticSentence.extractStandardSubjectPredicateLabel();
					String subject = semanticSentence.extractStandardSubjectLabel();
					String predicate = semanticSentence.extractStandardPredicateLabel();
					String attribute = semanticSentence.extractStandardAttributesLabel();
					
					semanticSentence = sentiment.analyzePolarity(semanticSentence);
					double polarity = semanticSentence.getPolarity();
					double polarityStrength = semanticSentence.getPolarityStrength();
					
					// write new collected data into source file
					brData.write(
							TARGET_SITE_NAME + DELIMITER +
							objectId + DELIMITER +
							locale + DELIMITER +
							currentDatetime + DELIMITER +
							commentId + DELIMITER +
							createDate + DELIMITER + 
							authorId + DELIMITER +		
							authorName + DELIMITER +
							rating + DELIMITER +
							isSpam + DELIMITER +							
							text + DELIMITER +		
							text1 + DELIMITER +		
							text2 + DELIMITER +		
							subjectpredicate + DELIMITER +		
							subject + DELIMITER +		
							predicate + DELIMITER +		
							attribute + DELIMITER +		
							polarity + DELIMITER +		
							polarityStrength
							);
					brData.newLine();
					
					////////////////////////////////////////
					// write new collected data into index file
					////////////////////////////////////////
					if (!isSpam) {
						Set<Document> existDocs = indexSearcher.searchDocuments(FieldConstants.DOC_ID, commentId);
						
						if (existDocs.size() > 0) {
							for (Iterator<Document> it = existDocs.iterator(); it.hasNext();) {
								Document existDoc = (Document) it.next();
								String objects = existDoc.get(FieldConstants.OBJECT);
								objects = objects + " " + objectId;
								
								indexWriter.update(FieldConstants.OBJECT, objects, existDoc);
						     }
						}
						else {
							for (SemanticClause clause : semanticSentence) {
								DetailDoc doc = new DetailDoc();
								doc.setSite(TARGET_SITE_NAME);
								doc.setObject(objectId);
								doc.setLanguage(locale.toString());
								doc.setCollectDate(currentDatetime);
								doc.setDocId(commentId);
								doc.setDate(createDate);
								doc.setUserId(authorId);
								doc.setUserName(authorName);								
								doc.setSubject(clause.getSubject());
								doc.setPredicate(clause.getPredicate());
								doc.setAttribute(clause.makeAttributesLabel());
								doc.setText(text);
								
								indexWriter.write(doc);
							}						
						}					
					}		
				}		
			}
		}
			
		brData.close();
		indexWriter.close();		
		history.writeCollectHistory(idSet);
	}
	
	public static void main(String[] args) {
		
		String loginAccount = "louiezzang@gmail.com";
		String loginPasswd = "bae120809";
		AndroidMarketDataCollector collector = new AndroidMarketDataCollector(loginAccount, loginPasswd);	
		
		//Set<Locale> locales = AndroidMarkets.getAllAndroidMarkets();
		Set<Locale> locales = new HashSet<Locale>();
		locales.add(Locale.KOREA);
		
		//String query = "네이버톡";
		//String query = "pname:com.nhn.android.navertalk";
		//collector.searchApps(Locale.KOREA, query, 1);
		
		String objectId = "naverline";
		//String objectId = "kakaotalk";
		//String appId = "com.nhn.android.navertalk";
		//String appId = "com.nhn.android.search";
		String appId = "jp.naver.line.android";
		//String appId = "com.nhn.android.nbooks";
		//String appId = "com.kakao.talk";
		
		//List<Comment> comments = collector.getAppComments(locales, appId, 2);
		Map<Locale, List<Comment>> commentsMap = collector.getAppCommentsByLocales(locales, appId, 10);
		try {
			collector.writeOutput("./bin/data/androidmarket/collect/", "./bin/data/androidmarket/index/", "./bin/liwc/LIWC_ko.txt", objectId, commentsMap, new Date(), 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
