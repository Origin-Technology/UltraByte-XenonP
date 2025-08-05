package dev.opan.utils.miscellaneous;

import java.util.List;

public class ListUtils {
    public static int getIndex(List<String> list, String target) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(target)) return i;
        }

        return -1;
    }

    public static void waitSeconds(int seconds) {
        if (seconds == 0) {
            return;
        }

        try {
            // 将秒数转换为毫秒
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }
}
