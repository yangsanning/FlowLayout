package ysn.com.library;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangsanning
 * @ClassName FlowLayout
 * @Description 一句话概括作用
 * @Date 2019/3/27
 * @History 2019/3/27 author: description:
 */
public class FlowLayout extends ViewGroup implements View.OnClickListener {

    /**
     * TextView绑定数据（item关联数据）
     */
    private static final int KEY_DATA = R.id.fl_tag_key_data;
    private static final int KEY_POSITION = R.id.fl_tag_key_position;

    /**
     * 保存View信息的key
     */
    private static final String KEY_SUPER_STATE = "key_super_state";
    private static final String KEY_TEXT_COLOR_STATE = "key_text_color_state";
    private static final String KEY_TEXT_SIZE_STATE = "key_text_size_state";
    private static final String KEY_PADDING_STATE = "key_padding_state";
    private static final String KEY_COLUMN_SPACE_STATE = "key_column_space_state";
    private static final String KEY_ROW_SPACE_STATE = "key_row_space_state";
    private static final String KEY_SELECT_TYPE_STATE = "key_select_type_state";
    private static final String KEY_MAX_SELECT_STATE = "key_max_select_state";
    private static final String KEY_MAX_LINES_STATE = "key_max_lines_state";
    private static final String KEY_SELECT_LIST_STATE = "key_select_list_state";
    private static final String KEY_KEEP_LIST_STATE = "key_keep_list_state";

    private Context context;

    private Drawable bgDrawable;
    private ColorStateList textColor;
    private float textSize;

    private int paddingLeft, paddingTop, paddingRight, paddingBottom;
    /**
     * 列间距
     */
    private int columnSpace;
    /**
     * 行间距
     */
    private int rowSpace;

    private SelectType selectType;

    private int defaultSelect;
    private int maxSelect;
    /**
     * 最大行数，小于等于0则不限行数
     */
    private int maxLines;

    private ArrayList<Object> dataList = new ArrayList<>();

    /**
     * 必选项（在多选模式下，可以设置必选项，必选项默认选中，不能反选）
     */
    private ArrayList<Integer> keepList = new ArrayList<>();
    private ArrayList<Integer> selectList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;
    private OnSelectChangeListener onSelectChangeListener;
    private OnMoreThanMaxLineListener onMoreThanMaxLineListener;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
            int type = typedArray.getInt(R.styleable.FlowLayout_fl_select_type, 1);
            selectType = SelectType.get(type);

            defaultSelect = typedArray.getInteger(R.styleable.FlowLayout_fl_default_select, 0);
            maxSelect = typedArray.getInteger(R.styleable.FlowLayout_fl_max_select, 0);
            maxLines = typedArray.getInteger(R.styleable.FlowLayout_fl_max_lines, 0);
            textColor = typedArray.getColorStateList(R.styleable.FlowLayout_fl_text_color);
            textSize = typedArray.getDimension(R.styleable.FlowLayout_fl_text_size, sp2px(context, 14));

