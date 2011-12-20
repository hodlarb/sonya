package com.nhn.socialanalytics.miner.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.apache.mahout.common.iterator.FileLineIterator;

import com.nhn.socialanalytics.miner.opinion.OpinionTerm;
import com.nhn.socialanalytics.nlp.sentiment.SentimentAnalyzer;

public class DocIndexSearcher {

	private IndexSearcher searcher;
	private Map<String, SentimentAnalyzer> sentimentAnalyzers = new HashMap<String, SentimentAnalyzer>();
	
	private Set<String> stopwordSet = new HashSet<String>();
	private static final Set<?> DEFAULT_STOPWORDS;
	static {
		final List<?> stopWords = Arrays.asList(
				 "tagquestion"
				, "tagsmile"
				, "tagcry"
				, "taglove"
				, "tagexclamation"
				, "tagempty"
				, "http"
				, "gt"
				, "lt"
				, "brgt"
				, "brgt"
		);
		final CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
		stopSet.addAll(stopWords);
		DEFAULT_STOPWORDS = CharArraySet.unmodifiableSet(stopSet);
	}
	
	public DocIndexSearcher(File[] indexDirs) throws IOException, CorruptIndexException {
		List<IndexReader> indexReaders = getIndexReaders(indexDirs);
		MultiReader multiReader = new MultiReader(indexReaders.toArray(new IndexReader[0]));  
		this.searcher = new IndexSearcher(multiReader);	
		
		stopwordSet.addAll((Set<String>)DEFAULT_STOPWORDS);	
	} 
	
	public void putStopwordFile(File stopwordFile) throws IOException {
		Set<String> stopwords = loadStopwords(stopwordFile);
		stopwordSet.addAll(stopwords);
	}
	
	public void putCustomStopwords(Set<String> customStopwords) {
		if (customStopwords != null)
			stopwordSet.addAll(customStopwords);
	}

	public void putSentimentAnalyzer(String language, SentimentAnalyzer analyzer) {
		if (analyzer != null) {
			sentimentAnalyzers.put(language, analyzer);
		}			
	} 
	
