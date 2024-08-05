package com.example.dartsApp1;

import android.util.Log;

import com.neu.exbrainsdk.component.CommonFunctions;

import java.util.ArrayList;

public class NFMeasureDataManager
{
    boolean             mMeasureLogEnabled = false;
    String              mFolderName;

    NFMeasureDataLogger mNFMeasureDataLogger = null;
    String              mAddress;

    // コンストラクタ
    public void NFMeasureDataLogger()
    {
        mNFMeasureDataLogger = new NFMeasureDataLogger(this);
    }

    // NFシステム 計測データ ストア開始
    public boolean startStoreNFMeasureData(boolean should_output_log, String folderName, String address, String preRestTime, String focusTime, String postRestTime, String trialNumber)
    {
        mMeasureLogEnabled = should_output_log;
        mFolderName        = folderName;
        mAddress           = address;

        if (mMeasureLogEnabled)
        {
            mNFMeasureDataLogger = new NFMeasureDataLogger(this);
        }
        else
        {
            return false;
        }

        // 許可されている場合、ロギング開始
        if (mMeasureLogEnabled)
        {
            String fileName = NFSystemDefine.MEASURE_RESULT_FILENAME_PREFIX + CommonFunctions.getNowDate("_yyyMMdd_HHmmss");
            boolean result  = mNFMeasureDataLogger.open(fileName,folderName,mAddress,preRestTime,focusTime,postRestTime,trialNumber);
            Log.d("Result", String.valueOf(result));
        }
        else
        {
            return false;
        }

        return true;
    }

    // NFシステム 計測データ エントリー開始
    public boolean entryStoredMeasureData(int serialCounter,ArrayList<Double> brainDoubleData, ArrayList<Double> heartDoubleData, ArrayList<Double> motionDoubleData, ArrayList<Short> shortData)
    {
        // 計測データ書き込み開始
        if (mMeasureLogEnabled)
        {
            boolean LogEntryResult = mNFMeasureDataLogger.write(serialCounter,brainDoubleData,heartDoubleData,motionDoubleData,shortData);

            if (LogEntryResult)
            {
                Log.d("LogEntryResult","Success");
                return true;
            }
            else
            {
                Log.d("LogEntryResult","False");
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}