            paddingLeft = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_padding_left, 0);
            paddingTop = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_padding_top, 0);
            paddingRight = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_padding_right, 0);
            paddingBottom = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_padding_bottom, 0);

            rowSpace = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_row_space, 0);
            columnSpace = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_fl_column_space, 0);

            int bgResId = typedArray.getResourceId(R.styleable.FlowLayout_fl_bg, R.drawable.bg_flow_layout);
            if (bgResId != 0) {
                bgDrawable = getResources().getDrawable(bgResId);
            } else {
                int labelBgColor = typedArray.getColor(R.styleable.FlowLayout_fl_bg, Color.TRANSPARENT);
                bgDrawable = new ColorDrawable(labelBgColor);
            }

            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int count = getChildCount();
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        //记录内容的高度
        int contentHeight = 0;
        //记录行的宽度
        int lineWidth = 0;
        //记录最宽的行宽
        int maxLineWidth = 0;
        //记录一行中item高度最大的高度
        int maxItemHeight = 0;
        int lineCount = 1;

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);

            if (lineWidth + view.getMeasuredWidth() > maxWidth) {
                lineCount++;
                if (maxLines > 0 && lineCount > maxLines) {
                    if (onMoreThanMaxLineListener != null) {
                        onMoreThanMaxLineListener.onMoreThan(contentHeight);
                    }
                    break;
                }
                contentHeight += rowSpace;
                contentHeight += maxItemHeight;
                maxItemHeight = 0;
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0;
            }

            lineWidth += view.getMeasuredWidth();
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());

            if (i != count - 1) {
                if (lineWidth + columnSpace > maxWidth) {
                    // 换行
                    lineCount++;
                    if (maxLines > 0 && lineCount > maxLines) {
                        break;
                    }
                    contentHeight += rowSpace;
                    contentHeight += maxItemHeight;
                    maxItemHeight = 0;
                    maxLineWidth = Math.max(maxLineWidth, lineWidth);
                    lineWidth = 0;
                } else {
                    lineWidth += columnSpace;
                }
            }
        }

        contentHeight += maxItemHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        setMeasuredDimension(measureWidth(widthMeasureSpec, maxLineWidth), measureHeight(heightMeasureSpec, contentHeight));
    }

    private int measureWidth(int measureSpec, int contentWidth) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentWidth + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return Math.max(result, getSuggestedMinimumWidth());
    }

    private int measureHeight(int measureSpec, int contentHeight) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentHeight + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return Math.max(result, getSuggestedMinimumHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int x = getPaddingLeft();
        int y = getPaddingTop();

        int contentWidth = right - left;
        int maxItemHeight = 0;
        int lineCount = 1;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            if (contentWidth < x + view.getMeasuredWidth() + getPaddingRight()) {
                lineCount++;
                if (maxLines > 0 && lineCount > maxLines) {
                    break;
                }
                x = getPaddingLeft();
                y += rowSpace;
                y += maxItemHeight;
                maxItemHeight = 0;
            }
            view.layout(x, y, x + view.getMeasuredWidth(), y + view.getMeasuredHeight());
            x += view.getMeasuredWidth();
            x += columnSpace;
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
        }
    }

    /**
     * 保存数据
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());

        //保存item文字颜色
        if (textColor != null) {
            bundle.putParcelable(KEY_TEXT_COLOR_STATE, textColor);
        }
        //保存item文字大小
        bundle.putFloat(KEY_TEXT_SIZE_STATE, textSize);

        //保存item内边距
        bundle.putIntArray(KEY_PADDING_STATE, new int[]{paddingLeft, paddingTop, paddingRight, paddingBottom});
        //保存item列间隔
        bundle.putInt(KEY_COLUMN_SPACE_STATE, columnSpace);
        //保存行间隔
        bundle.putInt(KEY_ROW_SPACE_STATE, rowSpace);

        //保存item的选择类型
        bundle.putInt(KEY_SELECT_TYPE_STATE, selectType.value);
        //保存item的最大选择数量
        bundle.putInt(KEY_MAX_SELECT_STATE, maxSelect);
        //保存item的最大行数
        bundle.putInt(KEY_MAX_LINES_STATE, maxLines);

        //保存已选择的item列表
        if (!selectList.isEmpty()) {
            bundle.putIntegerArrayList(KEY_SELECT_LIST_STATE, selectList);
        }

        //保存必选项列表
        if (!keepList.isEmpty()) {
            bundle.putIntegerArrayList(KEY_KEEP_LIST_STATE, keepList);
        }
        return bundle;
    }

    /**
     * 恢复数据
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));

            //恢复item文字颜色
            ColorStateList color = bundle.getParcelable(KEY_TEXT_COLOR_STATE);
            if (color != null) {
                setTextColor(color);
            }
            //恢复item文字大小
            setTextSize(bundle.getFloat(KEY_TEXT_SIZE_STATE, textSize));

            //恢复item内边距
            int[] padding = bundle.getIntArray(KEY_PADDING_STATE);
            if (padding != null && padding.length == 4) {
                setTextPadding(padding[0], padding[1], padding[2], padding[3]);
            }
            //恢复item列间距
            setColumnSpace(bundle.getInt(KEY_COLUMN_SPACE_STATE, columnSpace));
            //恢复行间隔
            setRowSpace(bundle.getInt(KEY_ROW_SPACE_STATE, rowSpace));

            //恢复item的选择类型
            setSelectType(SelectType.get(bundle.getInt(KEY_SELECT_TYPE_STATE, selectType.value)));
            //恢复item的最大选择数量
            setMaxSelect(bundle.getInt(KEY_MAX_SELECT_STATE, maxSelect));
            //恢复item的最大行数
            setMaxLines(bundle.getInt(KEY_MAX_LINES_STATE, maxLines));

            //恢复必选项列表
            ArrayList<Integer> keepList = bundle.getIntegerArrayList(KEY_KEEP_LIST_STATE);
            if (keepList != null && !keepList.isEmpty()) {
                setKeepList(keepList);
            }

            //恢复已选择的item列表
            ArrayList<Integer> selectList = bundle.getIntegerArrayList(KEY_SELECT_LIST_STATE);
            if (selectList != null && !selectList.isEmpty()) {
                int size = selectList.size();
                int[] positions = new int[size];
                for (int i = 0; i < size; i++) {
                    positions[i] = selectList.get(i);
                }
                setSelects(positions);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 设置新数据, 由TextProvider负责展示
     *
     * @see TextProvider
     */
    public <T> void setNewData(List<T> data, TextProvider<T> provider) {
        initAllSelect();
        removeAllViews();
        dataList.clear();

        if (data != null) {
            dataList.addAll(data);
            int size = data.size();
            for (int i = 0; i < size; i++) {
                addItem(data.get(i), i, provider);
            }
            refreshItemClickable();
        }

        if (selectType == SelectType.SINGLE_NOT_RESELECT) {
            setSelects(defaultSelect);
        }
    }

    /**
     * 初始化item状态
     */
    private void initAllSelect() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            selectChange((TextView) getChildAt(i), false);
        }
        selectList.clear();
    }

    private <T> void addItem(T data, int position, TextProvider<T> provider) {
        final TextView textView = new TextView(context);
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
        textView.setBackgroundDrawable(bgDrawable.getConstantState().newDrawable());
        //关联数据
        textView.setTag(KEY_DATA, data);
        textView.setTag(KEY_POSITION, position);
        textView.setOnClickListener(this);
        addView(textView);
        textView.setText(provider.getLabelText(textView, position, data));
    }

    /**
     * 刷新item的响应事件（item可选或者item设置了点击事件监听，则响应事件）
     */
    private void refreshItemClickable() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setClickable(onItemClickListener != null || selectType != SelectType.NONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            if (selectType != SelectType.NONE) {
                if (textView.isSelected()) {
                    if (selectType != SelectType.SINGLE_NOT_RESELECT && !keepList.contains(textView.getTag(KEY_POSITION))) {
                        selectChange(textView, false);
                    }
                } else if (selectType == SelectType.SINGLE || selectType == SelectType.SINGLE_NOT_RESELECT) {
                    initAllSelect();
                    selectChange(textView, true);
                } else if (selectType == SelectType.MULTI && (maxSelect <= 0 || maxSelect > selectList.size())) {
                    selectChange(textView, true);
                }
            }

            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(textView, textView.getTag(KEY_DATA), (int) textView.getTag(KEY_POSITION));
            }
        }
    }

    private void selectChange(TextView textView, boolean isSelect) {
        if (textView.isSelected() != isSelect) {
            textView.setSelected(isSelect);
            if (isSelect) {
                selectList.add((Integer) textView.getTag(KEY_POSITION));
            } else {
                selectList.remove(textView.getTag(KEY_POSITION));
            }
            if (onSelectChangeListener != null) {
                onSelectChangeListener.onSelectChange(textView, textView.getTag(KEY_DATA),
                        isSelect, (int) textView.getTag(KEY_POSITION));
            }
        }
    }

    /**
     * 取消所有选中的item
     */
    public void clearAllSelect() {
        if (selectType != SelectType.SINGLE_NOT_RESELECT) {
            if (selectType == SelectType.MULTI && !keepList.isEmpty()) {
                clearNotCompulsorySelect();
            } else {
                initAllSelect();
            }
        }
    }

    private void clearNotCompulsorySelect() {
        int count = getChildCount();
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!keepList.contains(i)) {
                selectChange((TextView) getChildAt(i), false);
                temps.add(i);
            }

        }
        selectList.removeAll(temps);
    }

    /**
     * 设置选中item
     */
    public void setSelects(List<Integer> positionList) {
        if (positionList != null) {
            int size = positionList.size();
            int[] ps = new int[size];
            for (int i = 0; i < size; i++) {
                ps[i] = positionList.get(i);
            }
            setSelects(ps);
        }
    }

    /**
     * 设置选中item
     */
    public void setSelects(int... positions) {
        if (selectType != SelectType.NONE) {
            ArrayList<TextView> selectLabels = new ArrayList<>();
            int count = getChildCount();
            int size = selectType == SelectType.SINGLE || selectType == SelectType.SINGLE_NOT_RESELECT ? 1 : maxSelect;
            for (int position : positions) {
                if (position < count) {
                    TextView textView = (TextView) getChildAt(position);
                    if (!selectLabels.contains(textView)) {
                        selectChange(textView, true);
                        selectLabels.add(textView);
                    }
                    if (size > 0 && selectLabels.size() == size) {
                        break;
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                TextView textView = (TextView) getChildAt(i);
                if (!selectLabels.contains(textView)) {
                    selectChange(textView, false);
                }
            }
        }
    }

    /**
     * 设置必选项(只有在多项模式下，这个方法才有效)
     */
    public void setKeepList(List<Integer> positions) {
        if (selectType == SelectType.MULTI && positions != null) {
            keepList.clear();
            keepList.addAll(positions);
            //必选项发生改变，就要恢复到初始状态。
            initAllSelect();
            setSelects(positions);
        }
    }

    /**
     * 设置必选项（只有在多项模式下，这个方法才有效）
     */
    public void setKeepList(int... positions) {
        if (selectType == SelectType.MULTI && positions != null) {
            List<Integer> ps = new ArrayList<>(positions.length);
            for (int i : positions) {
                ps.add(i);
            }
            setKeepList(ps);
        }
    }

    /**
     * 获取必选项，
     */
    public List<Integer> getKeepList() {
        return keepList;
    }

    /**
     * 清空必选项（只有在多项模式下，这个方法才有效）
     */
    public void clearKeepList() {
        if (selectType == SelectType.MULTI && !keepList.isEmpty()) {
            keepList.clear();
            initAllSelect();
        }
    }

    /**
     * 获取所有选中item的position
     */
    public List<Integer> getSelectPositionList() {
        return selectList;
    }

    /**
     * 获取所有选中的item
     */
    public <T> List<T> getSelectList() {
        List<T> list = new ArrayList<>();
        int size = selectList.size();
        for (int i = 0; i < size; i++) {
            View label = getChildAt(selectList.get(i));
            Object data = label.getTag(KEY_DATA);
            if (data != null) {
                list.add((T) data);
            }
        }
        return list;
    }

    /**
     * 设置item背景
     */
    public void setItemBackgroundResource(int resId) {
        setItemBackgroundDrawable(getResources().getDrawable(resId));
    }

    /**
     * 设置item背景
     */
    public void setItemBackgroundColor(int color) {
        setItemBackgroundDrawable(new ColorDrawable(color));
    }

    /**
     * 设置item背景
     */
    public void setItemBackgroundDrawable(Drawable drawable) {
        bgDrawable = drawable;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            // 背景(Drawable)是一个Drawable对象的拷贝，如果所有的item都共用一个Drawable对象，会引起背景错乱。
            getChildAt(i).setBackgroundDrawable(bgDrawable.getConstantState().newDrawable());
        }
    }

    /**
     * 设置item内边距
     */
    public void setTextPadding(int left, int top, int right, int bottom) {
        if (paddingLeft != left || paddingTop != top || paddingRight != right || paddingBottom != bottom) {
            paddingLeft = left;
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setPadding(left, top, right, bottom);
            }
        }
    }

    /**
     * 设置item的文字大小（单位是px）
     */
    public void setTextSize(float size) {
        if (textSize != size) {
            textSize = size;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView textView = (TextView) getChildAt(i);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        }
    }

    /**
     * 设置item的文字颜色
     */
    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置item的文字颜色
     */
    public void setTextColor(ColorStateList color) {
        textColor = color;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TextView textView = (TextView) getChildAt(i);
            textView.setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
        }
    }

    /**
     * 设置行间隔
     */
    public void setRowSpace(int margin) {
        if (rowSpace != margin) {
            rowSpace = margin;
            requestLayout();
        }
    }

    /**
     * 设置item的间隔
     */
    public void setColumnSpace(int margin) {
        if (columnSpace != margin) {
            columnSpace = margin;
            requestLayout();
        }
    }

    /**
     * 设置item的选择类型
     */
    public void setSelectType(SelectType selectType) {
        if (this.selectType != selectType) {
            this.selectType = selectType;

            initAllSelect();

            if (this.selectType == SelectType.SINGLE_NOT_RESELECT) {
                setSelects(defaultSelect);
            }

            if (this.selectType != SelectType.MULTI) {
                keepList.clear();
            }
            refreshItemClickable();
        }
    }

    public SelectType getSelectType() {
        return selectType;
    }

    /**
     * 设置最大的选择数量
     */
    public void setMaxSelect(int maxSelect) {
        if (this.maxSelect != maxSelect) {
            this.maxSelect = maxSelect;
            if (selectType == SelectType.MULTI) {
                initAllSelect();
            }
        }
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    /**
     * 设置最大行数
     */
    public void setMaxLines(int maxLines) {
        if (this.maxLines != maxLines) {
            this.maxLines = maxLines;
            requestLayout();
        }
    }

    public int getMaxLines() {
        return maxLines;
    }

    /**
     * 设置item的点击监听
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        refreshItemClickable();
    }

    /**
     * 设置item的选择监听
     */
    public void setOnSelectChangeListener(OnSelectChangeListener onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
    }

    /**
     * 设置达到最大行数的监听
     */
    public void setOnMoreThanMaxLineListener(OnMoreThanMaxLineListener onMoreThanMaxLineListener) {
        this.onMoreThanMaxLineListener = onMoreThanMaxLineListener;
    }

    /**
     * sp转px
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    public interface OnItemClickListener {

        /**
         * @param textView item
         * @param data     item对应的数据
         * @param position item位置
         */
        void onItemClick(TextView textView, Object data, int position);
    }

    public interface OnSelectChangeListener {

        /**
         * @param textView item
         * @param data     item对应的数据
         * @param isSelect 是否选中
         * @param position item的position
         */
        void onSelectChange(TextView textView, Object data, boolean isSelect, int position);
    }

    public interface OnMoreThanMaxLineListener {

        /**
         * @param contentHeight 内容高度
         */
        void onMoreThan(int contentHeight);
    }
}