	private List<IndexReader> getIndexReaders(File[] indexDirs) 
			throws IOException, CorruptIndexException {
		List<IndexReader> readers = new ArrayList<IndexReader>();
		
		for (int i = 0; i < indexDirs.length; i++) {
			try {
				IndexReader reader = IndexReader.open(FSDirectory.open(indexDirs[i]));
				readers.add(reader);
			} catch (IndexNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (NoSuchDirectoryException e) {
				System.out.println(e.getMessage());
			}
		}
		return readers;
	}
	
	private Set<String> loadStopwords(File stopwordFile) throws IOException {
		Set<String> stopSet = new HashSet<String>();
		
		if (stopwordFile == null)
			return stopSet;
		
		FileLineIterator it = new FileLineIterator(new FileInputStream(stopwordFile));

		while (it.hasNext()) {
			String line = it.next();
			line = line.trim();
			if (line.startsWith("#")) {
				continue;
			}
			
			if (!line.equals("")) {
				stopSet.add(line);
			}
		}
		return stopSet;
	}
	
	public Set<String> getStopwords() {
		return this.stopwordSet;
	}
	
	public void setStopwords(Set<String> stopwords) {
		this.stopwordSet = stopwords;
	}
	
	public int getTF(String object, String field, String text) throws IOException {		
		Term objTerm = new Term(FieldConstants.OBJECT, object);		
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		Term term = new Term(field, text);
		Query query = new TermQuery(term);		

		TopDocs rs = searcher.search(query, filter, 1000000);
		
		return rs.totalHits;
	}
	
	public int getTF(String object, Map<String, String> queryMap) 
			throws IOException, CorruptIndexException {
		Term objTerm = new Term(FieldConstants.OBJECT, object);	
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		BooleanQuery bQuery = new BooleanQuery();
		for (Map.Entry<String, String> entry : queryMap.entrySet()) {
			String field = entry.getKey();
			String text = entry.getValue();
			
			Term term = new Term(field, text);
			Query query = new TermQuery(term);
			
			bQuery.add(query, BooleanClause.Occur.MUST);
		}

		TopDocs rs = searcher.search(bQuery, filter, 1000000);		
		return rs.totalHits;
	}
	
	public Set<Document> searchDocuments(String field, String text) throws IOException {		
		Set<Document> docs = new HashSet<Document>();
		
		Term term = new Term(field, text);
		Query query = new TermQuery(term);		

		TopDocs rs = searcher.search(query, null, 1000000);
		System.out.println("search: field = " + field + ", text = " + text + ", docFreq = " + rs.totalHits);

		for (int i = 0; i < rs.totalHits; i++) {
			int docId = rs.scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			docs.add(doc);
		}

		return docs;
	}
	
	/*
	public List<OpinionTerm> searchTerms(String object, String language, String field, 
			int minTF, boolean excludeStopwords) throws IOException {	
		
		Map<String, OpinionTerm> opinionTerms = new HashMap<String, OpinionTerm>();
		
		Term objTerm = new Term(FieldConstants.OBJECT, object);	
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		Term term = new Term(FieldConstants.LANGUAGE, language);
		Query query = new TermQuery(term);

		TopDocs rs = searcher.search(query, filter, 1000000);
		System.out.println("search: field = " + field + ", minTF = " + minTF + ", docFreq = " + rs.totalHits);
		
		// get sentiment analyzer suitable for the language
		SentimentAnalyzer sentimentAnalyzer = sentimentAnalyzers.get(language);

		for (int i = 0; i < rs.totalHits; i++) {
			int docId = rs.scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			String text = doc.get(field);
			String feature = this.getFeatureName(doc);
			
			if (excludeStopwords && opinionTerms.get(text) == null && !stopwordSet.contains(text)) {
				int tf = this.getTF(object, field, text);
				if (tf >= minTF) {
					OpinionTerm opinionTerm = new OpinionTerm();
					opinionTerm.setType(field);
					opinionTerm.setObject(object);
					opinionTerm.setFeature(feature);
					opinionTerm.setTerm(text);
					opinionTerm.setTF(tf);					

					if (sentimentAnalyzer != null) {
						double polarity = sentimentAnalyzer.analyzePolarity(text);
						opinionTerm.setPolarity(polarity);			
					}

					opinionTerms.put(text, opinionTerm);
				}			
			} else if (!excludeStopwords && opinionTerms.get(text) == null) {
				int tf = this.getTF(object, field, text);
				if (tf >= minTF) {
					OpinionTerm opinionTerm = new OpinionTerm();
					opinionTerm.setType(field);
					opinionTerm.setObject(object);
					opinionTerm.setFeature(feature);
					opinionTerm.setTerm(text);
					opinionTerm.setTF(tf);

					if (sentimentAnalyzer != null) {
						double polarity = sentimentAnalyzer.analyzePolarity(text);
						opinionTerm.setPolarity(polarity);			
					}

					opinionTerms.put(text, opinionTerm);
				}				
			}
		}

		return new ArrayList<OpinionTerm>(opinionTerms.values());
	}
	*/
	
	public List<OpinionTerm> searchTerms(String object, String language, String field, 
			int minTF, boolean excludeStopwords, boolean byFeature) throws IOException {	

		Map<String, OpinionTerm> opinionTerms = new HashMap<String, OpinionTerm>();
		
		Term objTerm = new Term(FieldConstants.OBJECT, object);	
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		Term term = new Term(FieldConstants.LANGUAGE, language);
		Query query = new TermQuery(term);

		TopDocs rs = searcher.search(query, filter, 1000000);
		System.out.println("search: byFeature = " + byFeature + ", field = " + field + ", minTF = " + minTF + ", docFreq = " + rs.totalHits);
		
		// get sentiment analyzer suitable for the language
		SentimentAnalyzer sentimentAnalyzer = sentimentAnalyzers.get(language);

		for (int i = 0; i < rs.totalHits; i++) {
			int docId = rs.scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			String text = doc.get(field);
			//String feature = this.getFeatureName(doc);
			String feature = doc.get(FieldConstants.CLAUSE_MAIN_FEATURE);
			
			String termId = text;			
			if (byFeature)
				termId = feature + "-" + text;
			
			if (excludeStopwords && opinionTerms.get(termId) == null && !stopwordSet.contains(text)) {
				int tf = 0;
				if (byFeature) {					
					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);	
					queryMap.put(field, text);									
					tf = this.getTF(object, queryMap);					
				}
				else {
					tf = this.getTF(object, field, text);
				}
				
				if (tf >= minTF) {
					OpinionTerm opinionTerm = new OpinionTerm();
					opinionTerm.setId(termId);
					opinionTerm.setType(field);
					opinionTerm.setObject(object);
					opinionTerm.setFeature(feature);
					opinionTerm.setTerm(text);
					opinionTerm.setTF(tf);					

					if (sentimentAnalyzer != null) {
						double polarity = sentimentAnalyzer.analyzePolarity(text);
						opinionTerm.setPolarity(polarity);			
					}

					opinionTerms.put(termId, opinionTerm);
				}			
			} else if (!excludeStopwords && opinionTerms.get(termId) == null) {
				int tf = 0;
				if (byFeature) {					
					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);	
					queryMap.put(field, text);									
					tf = this.getTF(object, queryMap);					
				}
				else {
					tf = this.getTF(object, field, text);
				}
				
				if (tf >= minTF) {
					OpinionTerm opinionTerm = new OpinionTerm();
					opinionTerm.setId(termId);
					opinionTerm.setType(field);
					opinionTerm.setObject(object);
					opinionTerm.setFeature(feature);
					opinionTerm.setTerm(text);
					opinionTerm.setTF(tf);

					if (sentimentAnalyzer != null) {
						double polarity = sentimentAnalyzer.analyzePolarity(text);
						opinionTerm.setPolarity(polarity);			
					}

					opinionTerms.put(termId, opinionTerm);
				}				
			}
		}

