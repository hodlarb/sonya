package com.nhn.socialanalytics.twitter.collect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.nhn.socialanalytics.common.Config;
import com.nhn.socialanalytics.common.JobLogger;
import com.nhn.socialanalytics.nlp.kr.morpheme.MorphemeAnalyzer;
import com.nhn.socialanalytics.twitter.parse.TwitterParser;

public class TwitterDataCollector {

	private static JobLogger logger = JobLogger.getLogger(TwitterDataCollector.class, "twitter-collect.log");
	private File outputDir;
	
	private Twitter twitter;
	
	public TwitterDataCollector() {	
		outputDir = new File(Config.getProperty("TWITTER_SOURCE_DATA_DIR"));
		if (!outputDir.exists()) {
			outputDir.mkdir();
			logger.info(outputDir + " is created.");
		}
		
		twitter = new TwitterFactory().getInstance();
	}
	
	public List<twitter4j.Tweet> searchTweets(String objectId, String strQuery, String since, String until, int maxPage) {
		List<twitter4j.Tweet> tweets = new ArrayList<twitter4j.Tweet>();
		
		try {           	
        	Query query = new Query(strQuery);
        	query.rpp(100);
        	query.setSince(since);
        	
        	logger.info("------------------------------------------------");
        	logger.info("object id: " + objectId);
        	logger.info("query = " + query.getQuery() + " since: " + since + " page: " + maxPage);
         	
        	for (int page = 1; page <= maxPage; page++) {
	        	query.page(page);		        	
	            QueryResult result = twitter.search(query);
	            
	            logger.info("query result size[page:" + page + "] = " + result.getTweets().size());
	            tweets.addAll(result.getTweets());
        	}
           
		} catch (TwitterException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		return tweets;
	}
	
	public void writeOutput(String objectId, List<twitter4j.Tweet> tweets) throws IOException {
				
		File file = new File(outputDir.getPath() + File.separator + objectId + ".txt");
		File fileSource = new File(outputDir.getPath() + File.separator + objectId + "_src.txt");		
		
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));
		BufferedWriter brSource = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileSource.getPath()), "UTF-8"));
			
		br.write("object_id	tweet_id	created_at	from_user	to_user	filtered_text1	filtered_text2");
		br.newLine();
			
		MorphemeAnalyzer morph = MorphemeAnalyzer.getInstance();
		
		// post
		for (twitter4j.Tweet tweet : tweets) {
			String text = TwitterParser.extractContent(tweet.getText());
						
			//String orgText = tweet.getText().replaceAll("\n", " ").replaceAll("\t", " ");
			
			br.write(
					objectId + "\t" +
					tweet.getId() + "\t" +
					tweet.getCreatedAt() + "\t" + 
					tweet.getFromUser() + "\t" +
					tweet.getToUser() + "\t" +
					//orgText + "\t" +
					morph.extractTerms(text) + "\t" +	// filtered text1
					morph.extractCoreTerms(text) 		// filtered text2
					);
			br.newLine();
			
			//////////////////////
			brSource.write(tweet.getText());
			brSource.newLine();
		}
		br.close();
		brSource.close();
	}
	
	public static void main(String[] args) {
		TwitterDataCollector collector = new TwitterDataCollector();
		
		String objectId = "navertalk";
		String query = "네이버톡";
		
		//String objectId = "naver";
		//String query = "네이버";
		
		//String objectId = "poll";
		//String query = "선거";
		
		//String objectId = "galaxy";
		//String query = "갤럭시 노트";
		
		List<twitter4j.Tweet> tweets = collector.searchTweets(objectId, query, "2011-08-01", null, 10);
		
		try {
			collector.writeOutput(objectId, tweets);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
