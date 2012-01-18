package com.nhn.socialanalytics.appleappstore.collect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;

import com.nhn.socialanalytics.appleappstore.model.Review;
import com.nhn.socialanalytics.common.Config;
import com.nhn.socialanalytics.common.JobLogger;
import com.nhn.socialanalytics.common.collect.CollectHistoryBuffer;
import com.nhn.socialanalytics.common.collect.Collector;
import com.nhn.socialanalytics.common.util.DateUtil;
import com.nhn.socialanalytics.common.util.StringUtil;
import com.nhn.socialanalytics.nlp.feature.FeatureClassifier;
import com.nhn.socialanalytics.nlp.lang.ja.JapaneseMorphemeAnalyzer;
import com.nhn.socialanalytics.nlp.lang.ja.JapaneseSemanticAnalyzer;
import com.nhn.socialanalytics.nlp.lang.ko.KoreanMorphemeAnalyzer;
import com.nhn.socialanalytics.nlp.lang.ko.KoreanSemanticAnalyzer;
import com.nhn.socialanalytics.nlp.morpheme.MorphemeAnalyzer;
import com.nhn.socialanalytics.nlp.semantic.SemanticAnalyzer;
import com.nhn.socialanalytics.nlp.semantic.SemanticClause;
import com.nhn.socialanalytics.nlp.semantic.SemanticSentence;
import com.nhn.socialanalytics.nlp.sentiment.SentimentAnalyzer;
import com.nhn.socialanalytics.nlp.sentiment.SentimentAnalyzer.Polarity;
import com.nhn.socialanalytics.opinion.common.FieldConstants;
import com.nhn.socialanalytics.opinion.common.OpinionDocument;
import com.nhn.socialanalytics.opinion.dao.file.DocFileWriter;
import com.nhn.socialanalytics.opinion.dao.lucene.DocIndexSearcher;
import com.nhn.socialanalytics.opinion.dao.lucene.DocIndexWriter;


public class AppStoreDataCollector extends Collector {
	
	public static final String TARGET_SITE_NAME = "appstore";
	private static JobLogger logger = JobLogger.getLogger(AppStoreDataCollector.class, "appstore-collect.log");
		
	private AppStoreCrawler crawler;

	public AppStoreDataCollector() {
		this.crawler = new AppStoreCrawler();
	}
	
	public List<Review> getReviews(Set<String> appStores, String appId) {
		List<Review> result = new ArrayList<Review>();
		
		try {           	
        	logger.info("------------------------------------------------");
        	logger.info("appStores = " + appStores + " appId: " + appId);
         	
        	result = crawler.getReviews(appStores, appId);
	            
	        logger.info("result size [appStores:" + appStores + "] = " + result.size());           
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		return result;
	}
	
	public List<Review> getReviews(Set<String> appStores, String appId, int maxPage) {
		List<Review> result = new ArrayList<Review>();
		
		try {           	
        	logger.info("------------------------------------------------");
        	logger.info("appStores = " + appStores + " appId: " + appId + " page: " + maxPage);
         	
        	result = crawler.getReviews(appStores, appId, maxPage);
	            
	        logger.info("result size [appStores:" + appStores + "] = " + result.size());           
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		return result;
	}
	
	public void writeOutput(String dataDir, String indexDir, String objectId, 
			List<Review> reviews, Date collectDate, int historyBufferMaxRound) throws IOException, Exception {
		
		String currentDatetime = DateUtil.convertDateToString("yyyyMMddHHmmss", new Date());	
		File docIndexDir = super.getDocIndexDir(indexDir, collectDate);
		File sourceDocFile = super.getSourceDocFile(dataDir, objectId, collectDate);
		File opinionDocFile = super.getOpinionDocFile(dataDir, objectId, collectDate);

		// collect history buffer
		Set<String> idSet = new HashSet<String>();
		CollectHistoryBuffer history = new CollectHistoryBuffer(super.getCollectHistoryFile(dataDir, objectId), historyBufferMaxRound);
		
		// text analyzer
		MorphemeAnalyzer morphemeKorean = super.getMorphemeAnalyzer(Collector.LANG_KOREAN);
		MorphemeAnalyzer morphemeJapanese = super.getMorphemeAnalyzer(Collector.LANG_JAPANESE);
		SemanticAnalyzer semanticKorean = super.getSemanticAnalyzer(Collector.LANG_KOREAN);
		SemanticAnalyzer semanticJapanese = super.getSemanticAnalyzer(Collector.LANG_JAPANESE);
		SentimentAnalyzer sentimentKorean = super.getSentimentAnalyzer(Collector.LANG_KOREAN);
		SentimentAnalyzer sentimentJapanese = super.getSentimentAnalyzer(Collector.LANG_JAPANESE);
		
		// feature classifier
		FeatureClassifier featureKorean = super.getFeatureClassifier(objectId, Collector.LANG_KOREAN);
		FeatureClassifier featureJapanese = super.getFeatureClassifier(objectId, Collector.LANG_JAPANESE);
		
		// indexer
		DocIndexWriter indexWriter = new DocIndexWriter(docIndexDir);		
		DocIndexSearcher indexSearcher = new DocIndexSearcher(super.getDocumentIndexDirsToSearch(indexDir, collectDate));
		
		// file writer
		DocFileWriter opinionDocWriter = new DocFileWriter(opinionDocFile);
		
		// source doc file
		boolean existSourceDoc = false;
		
		if (sourceDocFile.exists())
			existSourceDoc = true;
		
		BufferedWriter sourceDocWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourceDocFile.getPath(), true), "UTF-8"));
		
