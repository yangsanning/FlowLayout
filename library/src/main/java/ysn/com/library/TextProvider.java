package ysn.com.library;

import android.widget.TextView;

/**
 * @Author yangsanning
 * @ClassName TextProvider
 * @Description 用于展示item的text
 * @Date 2019/3/27
 * @History 2019/3/27 author: description:
 */
public interface TextProvider<T> {

    /**
     * 根据data和position获取需要展示的数据
     */
    CharSequence getLabelText(TextView textView, int position, T data);
}
