package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class HuskyChineseHelper {
    ArrayList<String> arrayList = new ArrayList<>();
    HashMap<String,Integer> result = new HashMap<>();
    public String[] help() {
        HashMap<String,Integer> pinyinToInteger = new HashMap<>();
        read();
        String[] pinyinList = new String[arrayList.size()];
        for(int i = 0; i<pinyinList.length; i++){
            pinyinList[i]=arrayList.get(i);
        }
        Arrays.sort(pinyinList);
        int pos = 0;
        for(int i = 0; i<pinyinList.length; i++){
            for(int j = 1; j<5; j++){
                int x = result.size();
                result.put(pinyinList[i]+String.valueOf(j),pos);
                int y =result.size();
                if(x==y){
                    System.out.println(i);
                }
                pos++;
            }
        }
        return pinyinList;
    }

    private void read() {
        String file = "pinyin.txt";
        try {
            readFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                arrayList.add(line);
            }
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
    public String[][] getPinyin(String src) {
        char[] srcChar;
        srcChar = src.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

        // output config
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        String[][] temp = new String[src.length()][];
        for (int i = 0; i < srcChar.length; i++) {
            char c = srcChar[i];
            try {
                temp[i] = PinyinHelper.toHanyuPinyinStringArray(
                        srcChar[i], hanYuPinOutputFormat);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        return temp;
    }
}
