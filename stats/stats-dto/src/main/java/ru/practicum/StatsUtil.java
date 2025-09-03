package ru.practicum;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StatsUtil {

    public final String IP_ADDRESS_PATTERN = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)[.]){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public String getIpAddressOrDefault(String ip) {
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "0.0.0.0";
        }
    }

}
