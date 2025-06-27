package ch.kirby.util;

import java.util.Map;

public class SharedFormatter {

    public static String formatBreakdown(Map<String, Integer> breakdown) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("h\n");
        }
        return sb.toString();
    }
}
