package com.tianji.aigc.memory;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

@Component
public class TokenCounter {

    private static final double AVG_CHARS_PER_TOKEN = 2.0;
    private static final double AVG_ENGLISH_CHARS_PER_TOKEN = 4.0;

    public int countTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }

        int chineseCount = 0;
        int englishCount = 0;
        int otherCount = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCount++;
            } else if (Character.isLetter(c) && c < 128) {
                englishCount++;
            } else {
                otherCount++;
            }
        }

        double chineseTokens = chineseCount / AVG_CHARS_PER_TOKEN;
        double englishTokens = englishCount / AVG_ENGLISH_CHARS_PER_TOKEN;
        double otherTokens = otherCount / 4.0;

        return (int) Math.ceil(chineseTokens + englishTokens + otherTokens);
    }

    public int estimateTokens(String text) {
        return countTokens(text);
    }
}
