package ysn.com.library;

/**
 * @Author yangsanning
 * @ClassName SelectType
 * @Description FlowLayout的选择类型
 * @Date 2019/3/27
 * @History 2019/3/27 author: description:
 */
public enum SelectType {

    /**
     * 不可选中且不响应选中事件回调（默认）
     */
    NONE(1),

    /**
     * 单选,可以反选。
     */
    SINGLE(2),

    /**
     * 单选,不可以反选（至少有一个是选中的，默认选中defaultSelect）
     */
    SINGLE_NOT_RESELECT(3),

    /**
     * 多选
     */
    MULTI(4);

    int value;

    SelectType(int value) {
        this.value = value;
    }

    static SelectType get(int value) {
        switch (value) {
            case 1:
                return NONE;
            case 2:
                return SINGLE;
            case 3:
                return SINGLE_NOT_RESELECT;
            case 4:
                return MULTI;
            default:
                return NONE;
        }
    }
}