		if (!existSourceDoc) {
			sourceDocWriter.write("site" + DELIMITER +
					"object_id" + DELIMITER +
					"country" + DELIMITER + 
					"collect_date" + DELIMITER +
					"review_id" + DELIMITER +	
					"create_date" + DELIMITER +	
					"author_id" + DELIMITER +		
					"author_name" + DELIMITER +						
					"version" + DELIMITER +	
					"rating" + DELIMITER +	
					"is_spam" + DELIMITER +	
					"appstore_id" + DELIMITER + 					
					"topic" + DELIMITER +	
					"text" + DELIMITER +		
					"text1" + DELIMITER +		
					"text2" + DELIMITER +
					"feature" + DELIMITER +
					"main_feature" + DELIMITER +
					"clause" + DELIMITER +		
					"subject" + DELIMITER +		
					"predicate" + DELIMITER +		
					"attribute" + DELIMITER +	
					"modifier" + DELIMITER +	
					"polarity" + DELIMITER +
					"polarity_strength"
					);
			sourceDocWriter.newLine();			
		}
		
		// review
		for (Review review : reviews) {			
			String appStoreId = review.getAppStoreId();
			String country = review.getCountry();
			String reviewId = review.getReviewId();
			String authorId = review.getAuthorId();
			String authorName = review.getAuthorName();
			String topic = review.getTopic();
			String text = review.getText();
			topic = topic.replaceAll("\t", " ").replaceAll("\n", " ");
			text = text.replaceAll("\t", " ").replaceAll("\n", " ");
			String version = review.getVersion();
			String createDate = review.getCreateDate();
			int rating = review.getRating();
			
			/////////////////////////////////
			// add new collected id into set
			idSet.add(reviewId);
			/////////////////////////////////			
						
			// if no duplication, write collected data
			if (!history.checkDuplicate(reviewId)) {
				boolean isSpam = super.isSpam(topic + " " + text);

				String topicEmotiTagged = StringUtil.convertEmoticonToTag(topic);	
				String textEmotiTagged = StringUtil.convertEmoticonToTag(text);	
				
				String language = "";
				String text1 = "";
				String text2 = "";
				String feature = "";
				String mainFeature = "";
				SemanticSentence semanticSentenceTopic = null;
				SemanticSentence semanticSentenceText = null;
				Polarity polarity = null;	
				
				List<SemanticSentence> semanticSentences = new ArrayList<SemanticSentence>();
				
				if (review.getCountry().equalsIgnoreCase("Korea")) {
					language = FieldConstants.LANG_KOREAN;
					// morpheme analysis
					text1 = morphemeKorean.extractTerms(topicEmotiTagged);
					text1 = text1 +  " " + morphemeKorean.extractTerms(textEmotiTagged);
					text2 = morphemeKorean.extractCoreTerms(topicEmotiTagged);
					text2 = text2 +  " " + morphemeKorean.extractCoreTerms(textEmotiTagged);
					
					// semantic analysis
					semanticSentenceTopic = semanticKorean.analyze(topicEmotiTagged);
					semanticSentenceText = semanticKorean.analyze(textEmotiTagged);
					semanticSentences.add(semanticSentenceTopic);
					semanticSentences.add(semanticSentenceText);
					
					// sentiment analysis
					polarity  = sentimentKorean.analyzePolarity(semanticSentences);
					
					// feature classification
					String standardLabels = semanticSentenceTopic.extractStandardLabel(" ", " ", true, false, false);
					standardLabels = standardLabels +  " " + semanticSentenceText.extractStandardLabel(" ", " ", true, false, false);
					Map<String, Double> featureCounts = featureKorean.getFeatureCounts(standardLabels, true);
					feature = featureKorean.getFeatureLabel(featureCounts);
					mainFeature = featureKorean.getMainFeatureLabel(featureCounts);
				}
				else if (review.getCountry().equalsIgnoreCase("Japan")) {
					language = FieldConstants.LANG_JAPANESE;
					// morpheme analysis
					text1 = morphemeJapanese.extractTerms(topicEmotiTagged);
					text1 = text1 +  " " + morphemeJapanese.extractTerms(textEmotiTagged);
					text2 = morphemeJapanese.extractCoreTerms(topicEmotiTagged);
					text2 = text2 +  " " + morphemeJapanese.extractCoreTerms(textEmotiTagged);
					
					// semantic analysis
					semanticSentenceTopic = semanticJapanese.analyze(topicEmotiTagged);
					semanticSentenceText = semanticJapanese.analyze(textEmotiTagged);
					semanticSentences.add(semanticSentenceTopic);
					semanticSentences.add(semanticSentenceText);
					
					// sentiment analysis
					polarity  = sentimentJapanese.analyzePolarity(semanticSentences);
					
					// feature classification
					String standardLabels = semanticSentenceTopic.extractStandardLabel(" ", " ", true, false, false);
					standardLabels = standardLabels +  " " + semanticSentenceText.extractStandardLabel(" ", " ", true, false, false);
					Map<String, Double> featureCounts = featureJapanese.getFeatureCounts(standardLabels, true);
					feature = featureJapanese.getFeatureLabel(featureCounts);
					mainFeature = featureJapanese.getMainFeatureLabel(featureCounts);
				}
				else {
					// morpheme analysis
					text1 = morphemeKorean.extractTerms(topicEmotiTagged);
					text1 = text1 +  " " + morphemeKorean.extractTerms(textEmotiTagged);
					text2 = morphemeKorean.extractCoreTerms(topicEmotiTagged);
					text2 = text2 +  " " + morphemeKorean.extractCoreTerms(textEmotiTagged);
					
					// semantic analysis
					semanticSentenceTopic = semanticKorean.analyze(topicEmotiTagged);
					semanticSentenceText = semanticKorean.analyze(textEmotiTagged);
					semanticSentences.add(semanticSentenceTopic);
					semanticSentences.add(semanticSentenceText);
					
					// sentiment analysis
					polarity  = sentimentKorean.analyzePolarity(semanticSentences);
					
					// feature classification
					String standardLabels = semanticSentenceTopic.extractStandardLabel(" ", " ", true, false, false);
					standardLabels = standardLabels +  " " + semanticSentenceText.extractStandardLabel(" ", " ", true, false, false);
					Map<String, Double> featureCounts = featureKorean.getFeatureCounts(standardLabels, true);
					feature = featureKorean.getFeatureLabel(featureCounts);
					mainFeature = featureKorean.getMainFeatureLabel(featureCounts);
				}

				String strClause = semanticSentenceTopic.extractStandardLabel(",", "-", true, false, false);
				if (!strClause.equals(""))
					strClause = strClause + "," ;
				strClause = strClause + semanticSentenceText.extractStandardLabel(",", "-", true, false, false);
				
				String subject = semanticSentenceTopic.extractStandardSubjectLabel();
				if (!subject.equals(""))
					subject = subject + " " ;
				subject = subject + semanticSentenceText.extractStandardSubjectLabel();
				
				String predicate = semanticSentenceTopic.extractStandardPredicateLabel();
				if (!predicate.equals(""))
					predicate = predicate + " " ;
				predicate = predicate + semanticSentenceText.extractStandardPredicateLabel();
				
				String attribute = semanticSentenceTopic.extractStandardAttributesLabel();
				if (!attribute.equals(""))
					attribute = attribute + " " ;
				attribute = attribute + semanticSentenceText.extractStandardAttributesLabel();
				
				String modifier = semanticSentenceTopic.extractStandardModifiersLabel();
				if (!modifier.equals(""))
					modifier = modifier + " " ;
				modifier = modifier + semanticSentenceText.extractStandardModifiersLabel();
				
				// write new collected data into source file
				sourceDocWriter.write(
						TARGET_SITE_NAME + DELIMITER +
						objectId + DELIMITER +
						country + DELIMITER +
						currentDatetime + DELIMITER +						
						reviewId + DELIMITER +
						createDate + DELIMITER + 
						authorId + DELIMITER +		
						authorName + DELIMITER +	
						version + DELIMITER +
						rating + DELIMITER +
						isSpam + DELIMITER +
						appStoreId + DELIMITER +
						topic + DELIMITER +
						text + DELIMITER +		
						text1 + DELIMITER +		
						text2 + DELIMITER +	
						feature + DELIMITER +
						mainFeature + DELIMITER +
						strClause + DELIMITER +		
						subject + DELIMITER +		
						predicate + DELIMITER +		
						attribute + DELIMITER +	
						modifier + DELIMITER +	
						polarity.getPolarity() + DELIMITER +		
						polarity.getPolarityStrength()
						);
				sourceDocWriter.newLine();
				
				////////////////////////////////////////
				// write new collected data into index file
				////////////////////////////////////////
				if (!isSpam) {
					Set<Document> existDocs = indexSearcher.searchDocuments(FieldConstants.DOC_ID, reviewId);
					
					if (existDocs.size() > 0) {
						for (Iterator<Document> it = existDocs.iterator(); it.hasNext();) {
							Document existDoc = (Document) it.next();
							String objects = existDoc.get(FieldConstants.OBJECT);
							if (objects.indexOf(objectId) < 0) {
								objects = objects + " " + objectId;
								indexWriter.update(FieldConstants.OBJECT, objects, existDoc);
							}
					     }
					}
					else {
						// topic
						for (SemanticClause clause : semanticSentenceTopic) {
							if (super.isValidClause(clause)) {
								OpinionDocument doc = new OpinionDocument();
								doc.setSite(TARGET_SITE_NAME);
								doc.setObject(objectId);
								doc.setLanguage(language);
								doc.setCollectDate(currentDatetime);
								doc.setDocId(reviewId);
								doc.setDate(createDate);
								doc.setAuthorId(authorId);
								doc.setAuthorName(authorName);
								doc.setDocFeature(feature);
								doc.setDocMainFeature(mainFeature);
								doc.setSubject((clause.getStandardSubject() == null) ? "" : clause.getStandardSubject());
								doc.setPredicate((clause.getStandardPredicate() == null) ? "" : clause.getStandardPredicate());
								doc.setAttribute(clause.makeStandardAttributesLabel());
								doc.setModifier(clause.makeStandardModifiersLabel());
								doc.setText(text);
								doc.setDocPolarity(polarity.getPolarity());
								doc.setDocPolarityStrength(polarity.getPolarityStrength());
								doc.setClausePolarity(clause.getPolarity());
								doc.setClausePolarityStrength(clause.getPolarityStrength());
								
								// feature classification for semantic clause
								doc = super.setClauseFeatureToDocument(objectId, language, clause, doc);
							
								indexWriter.write(doc);
								opinionDocWriter.write(doc);
							}
						}
						// text
						for (SemanticClause clause : semanticSentenceText) {
							if (super.isValidClause(clause)) {
								OpinionDocument doc = new OpinionDocument();
								doc.setSite(TARGET_SITE_NAME);
								doc.setObject(objectId);
								doc.setLanguage(language);
								doc.setCollectDate(currentDatetime);
								doc.setDocId(reviewId);
								doc.setDate(createDate);
								doc.setAuthorId(authorId);
								doc.setAuthorName(authorName);
								doc.setDocFeature(feature);
								doc.setDocMainFeature(mainFeature);
								doc.setSubject((clause.getStandardSubject() == null) ? "" : clause.getStandardSubject());
								doc.setPredicate((clause.getStandardPredicate() == null) ? "" : clause.getStandardPredicate());
								doc.setAttribute(clause.makeStandardAttributesLabel());
								doc.setModifier(clause.makeStandardModifiersLabel());
								doc.setText(text);
								doc.setDocPolarity(polarity.getPolarity());
								doc.setDocPolarityStrength(polarity.getPolarityStrength());
								doc.setClausePolarity(clause.getPolarity());
								doc.setClausePolarityStrength(clause.getPolarityStrength());
								
								// feature classification for semantic clause
								doc = super.setClauseFeatureToDocument(objectId, language, clause, doc);
								
								indexWriter.write(doc);
								opinionDocWriter.write(doc);
							}
						}						
					}					
				}			
			}		
		}
		
