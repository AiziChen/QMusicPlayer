package quanye.qmuiscplayer;

import java.util.Random;

/**
 * @author 陈权业
 * 工具类
 */

public class Tools {

    private static final String[] hellos = {
            "祝你天天开心！每天好心情！(^_^)",
            "活出自己吧！(^_^)",
            "音乐都给你，亲爱的！(^_^)"
    };

    /**
     * 随机问候语
     * @return 一个问候语
     */
    public static String randomHello() {
        Random rd = new Random();
        int ni = rd.nextInt(hellos.length);
        return hellos[ni];
    }
}
