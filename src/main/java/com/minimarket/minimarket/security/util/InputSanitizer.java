package com.minimarket.minimarket.security.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class InputSanitizer {

    public static String sanitizeInput(String input){
        if(input == null){
            return null;
        }
        return Jsoup.clean(input, Safelist.basic());

    }

}
