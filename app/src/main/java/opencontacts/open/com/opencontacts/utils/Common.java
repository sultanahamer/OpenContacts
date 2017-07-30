package opencontacts.open.com.opencontacts.utils;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sultanm on 7/30/17.
 */

public class Common {
    static Map<Character, Integer> characterToIntegerMappingForKeyboardLayout;
    static {
        characterToIntegerMappingForKeyboardLayout = new HashMap();
        int[] numericsMappingForAlphabetsInNumberKeypad = { 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9};
        for(int i=0, charCodeForA = 65; i<26; i++){
            characterToIntegerMappingForKeyboardLayout.put((char) (charCodeForA + i), numericsMappingForAlphabetsInNumberKeypad[i]);
        }
    }
    public static String getNumericKeyPadNumberForString(String string){
        StringBuffer numericString = new StringBuffer();
        for(char c: string.toCharArray()){
            if(Character.isSpaceChar(c)){
                numericString.append(" ");
                continue;
            }
            Integer numericCode = characterToIntegerMappingForKeyboardLayout.get(Character.toUpperCase(c));
            if(numericCode == null)
                numericString.append("");
            else
                numericString.append(characterToIntegerMappingForKeyboardLayout.get(Character.toUpperCase(c)));
        }
        return numericString.toString();
    }

    public static String getDurationInMinsAndSecs(int duration){
        NumberFormat twoDigitFormat = NumberFormat.getInstance();
        twoDigitFormat.setMinimumIntegerDigits(2);
        return twoDigitFormat.format(duration / 60) + ":" + twoDigitFormat.format(duration % 60);
    }
}