		return new ArrayList<OpinionTerm>(opinionTerms.values());
	}
		
	public List<OpinionTerm> searchLinkedTerms(String object, String language, OpinionTerm baseTerm, 
			String linkedField, int minTF, int minLinkedTF, boolean byFeature) throws Exception {
		
		Map<String, OpinionTerm> linkedTerms = new HashMap<String, OpinionTerm>();
		
		Term objTerm = new Term(FieldConstants.OBJECT, object);		
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		String baseField =  baseTerm.getType();
		String baseText = baseTerm.getTerm();
		String feature = baseTerm.getFeature();
		
		Term term1 = new Term(FieldConstants.LANGUAGE, language);
		Query query1 = new TermQuery(term1);
		
		Term term2 = new Term(baseField, baseText);
		Query query2 = new TermQuery(term2);
		
		Term term3 = new Term(FieldConstants.CLAUSE_MAIN_FEATURE, feature);
		Query query3 = new TermQuery(term3);
		
		BooleanQuery bQuery = new BooleanQuery();
		bQuery.add(query1, BooleanClause.Occur.MUST);
		bQuery.add(query2, BooleanClause.Occur.MUST);		
		if (byFeature)
			bQuery.add(query3, BooleanClause.Occur.MUST);

		TopDocs rs = searcher.search(bQuery, filter, 1000000);
		System.out.println("search: feature = " + feature + ", field = " + baseField + ", text = " + baseText + ", linkedField = " + linkedField);

		// get sentiment analyzer suitable for the language
		SentimentAnalyzer sentimentAnalyzer = sentimentAnalyzers.get(language);

		for (int i = 0; i < rs.totalHits; i++) {
			int docId = rs.scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			String linkedText = doc.get(linkedField);	
			DetailDoc detailDoc = this.makeDetailDoc(doc);
			
			String termId = linkedText;
			if (byFeature)
				termId = feature + "-" + linkedText;
			
			baseTerm.addDoc(detailDoc);

			if (linkedField.equals(FieldConstants.SUBJECT) || linkedField.equals(FieldConstants.PREDICATE)) {
				OpinionTerm exist = linkedTerms.get(termId);
				if (exist == null && !stopwordSet.contains(linkedText)) {
					int tf = 0;
					if (byFeature) {					
						Map<String, String> queryMap = new HashMap<String, String>();
						queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);	
						queryMap.put(linkedField, linkedText);									
						tf = this.getTF(object, queryMap);					
					}
					else {
						tf = this.getTF(object, linkedField, linkedText);
					}

					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put(baseField, baseText);
					queryMap.put(linkedField, linkedText);
					if (byFeature)
						queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);
					int linkedTF = this.getTF(object, queryMap);
					
					if (tf >= minTF && linkedTF >= minLinkedTF) {
						OpinionTerm linkedTerm = new OpinionTerm();
						linkedTerm.setId(termId);
						linkedTerm.setType(linkedField);
						linkedTerm.setObject(object);
						linkedTerm.setFeature(feature);
						linkedTerm.setTerm(linkedText);
						linkedTerm.setTF(tf);
						linkedTerm.setLinkedTF(linkedTF);
						linkedTerm.addDoc(detailDoc);	
						
						double polarity = 0.0;
						if (sentimentAnalyzer != null) {
							polarity = sentimentAnalyzer.analyzePolarity(linkedText);							
						}
						linkedTerm.setPolarity(polarity);
						
						linkedTerms.put(termId, linkedTerm);						
						System.out.println("-> link: feature = " + feature + ", field = " + linkedField + ", text = " + linkedText + " (tf: " + tf + " polarity: " + polarity + ")");
					}
				}
				else if (exist != null && !stopwordSet.contains(linkedText)) {
					exist.addDoc(detailDoc);
				}
			} else if (linkedField.equals(FieldConstants.ATTRIBUTE)) {
				Map<String, Integer[]> mapAttribute = this.tokenizeAttributes(object, feature, baseField, baseText, linkedText, byFeature);

				for (Map.Entry<String, Integer[]> entry : mapAttribute.entrySet()) {
					String attributeText = entry.getKey();
					Integer[] attributeTFs = (Integer[]) entry.getValue();
					int attributeTF = attributeTFs[0];
					int attributeLinkedTF = attributeTFs[1];
					
					String attrTermId = attributeText;
					if (byFeature)
						attrTermId = feature + "-" + attributeText;

					OpinionTerm exist = linkedTerms.get(attrTermId);					
					if (exist == null && !stopwordSet.contains(attributeText)) {
						if (attributeTF >= minTF && attributeLinkedTF >= minLinkedTF) { 
							OpinionTerm linkedTerm = new OpinionTerm();
							linkedTerm.setId(attrTermId);
							linkedTerm.setType(linkedField);
							linkedTerm.setObject(object);
							linkedTerm.setFeature(feature);
							linkedTerm.setTerm(attributeText);
							linkedTerm.setTF(attributeTF);
							linkedTerm.setLinkedTF(attributeLinkedTF);
							linkedTerm.addDoc(detailDoc);					
							
							double polarity = 0.0;
							if (sentimentAnalyzer != null) {
								polarity = sentimentAnalyzer.analyzePolarity(attributeText);								
							}
							linkedTerm.setPolarity(polarity);
							
							linkedTerms.put(attrTermId, linkedTerm);							
							System.out.println("-> link: feature = " + feature + ", field = " + linkedField + ", text = " + attributeText + " (tf: " + attributeTF + " polarity: " + polarity + ")");
						}
					}
					else if (exist != null && !stopwordSet.contains(attributeText)) {
						exist.addDoc(detailDoc);
					}
				}
			}
		}

		return new ArrayList<OpinionTerm>(linkedTerms.values());
	}
	
	public List<DetailDoc> searchFeatures(String object, String language) throws IOException {
		List<DetailDoc> detailDocs = new ArrayList<DetailDoc>();
		Term objTerm = new Term(FieldConstants.OBJECT, object);	
		TermsFilter filter = new TermsFilter();
		filter.addTerm(objTerm);
		
		Term term = new Term(FieldConstants.LANGUAGE, language);
		Query query = new TermQuery(term);

		TopDocs rs = searcher.search(query, filter, 1000000);

		for (int i = 0; i < rs.totalHits; i++) {
			int docId = rs.scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			detailDocs.add(this.makeDetailDoc(doc));
		}
		
		return detailDocs;
	}
	
	/*
	private String getFeatureName(Document doc) {
		String feature = doc.get(FieldConstants.MAIN_FEATURE);
		String clauseFeature = doc.get(FieldConstants.CLAUSE_MAIN_FEATURE);
		
		if (clauseFeature != null && !clauseFeature.equals("") && !clauseFeature.equalsIgnoreCase("ETC")) {
			return clauseFeature;
		}
		else {
			return feature;
		}
	}
	*/
	 
	private DetailDoc makeDetailDoc(Document doc) {
		DetailDoc detailDoc = new DetailDoc();
		detailDoc.setSite(doc.get(FieldConstants.SITE));
		detailDoc.setObject(doc.get(FieldConstants.OBJECT));
		detailDoc.setLanguage(doc.get(FieldConstants.LANGUAGE));
		detailDoc.setCollectDate(doc.get(FieldConstants.COLLECT_DATE));
		detailDoc.setDocId(doc.get(FieldConstants.DOC_ID));
		detailDoc.setDate(doc.get(FieldConstants.DATE));
		detailDoc.setUserId(doc.get(FieldConstants.USER_ID));
		detailDoc.setUserName(doc.get(FieldConstants.USER_NAME));
		detailDoc.setFeature(doc.get(FieldConstants.FEATURE));
		detailDoc.setMainFeature(doc.get(FieldConstants.MAIN_FEATURE));
		detailDoc.setClauseFeature(doc.get(FieldConstants.CLAUSE_FEATURE));
		detailDoc.setClauseMainFeature(doc.get(FieldConstants.CLAUSE_MAIN_FEATURE));
		detailDoc.setSubject(doc.get(FieldConstants.SUBJECT));
		detailDoc.setPredicate(doc.get(FieldConstants.PREDICATE));
		detailDoc.setAttribute(doc.get(FieldConstants.ATTRIBUTE));
		detailDoc.setText(doc.get(FieldConstants.TEXT));
		detailDoc.setPolarity(Double.valueOf(doc.get(FieldConstants.POLARITY)));
		detailDoc.setPolarityStrength(Double.valueOf(doc.get(FieldConstants.POLARITY_STRENGTH)));
		detailDoc.setClausePolarity(Double.valueOf(doc.get(FieldConstants.CLAUSE_POLARITY)));
		detailDoc.setClausePolarityStrength(Double.valueOf(doc.get(FieldConstants.CLAUSE_POLARITY_STRENGTH)));
		
		return detailDoc;
	}
	
	private Map<String, Integer[]> tokenizeAttributes(String object, String feature, String searchField, String searchText, 
			String attributes, boolean byFeature) throws Exception {
		Map<String, Integer[]> map = new HashMap<String, Integer[]>();
		
		if (attributes != null) {
			StringTokenizer st = new StringTokenizer(attributes.trim(), " ");
			while (st.hasMoreTokens()) {
				String attribute = st.nextToken();
				if (!stopwordSet.contains(attribute.trim()) && !attribute.trim().equals("")) {
					int tf = 0;
					if (byFeature) {					
						Map<String, String> queryMap = new HashMap<String, String>();
						queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);	
						queryMap.put(FieldConstants.ATTRIBUTE, attribute);									
						tf = this.getTF(object, queryMap);					
					}
					else {
						tf = this.getTF(object, FieldConstants.ATTRIBUTE, attribute);
					}
															
					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put(searchField, searchText);
					queryMap.put(FieldConstants.ATTRIBUTE, attribute);
					if (byFeature)
						queryMap.put(FieldConstants.CLAUSE_MAIN_FEATURE, feature);
					
					int linkedTF = this.getTF(object, queryMap);
					Integer[] tfs = { tf, linkedTF };
					map.put(attribute, tfs);
				}
			}			
		}
	
		return map;
	}
	
	public static File[] getIndexDirectories(String baseDir, Date start, Date end) {
		
		int days = (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 *24));

		System.out.println("start == " + start);
		System.out.println("end == " + end);
		System.out.println("end - start == " + days + " days");
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		File[] directories = new File[days+1];
		for (int day = 0 ; day <= days; day++) {
			cal.setTime(start);
			cal.add(Calendar.DAY_OF_YEAR, day);
			Date date = cal.getTime();
			String strDate = df.format(date);
			directories[day] = new File(baseDir + File.separator + strDate);
			System.out.println(directories[day]);
		}        
		
		return directories;
	}
	
	public static void main(String[] args) {		
		try {	
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());			
			cal.add(Calendar.DAY_OF_YEAR, -3);
			Date start = cal.getTime();			
			Date end = new Date();
			DocIndexSearcher.getIndexDirectories("./bin/data/appstore/index/", start, end);
			
			File[] indexDirs = new File[1];
			indexDirs[0] = new File("./bin/data/appstore/index/20111219");
			String object = "naverline";
			File liwcFile = new File("./bin/liwc/LIWC_ko.txt");
			DocIndexSearcher searcher = new DocIndexSearcher(indexDirs);
			searcher.putStopwordFile(new File("./conf/stopword_ko.txt"));
			searcher.putSentimentAnalyzer(FieldConstants.LANG_KOREAN, SentimentAnalyzer.getInstance(liwcFile));
			
			boolean excludeStopwords = true;
			boolean byFeature = true;			
			List<OpinionTerm> baseTerms = searcher.searchTerms(object, FieldConstants.LANG_KOREAN, FieldConstants.SUBJECT, 10, excludeStopwords, byFeature);
						
			for (OpinionTerm term : baseTerms) {
				System.out.println("--------------------------------------");
				searcher.searchLinkedTerms(object, FieldConstants.LANG_KOREAN, term, FieldConstants.PREDICATE, 10, 1, byFeature);
				searcher.searchLinkedTerms(object, FieldConstants.LANG_KOREAN, term, FieldConstants.ATTRIBUTE, 10, 1, byFeature);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}