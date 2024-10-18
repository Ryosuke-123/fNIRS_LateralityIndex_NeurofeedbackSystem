package com.example.neurofeedbackApp1;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class NFMeasureDataLogger extends NFLogger
{
    final static String CommentOut            = "#";
    final static String Copyright             = "HOT2000 NF System" + "(C) Ryosuke Hiyama & Hiroki Sato. 2024. All rights reserved";
    final static String Version               = "Ver 1.00";
    final static String Header_BtAddress      = "BluetoothAddress";
    final static String Header_RestTime       = "RestTime";
    final static String Header_BaselineTime   = "BaselineTime";
    final static String Header_MovingAvgTime  = "MovingAvgTime";
    final static String Header_TrainingTime   = "TrainingTime";
    String BtAddress     = "";
    String RestTime      = "";
    String BaselineTime  = "";
    String MovingAvgTime = "";
    String TrainingTime  = "";

    /*-----計測データ一覧-----*/
    // 時刻:                       Device time
    // タイムスタンプ:               Headset time(sec)
    // 脳活動値(左)                 HbT change(left,MD-ICA)
    // 脳活動値(右)                 HbT change(right,MD-ICA)
    // 平均脳血流変化(左)             Average HbT change(left)
    // 平均脳血流変化(右)             Average HbT change(right)
    // 算出された側性化指標:           Calculate LI
    // 実際にFBされた側性化指標:       Feedback LI
    // 脳血流変化(左SD3cm):          HbT change(left SD3cm)
    // 脳血流変化(左SD1cm):          HbT change(left SD1cm)
    // 脳血流変化(右SD3cm):          HbT change(right SD3cm)
    // 脳血流変化(右SD1cm):          HbT change(right SD1cm)
    // 脈拍(左):                    Pulse Rate
    // 加速度(X軸):                 ACC-X
    // 加速度(Y軸):                 ACC-Y
    // 加速度(Z軸):                 ACC-Z
    // 角速度(X軸):                 Gyro-X
    // 角速度(Y軸):                 Gyro-Y
    // 角速度(Z軸):                 Gyro-Z
    // マーク:                      Mark

    final static String NFMeasurementItem =
            "Device time,"+
            "Headset time(sec),"+
            "HbT change(left),"+
            "HbT change(right),"+
            "Avg HbT change(left),"+
            "Avg HbT change(right),"+
            "Calculate LI,"+
            "Feedback LI,"+
            "HbT change(left SD3cm),"+
            "HbT change(left SD1cm),"+
            "HbT change(right SD3cm),"+
            "HbT change(right SD1cm),"+
            "Pulse Rate,"+
            "ACC-X,"+
            "ACC-Y,"+
            "ACC-Z,"+
            "Gyro-X,"+
            "Gyro-Y,"+
            "Gyro-Z,"+
            "Mark";

    final static String Suffix = NFSystemDefine.MEASURE_RESULT_FILENAME_SUFFIX;

    StringBuilder                mStringBuilder;
    LinkedHashMap<String,String> headerMap;
    NFMeasureDataManager         mNFMeasureDataManager;
    SimpleDateFormat             mDateF;
    Date                         mDate;

    // コンストラクタ -> インスタンスを生成したときに一番最初に行われる処理
    public NFMeasureDataLogger(NFMeasureDataManager nfMeasureDataManager)
    {
        mNFMeasureDataManager = nfMeasureDataManager;

        headerMap = new LinkedHashMap<>();
        headerMap.put("copyright",Copyright);
        headerMap.put("header",Version);

        mDateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    }

    public boolean open(String fileName,String folderName,String address,String restTime,String baselineTime,String movingAvgTime,String trainingTime)
    {
        if (address != null)
        {
            BtAddress = address;
        }

        RestTime      = restTime;
        BaselineTime  = baselineTime;
        MovingAvgTime = movingAvgTime;
        TrainingTime  = trainingTime;

        boolean result = super.open(fileName,folderName,false); // エラー?(2022/09/09)

        if (result)
        {
            this.mStringBuilder = new StringBuilder();
        }

        return result;
    }

    @Override
    public void writeHeader()
    {
        for (String header:headerMap.values())
        {
            this.flushLine(CommentOut + header);
        }

        this.flushLine(CommentOut);

        this.flushLine(CommentOut + Header_BtAddress + BtAddress);
        this.flushLine(CommentOut + Header_RestTime + RestTime);
        this.flushLine(CommentOut + Header_BaselineTime + BaselineTime);
        this.flushLine(CommentOut + Header_MovingAvgTime + MovingAvgTime);
        this.flushLine(CommentOut + Header_TrainingTime + TrainingTime);

        this.flushLine(CommentOut);

        this.flushLine(NFMeasurementItem);
    }

    @Override
    public void setSuffix()
    {
        this.suffix = Suffix;
    }

    @Override
    public void write()
    {

    }

    public boolean write(int count, ArrayList<Double> brainDoubleArray, ArrayList<Double> heartDoubleArray, ArrayList<Double> motionDoubleArray, ArrayList<Short> shortArray)
    {
        try
        {
            mDate = new Date(System.currentTimeMillis());

            this.mStringBuilder.setLength(0);

            this.mStringBuilder.append(mDateF.format(mDate));
            this.mStringBuilder.append(",");

            this.mStringBuilder.append(count/10.f);
            this.mStringBuilder.append(",");

            // 脳活動データ
            this.mStringBuilder.append(TextUtils.join(",",brainDoubleArray.toArray()));
            this.mStringBuilder.append(",");

            // 心拍データ
            this.mStringBuilder.append(TextUtils.join(",",heartDoubleArray.toArray()));
            this.mStringBuilder.append(",");

            // 加速度、角速度データ
            this.mStringBuilder.append(TextUtils.join(",",motionDoubleArray.toArray()));
            this.mStringBuilder.append(",");

            // その他
            this.mStringBuilder.append(TextUtils.join(",",shortArray.toArray()));
            this.mStringBuilder.append(",");


            this.flushLine(this.mStringBuilder.toString());

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
