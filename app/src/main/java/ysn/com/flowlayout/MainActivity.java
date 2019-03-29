package ysn.com.flowlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import ysn.com.library.FlowLayout;
import ysn.com.library.SelectType;
import ysn.com.library.TextProvider;

/**
 * @Author yangsanning
 * @ClassName MainActivity
 * @Description 一句话概括作用
 * @Date 2019/3/27
 * @History 2019/3/27 author: description:
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "test";
    private FlowLayout dataFlowLayout;
    private Button moreButton, maxLineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBtn();
        initBtnFlowLayout();
        initDataFlowLayout();
    }

    private void initBtn() {
        moreButton = findViewById(R.id.main_activity_more);
        maxLineButton = findViewById(R.id.main_activity_max_line);

        moreButton.setOnClickListener(this);
        maxLineButton.setOnClickListener(this);
        findViewById(R.id.main_activity_clear_select).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_activity_max_line:
                dataFlowLayout.setMaxLines(2);
                if (maxLineButton.getTag() == null || (Boolean) maxLineButton.getTag()) {
                    maxLineButton.setTag(false);
                    maxLineButton.setText("取消最大行数");
                    dataFlowLayout.setMaxLines(2);
                } else {
                    maxLineButton.setTag(true);
                    maxLineButton.setText("设置最大行数");
                    dataFlowLayout.setMaxLines(0);

                    // 隐藏展开
                    moreButton.setVisibility(View.GONE);
                    moreButton.setTag(true);
                    moreButton.setText("展开");
                }
                break;
            case R.id.main_activity_more:
                if (moreButton.getTag() == null || (Boolean) moreButton.getTag()) {
                    moreButton.setTag(false);
                    moreButton.setText("收起");
                    dataFlowLayout.setMaxLines(0);
                } else {
                    moreButton.setTag(true);
                    moreButton.setText("展开");
                    dataFlowLayout.setMaxLines(2);
                }
                break;
            case R.id.main_activity_clear_select:
                dataFlowLayout.clearAllSelect();
                break;
            default:
                break;
        }
    }

    private void initBtnFlowLayout() {
        FlowLayout btnFlowLayout = findViewById(R.id.main_activity_btn_flow_layout);
        btnFlowLayout.setNewData(Arrays.asList(getResources().getStringArray(R.array.btn)), new TextProvider<String>() {
            @Override
            public CharSequence getLabelText(TextView textView, int position, String data) {
                return data;
            }
        });
        btnFlowLayout.setOnSelectChangeListener(new FlowLayout.OnSelectChangeListener() {
            @Override
            public void onSelectChange(TextView textView, Object data, boolean isSelect, int position) {
                switch (position) {
                    case 0:
                        dataFlowLayout.setSelectType(SelectType.NONE);
                        break;
                    case 1:
                        dataFlowLayout.setSelectType(SelectType.NONE);
                        dataFlowLayout.setOnItemClickListener(new FlowLayout.OnItemClickListener() {
                            @Override
                            public void onItemClick(TextView textView, Object data, int position) {
                                Toast.makeText((MainActivity.this), (position + " : " + ((Data) data).getName()), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 2:
                        dataFlowLayout.setSelectType(SelectType.SINGLE);
                        break;
                    case 3:
                        dataFlowLayout.setSelectType(SelectType.SINGLE_NOT_RESELECT);
                        break;
                    case 4:
                        dataFlowLayout.setSelectType(SelectType.MULTI);
                        dataFlowLayout.clearKeepList();
                        dataFlowLayout.setMaxSelect(-1);
                        break;
                    case 5:
                        dataFlowLayout.setSelectType(SelectType.MULTI);
                        dataFlowLayout.setMaxSelect(3);
                        break;
                    case 6:
                        dataFlowLayout.setSelectType(SelectType.MULTI);
                        dataFlowLayout.setMaxSelect(0);
                        dataFlowLayout.setKeepList(0, 1);
                        break;
                    case 7:
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initDataFlowLayout() {
        dataFlowLayout = findViewById(R.id.main_activity_data_flow_layout);
        dataFlowLayout.setOnMoreThanMaxLineListener(new FlowLayout.OnMoreThanMaxLineListener() {
            @Override
            public void onMoreThan(int contentHeight) {
                moreButton.setVisibility(View.VISIBLE);
                Log.d(TAG, "contentHeight: " + contentHeight);
            }
        });
        dataFlowLayout.setNewData(getData(), new TextProvider<Data>() {
            @Override
            public CharSequence getLabelText(TextView textView, int position, Data data) {
                return data.getName();
            }
        });
    }

    private ArrayList<Data> getData() {
        ArrayList<Data> dataList = new ArrayList<>();
        String[] array = getResources().getStringArray(R.array.data);
        for (int i = 0; i < array.length; i++) {
            dataList.add(new Data(array[i], i));
        }
        return dataList;
    }
}