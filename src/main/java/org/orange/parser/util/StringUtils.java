package org.orange.parser.util;

public class StringUtils {
    private StringUtils() {}

    /** 修剪头尾不可见符，包括\p{javaWhitespace}\u00a0\u3000 */
    public static String trim(String src){
        if(src!=null)
            return src.replaceAll("(^[\\p{javaWhitespace}\u00a0\u3000])|([\\p{javaWhitespace}\u00a0\u3000]$)", "");
            // ideographic space 0x3000 &#12288(HTML);
        else
            return null;
    }
    /** 删除所有不可见字符，包括\p{javaWhitespace}\u00a0\u3000 */
    public static String deleteSpace(String src){
        if(src!=null)
            return src.replaceAll("[\\p{javaWhitespace}\u00a0\u3000]", "");
        else
            return null;
    }
}
