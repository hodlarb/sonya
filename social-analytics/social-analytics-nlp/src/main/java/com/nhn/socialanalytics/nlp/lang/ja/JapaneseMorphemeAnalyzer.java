package com.nhn.socialanalytics.nlp.lang.ja;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

import com.nhn.socialanalytics.nlp.morpheme.MorphemeAnalyzer;
import com.nhn.socialanalytics.nlp.morpheme.Sentence;

public class JapaneseMorphemeAnalyzer implements MorphemeAnalyzer {
	
	private static JapaneseMorphemeAnalyzer instance = null;
	private StringTagger tagger;
	
	public JapaneseMorphemeAnalyzer() {
		tagger = SenFactory.getStringTagger("");
	}
	
	public static JapaneseMorphemeAnalyzer getInstance() {
		if (instance == null)
			instance = new JapaneseMorphemeAnalyzer();
		return instance;
	}

	public Sentence analyze(String text) {
		System.out.println("sentence == " + text);	
		
		Sentence sentence = new Sentence(text);
		
		try {
		List<Token> tokens = new ArrayList<Token>();
		tokens = tagger.analyze(text, tokens);

		int index = 0;
		for (Token token : tokens) {
			JapaneseToken jtoken = new JapaneseToken();			
			jtoken.makeObject(index, token);
			
			System.out.println(jtoken.toString());
			
			sentence.add(jtoken);			
			index++;
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sentence;
	}
	
	public String extractTerms(String text) {
		return null;
	}
	
	public String extractCoreTerms(String text) {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		//String text = "もう眠い";
		//String text = "上記のように最新のソースでは Listを 参照渡し風にしているようです。";
		String text = "何も問題なく快適。要望が２つ、１つはチャットのスタンプが大きすぎてインパクトはあるけどすぐログが飛ぶので少し小さくするか大きさの設定項目の追加。１つは過去ログ見てる時はコメントが来ても最下部まで降りないでほしぃです。（最下部閲覧中のみコメント来ても最下部張り付きが望ましい）お願いします。";
		
		JapaneseMorphemeAnalyzer analyzer = JapaneseMorphemeAnalyzer.getInstance();
		analyzer.analyze(text);
	}

}