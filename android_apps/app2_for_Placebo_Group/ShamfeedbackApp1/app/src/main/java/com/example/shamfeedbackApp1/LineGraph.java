package com.example.shamfeedbackApp1;

// 線グラフを表示させるためのクラス

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class LineGraph
{
    //　メンバ変数
    LineChart mLineChart;
    String labels1 = "LI (R-L/|R|+|L|)";
    String[] labels2 = new String[]{
            "Left Brain[mMmm]",
            "Right Brain[mMmm]"
    };
    String[] labels3 = new String[]{
            "Brain Index L",
            "Brain Index R",
            "LI (L-R/L+R)"
    };
    String[] leftLabel = new String[]{
            "left Brain",
            "left 3cm Brain",
            "left 1cm Brain"
    };
    String[] rightLabel = new String[]{
            "right Brain",
            "right 3cm Brain",
            "right 1cm Brain"
    };
    String[] liLabel = new String[]{
            "LI Brain",
            "left Brain",
            "right Brain"
    };
    int colors1 = Color.BLACK;
    int[] colors2 = new int[]{
            Color.GREEN,
            Color.BLUE
    };
    int[] colors3 = new int[]{
            Color.BLACK,
            Color.GREEN,
            Color.BLUE
    };

    // 引数なしのコンストラクタ(インスタンスが生成される際に実行されるメソッド)
    public LineGraph()
    {

    }

    // 初期設定用メソッド
    public void initSetting(LineChart lineChart)
    {
        mLineChart = lineChart;

        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);

        // Grid背景色
        mLineChart.setDrawGridBackground(true);

        // no description text
        mLineChart.getDescription().setEnabled(true);

        mLineChart.setBackgroundColor(Color.LTGRAY);

        mLineChart.setData(new LineData());

        // Grid縦軸を破線
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawLabels(false); // x軸ラベルを非表示にする
        xAxis.enableGridDashedLine(10f,10f,0f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);

        // Y軸の設定
        YAxis leftAxis = mLineChart.getAxisLeft();
        // Grid横軸を破線
        leftAxis.enableGridDashedLine(10f,10f,0f);
        leftAxis.setDrawZeroLine(true);
        // 右側の目盛り
        mLineChart.getAxisRight().setEnabled(false);
    }

    /**--------------- グラフ表示用メソッド -------------**/
    public void display(float data1)
    {
        float getData = data1;

        // グラフ描画
        LineData mLineData = mLineChart.getLineData();
        if (mLineData != null)
        {
            ILineDataSet mILineDataSet = mLineData.getDataSetByIndex(0);
            if (mILineDataSet == null)
            {
                LineDataSet mLineDataSet = new LineDataSet(null, labels1);
                mLineDataSet.setColor(colors1);
                mLineDataSet.setLineWidth(2.0f);

                mLineDataSet.setDrawCircles(false);
                mLineDataSet.setDrawValues(false);
                mILineDataSet = mLineDataSet;
                mLineData.addDataSet(mILineDataSet);
            }

            // 追加描画するデータ

            mLineData.addEntry(new Entry(mILineDataSet.getEntryCount(),getData),0);
            mLineData.notifyDataChanged();

            mLineChart.notifyDataSetChanged();                 // 表示の更新のために変更を通知する
            mLineChart.setVisibleXRangeMaximum(300);           // 表示の幅を決定する
            mLineChart.moveViewToX(mLineData.getEntryCount() - 301); // 最新のデータまで表示を移動する
        }
    }

    public void display(float data1, float data2)
    {
        float[] getData = {data1, data2};

        // グラフ描画
        LineData mLineData = mLineChart.getLineData();
        if (mLineData != null)
        {
            for (int i=0;i<2;i++)
            {
                ILineDataSet mILineDataSet = mLineData.getDataSetByIndex(i);
                if (mILineDataSet == null)
                {
                    LineDataSet mLineDataSet = new LineDataSet(null, labels2[i]);
                    mLineDataSet.setColor(colors2[i]);
                    mLineDataSet.setLineWidth(2.0f);

                    mLineDataSet.setDrawCircles(false);
                    mLineDataSet.setDrawValues(false);
                    mILineDataSet = mLineDataSet;
                    mLineData.addDataSet(mILineDataSet);
                }

                // 追加描画するデータ
                mLineData.addEntry(new Entry(mILineDataSet.getEntryCount(),getData[i]),i);
                mLineData.notifyDataChanged();
            }
            mLineChart.notifyDataSetChanged();                 // 表示の更新のために変更を通知する
            mLineChart.setVisibleXRangeMaximum(300);           // 表示の幅を決定する
            mLineChart.moveViewToX(mLineData.getEntryCount() - 301); // 最新のデータまで表示を移動する
        }
    }
    public void display(float data1, float data2, float data3)
    {
        float[] getData = {data1, data2, data3};

        // グラフ描画
        LineData mLineData = mLineChart.getData(); // getLineData -> getData()
        if (mLineData != null)
        {
            for (int i=0;i<3;i++)
            {
                ILineDataSet mILineDataSet = mLineData.getDataSetByIndex(i);
                if (mILineDataSet == null)
                {
                    LineDataSet mLineDataSet = new LineDataSet(null, labels3[i]);
                    mLineDataSet.setColor(colors3[i]);       // 線の色
                    // mLineDataSet.setCircleColor(colors3[i]); // 座標の色
                    mLineDataSet.setLineWidth(1.0f);         // 線の太さ
                    // mLineDataSet.setCircleRadius(2.0f);      // 座標の大きさ

                    mLineDataSet.setDrawCircles(false);
                    mLineDataSet.setDrawValues(false);
                    mILineDataSet = mLineDataSet;
                    mLineData.addDataSet(mILineDataSet);
                }

                // 追加描画するデータ
                mLineData.addEntry(new Entry(mILineDataSet.getEntryCount(),getData[i]),i);
                mLineData.notifyDataChanged();


            }
            mLineChart.notifyDataSetChanged();                 // 表示の更新のために変更を通知する
            mLineChart.setVisibleXRangeMaximum(300);           // 表示の幅を決定する
            mLineChart.moveViewToX(mLineData.getEntryCount()); // 最新のデータまで表示を移動する
        }
    }

    public void display(float data1, float data2, float data3, int mode)
    {
        float[] getData = {data1,data2,data3};

        // グラフ描画
        LineData mLineData = mLineChart.getLineData();
        if (mLineData != null)
        {
            for (int i=0;i<3;i++)
            {
                ILineDataSet mILineDataSet = mLineData.getDataSetByIndex(i);
                if (mILineDataSet == null)
                {
                    if (mode == 1)
                    {
                        LineDataSet mLineDataSet = new LineDataSet(null,leftLabel[i]);
                        mLineDataSet.setLineWidth(2.0f);
                        mLineDataSet.setColor(colors3[i]);
                        mLineDataSet.setDrawCircles(false);
                        mLineDataSet.setDrawValues(false);
                        mILineDataSet = mLineDataSet;
                        mLineData.addDataSet(mILineDataSet);
                    }
                    else if (mode == 2)
                    {
                        LineDataSet mLineDataSet = new LineDataSet(null,rightLabel[i]);
                        mLineDataSet.setLineWidth(2.0f);
                        mLineDataSet.setColor(colors3[i]);
                        mLineDataSet.setDrawCircles(false);
                        mLineDataSet.setDrawValues(false);
                        mILineDataSet = mLineDataSet;
                        mLineData.addDataSet(mILineDataSet);
                    }
                    else if (mode == 3)
                    {
                        LineDataSet mLineDataSet = new LineDataSet(null,liLabel[i]);
                        mLineDataSet.setLineWidth(2.0f);
                        mLineDataSet.setColor(colors3[i]);
                        mLineDataSet.setDrawCircles(false);
                        mLineDataSet.setDrawValues(false);
                        mILineDataSet = mLineDataSet;
                        mLineData.addDataSet(mILineDataSet);
                    }

                }

                // 追加描画するデータ
                mLineData.addEntry(new Entry(mILineDataSet.getEntryCount(),getData[i]),i);
                mLineData.notifyDataChanged();
            }
            mLineChart.notifyDataSetChanged();                 // 表示の更新のために変更を通知する
            mLineChart.setVisibleXRangeMaximum(10);           // 表示の幅を決定する
            mLineChart.moveViewToX(mLineData.getEntryCount()); // 最新のデータまで表示を移動する
        }
    }
}