		sourceDocWriter.close();
		indexWriter.close();	
		opinionDocWriter.close();
		history.writeCollectHistory(idSet);
	}
	
	public static void main(String[] args) {
		AppStoreDataCollector collector = new AppStoreDataCollector();
		collector.setSpamFilter(new File(Config.getProperty("COLLECT_SPAM_FILTER_APPSTORE")));		
		collector.putMorphemeAnalyzer(Collector.LANG_KOREAN, new KoreanMorphemeAnalyzer());
		collector.putMorphemeAnalyzer(Collector.LANG_JAPANESE, new JapaneseMorphemeAnalyzer());
		collector.putSemanticAnalyzer(Collector.LANG_KOREAN, new KoreanSemanticAnalyzer());
		collector.putSemanticAnalyzer(Collector.LANG_JAPANESE, new JapaneseSemanticAnalyzer());
		collector.putSentimentAnalyzer(Collector.LANG_KOREAN, new SentimentAnalyzer(new File(Config.getProperty("LIWC_KOREAN"))));
		collector.putSentimentAnalyzer(Collector.LANG_JAPANESE, new SentimentAnalyzer(new File(Config.getProperty("LIWC_JAPANESE"))));		

		Set<String> appStores = new HashSet<String>();
		appStores.add(AppStores.getAppStore("Korea"));
		appStores.add(AppStores.getAppStore("Japan"));
		
		String objectId = "naverline";
		String appId = "443904275";		
		
		//String objectId = "naverapp";
		//String appId = "393499958";
		
		//String objectId = "kakaotalk";
		//String appId = "362057947";		
		
		collector.putFeatureClassifier(objectId, Collector.LANG_KOREAN, new FeatureClassifier(new File(Config.getProperty("DEFAULT_FEATURE_KOREAN"))));
		collector.putFeatureClassifier(objectId, Collector.LANG_JAPANESE, new FeatureClassifier(new File(Config.getProperty("DEFAULT_FEATURE_JAPANESE"))));
		
		List<Review> reviews = collector.getReviews(appStores, appId, 30);
		//List<Review> reviews = collector.getReviews(appStores, appId);
		//List<Review> reviews = collector.getReviews(AppStores.getAllAppStores(), appId);
		
		try {
			collector.writeOutput("./bin/data/appstore/", "./bin/data/appstore/index/", objectId, reviews, new Date(), 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
