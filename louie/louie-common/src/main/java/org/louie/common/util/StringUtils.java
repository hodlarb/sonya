package org.louie.common.util;

import java.lang.Character.UnicodeBlock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is string utilities.
 * 
 * @author Younggue Bae
 */
public class StringUtils {
	
	public static final String replaceStrings(String text, String regex, String newStr) {
		if (text == null) {
			return null;
		}
	
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(text);
		return matcher.replaceAll(newStr);
	}

	public static final String removeKoreanUnsupportedCharacters(String str) {
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
			
			if ( !(Character.isDigit(ch)
					|| UnicodeBlock.HANGUL_SYLLABLES.equals(unicodeBlock)
					|| UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals(unicodeBlock)
					|| UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock)
					|| UnicodeBlock.BASIC_LATIN.equals(unicodeBlock)) 
				) {
				str = str.replace(ch, ' ');
			}
			
			str = str.replaceAll("ᆢ", "");
			str = str.replaceAll("'", "");
			str = str.replaceAll("ﾟ", "");
		}
		
		return str;
	}
	
	public static final boolean isEmpty(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		}
		
		return false;
	}
	
}
