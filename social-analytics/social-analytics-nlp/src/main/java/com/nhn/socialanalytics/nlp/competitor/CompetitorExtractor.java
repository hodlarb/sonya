package com.nhn.socialanalytics.nlp.competitor;

public class CompetitorExtractor {

	private CompetitorDictionary dictionary;
	
	/** Arrays of competitors that aren't included in one instance analysis (corpus analysis only). **/
	private static final String[] absoluteCountCompetitors = { "WC", "WPS", "DIC" };
	/** Set of competitors that aren't included in the models. **/
	private Set<String> absoluteCountCompetitorSet;
	
	public CompetitorExtractor(File catFile) {
		try {
			dictionary = new CompetitorDictionary(catFile);
			
			// load absolute competitors
			absoluteCountCompetitorSet = new LinkedHashSet<String>(Arrays.asList(absoluteCountCompetitors));	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, Double> getCompetitorCounts(String text, boolean absoluteCounts) {
		
		Map<String, Double> counts = new LinkedHashMap<String, Double>();	
		
		if (text == null || text.trim().equals("")) {
			return counts;
		}
		
		Map<String, Double> initCounts = dictionary.getCounts(text, absoluteCounts);
		Map<String, Double> sortedCounts = dictionary.sort(initCounts, false);
		
		sortedCounts.keySet().removeAll(absoluteCountCompetitorSet);
		
		for (String competitor : sortedCounts.keySet()) { 
			double count = (Double) sortedCounts.get(competitor);
			if (count > 0.0)
				counts.put(competitor, count);
		}
		
		return counts;
	}
	
	public static void main(String[] args) {
		CompetitorExtractor competitorExtractor = new CompetitorExtractor(new File("./dic/competitor/competitor_mobile.txt"));
		competitorExtractor.loadDictionary("naverline");
		
		String text = "ライン 네이버라인은 디자인은 이쁜데, 속도는 카카오톡 마이피플 mypeople 더 빠르다.";
		Map<String, Double> competitors = competitorExtractor.getCompetitorCounts(text, true);
		System.out.println("matched competitors == " + competitors);
	}
}