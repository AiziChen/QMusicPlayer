package quanye.qmuiscplayer;

/**
 * @author 陈权业
 * 音乐控制常量 —— 已经用字符串代替
 */

@Deprecated
public enum Cons {
    NEXT("next"), PREVIOUS("previous"), START("start"), RESUME("resume");
    private String name;
    private Cons(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
