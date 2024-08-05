package com.example.dartsApp1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.neu.exbrainsdk.ExBrainApi;
import com.neu.exbrainsdk.ExBrainEnumerations.*;
import com.neu.exbrainsdk.ExBrainException;
import com.neu.exbrainsdk.callbacks.IExBrainSdkCallbacks;
import com.neu.exbrainsdk.datas.DeviceInformationEx;
import com.neu.exbrainsdk.datas.DeviceMeasureData;
import com.neu.exbrainsdk.datas.ExBrainDiscoveredDevice;
import com.neu.exbrainsdk.datas.ExBrainMeasureData;
import com.neu.exbrainsdk.datas.ExBrainSearchParameter;
import com.neu.exbrainsdk.interfaces.bluetooth.DriverBluetoothDevice;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class StartExp extends AppCompatActivity implements View.OnClickListener
{
    BluetoothAdapter     mBluetoothAdapter;
    UsbManager           mUsbManager;
    ExBrainApi           mExBrainApi;
    String               mBtScanSearchNames = "HOT,XB";
    DeviceInformationEx  mDeviceInfo;
    String               mConnectedHandleID;

    LineGraph            mLineGraph;

    LineChart            mChart1;

    boolean              mLoggingTrigger = false;

    String               mBtAddress            = "";

    String               mPreRestTime          = "";
    String               mFocusTime            = "";
    String               mPostRestTime         = "";
    String               mTrialNumber          = "";

    double               mSamplingTime         = 0.1; // サンプリング時間[s]
    int                  mPreRestDataNumber    = 0;
    int                  mFocusDataNumber      = 0;
    int                  mPostRestDataNumber   = 0;
    int                  mTrialDataNumber      = 0;

    int                  mPreRestNumberCount   = 0;
    int                  mFocusNumberCount     = 0;
    int                  mPostRestNumberCount  = 0;
    int                  mTrialNumberCount     = 0;

    NFMeasureDataManager mNFMeasureDataManager;
    int                  mResultCounter       = 0;   // サンプリングカウンタ

    double               left3cmBaseline      = 0.0; // 左3cmベースライン脳血流
    double               left1cmBaseline      = 0.0; // 左1cmベースライン脳血流
    double               right3cmBaseline     = 0.0; // 右3cmベースライン脳血流
    double               right1cmBaseline     = 0.0; // 右1cmベースライン脳血流

    double               rawLeftBrain             = 0.0; // 脳血流変化の生データ(左)
    double               rawRightBrain            = 0.0; // 脳血流変化の生データ(右)
    double               leftBrainIndex           = 0.0; // MD-ICAによって算出された脳活動値(左)
    double               rightBrainIndex          = 0.0; // MD-ICAによって算出された脳活動値(右)
    double               left3cmBrain             = 0.0; // 左3cm脳血流変化
    double               left1cmBrain             = 0.0; // 左1cn脳血流変化
    double               right3cmBrain            = 0.0; // 右3cm脳血流変化
    double               right1cmBrain            = 0.0; // 右1cm脳血流変化
    double               pulseRate                = 0.0; // 脈拍
    double               accX                     = 0.0; // X軸加速度
    double               accY                     = 0.0; // Y軸加速度
    double               accZ                     = 0.0; // Z軸加速度
    double               gyroX                    = 0.0; // X軸角速度
    double               gyroY                    = 0.0; // Y軸角速度
    double               gyroZ                    = 0.0; // Z軸角速度
    short                mark                     = 0;   // マーク

    double               batteryHot2000    = 0.0; // HOT2000電池残量[%]

    int                  mExpModeFlag      = 1;
    int                  mExpMode          = 0;   // 実験モード(1: PreScan1, 2: PreScan2, 3: Training)

    // 聴覚NF用クラス
    NFVolume             mNFVolume;

    // MediaPlayerの設定
    MediaPlayer          mMediaPlayer1;
    MediaPlayer          mMediaPlayer2;
    MediaPlayer          mMediaPlayer3;

    // AudioManagerの設定
    AudioManager mAudioManager;

    String[] descriptionData = {"検索","接続","ゲイン調整","完了"};
    StateProgressBar mStateProgressBar;

    // ノイズ検出クラス
    DetectNoise mDetectNoise;
    int         mMotionArtifactState = 0;

    TextView mLeftBrainValue;
    TextView mRightBrainValue;
    TextView mLeftMotionArtifact;
    TextView mRightMotionArtifact;
    TextView mLIBrainValue;
    TextView mTrialValue;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_exp);

        mStateProgressBar = findViewById(R.id.device_setting_progress);
        mStateProgressBar.setStateDescriptionData(descriptionData);
        mStateProgressBar.checkStateCompleted(true);

        // タイトルバーの削除
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
        {
            mActionBar.hide();
        }

        // ボタンの設定
        findViewById(R.id.startSetting).setOnClickListener(this);
        findViewById(R.id.measureStart).setOnClickListener(this);
        findViewById(R.id.measureStop).setOnClickListener(this);

        // テキストの設定
        mLeftBrainValue      = findViewById(R.id.leftBrainValue);
        mRightBrainValue     = findViewById(R.id.rightBrainValue);
        mLeftMotionArtifact  = findViewById(R.id.leftMotionArtifact);
        mRightMotionArtifact = findViewById(R.id.rightMotionArtifact);
        mLIBrainValue        = findViewById(R.id.liBrainValue);
        mTrialValue          = findViewById(R.id.trialValue);


        // tabHostの初期化および設定処理
        initTabs();

        // Bluetoothサービス有無確認
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            finish();
        }

        // 外部ストレージ設定(ログファイル保存用)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // [Bluetoothアダプタ生成＆確認]
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            finish();
        }

        // [USBサービス生成]
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // ドライバAPI生成/コールバック登録
        mExBrainApi = ExBrainApi.instance();

        // BluetoothとUSBサービスおよび通信ログ出力ありとしてドライバAPIを初期化 デバイス1用
        try
        {
            EnumExBrainResult result = mExBrainApi.initApi(mBluetoothManager, mUsbManager, false, false, "ExBrainSdk1", new IExBrainSdkCallbacks() {
                @Override
                public void onNotifyStatusChanged(String s, EnumExBrainStatus enumExBrainStatus)
                {

                }

                @Override
                public void onNotifyActionEvents(String s, EnumExBrainEvents enumExBrainEvents)
                {

                }

                @Override
                public void onNotifyMeasureData(String s, ExBrainMeasureData exBrainMeasureData)
                {

                }

                @Override
                public void onNotifyMeasureRawData(String s, DeviceMeasureData deviceMeasureData)
                {
                    // HOT2000バッテリー残量確認
                    batteryHot2000 = deviceMeasureData.batteryGauge;
                    checkHOT2000Battery(batteryHot2000);

                    Log.d("Battery", String.valueOf(deviceMeasureData.batteryGauge));

                    // 計測データ ロギング
                    if (mLoggingTrigger)
                    {
                        loggingData(deviceMeasureData);
                    }

                    // グラフ描画
                    mLineGraph.display((float) rawLeftBrain, (float) rawRightBrain);

                    // テキスト
                    mLeftBrainValue.setText(String.format("%.2f",rawLeftBrain));
                    mRightBrainValue.setText(String.format("%.2f",rawRightBrain));

                    // テキスト表示
                    if (mMotionArtifactState == 1)
                    {
                        mLeftMotionArtifact.setText("異常");
                        mRightMotionArtifact.setText("正常");
                    }
                    else if (mMotionArtifactState == 2)
                    {
                        mLeftMotionArtifact.setText("正常");
                        mRightMotionArtifact.setText("異常");
                    }
                    else if (mMotionArtifactState == 3)
                    {
                        mLeftMotionArtifact.setText("異常");
                        mRightMotionArtifact.setText("異常");
                    }
                    else
                    {
                        mLeftMotionArtifact.setText("正常");
                        mRightMotionArtifact.setText("正常 ");
                    }
                }

                @Override
                public void onNotifySearchResult(EnumExBrainSearchEvent enumExBrainSearchEvent, DeviceInformationEx deviceInformationEx)
                {

                }

                @Override
                public void onUsbPermissionRequest(DeviceInformationEx deviceInformationEx)
                {

                }
            });

            if( result == EnumExBrainResult.eSuccess )
            {
                Log.d("Product Name", mExBrainApi.getProductName());
                Log.d("Product Version", mExBrainApi.getProductVersion());
            }

            // インテントフィルターとBroadcastReceiverの登録
            registerReceiver( mExBrainApi.getBluetoothReceiver(), mExBrainApi.getBluetoothReceiverIntentFilter() ) ;
        }
        catch (ExBrainException ex)
        {
            ex.printStackTrace();
        }

        // グラフ描画準備
        mLineGraph = new LineGraph();
        mChart1    = findViewById(R.id.chart);
        mLineGraph.initSetting(mChart1);

        // 実験条件準備
        Intent expSettingIntent = getIntent();

        mPreRestTime  = expSettingIntent.getStringExtra("preRestTime");
        mFocusTime    = expSettingIntent.getStringExtra("focusTime");
        mPostRestTime = expSettingIntent.getStringExtra("postRestTime");
        mTrialNumber  = expSettingIntent.getStringExtra("trialNumber");

        /*-------------- 実験条件の反映 ------------*/
        mPreRestDataNumber  = (int)(Integer.parseInt(mPreRestTime) / mSamplingTime);
        mFocusDataNumber    = (int)(Integer.parseInt(mFocusTime) / mSamplingTime);
        mPostRestDataNumber = (int)(Integer.parseInt(mPostRestTime) / mSamplingTime);
        mTrialDataNumber    = (int)(Integer.parseInt(mTrialNumber));

        mNFVolume = new NFVolume();

        /*---------------- MediaPlayerの準備 ------------------*/
        mMediaPlayer1 = MediaPlayer.create(this,R.raw.beep1);
        mMediaPlayer2 = MediaPlayer.create(this,R.raw.beep2);
        mMediaPlayer3 = MediaPlayer.create(this,R.raw.beep3);

        /*-------------------- AudioManagerの準備 -------------------*/
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /*--------------------- ビープ音の準備 ---------------------*/

        // データロガー準備
        mNFMeasureDataManager = new NFMeasureDataManager();

        // ノイズ検出クラス準備
        mDetectNoise = new DetectNoise();
    }

    void initTabs()
    {
        try
        {
            TabHost tabHost = findViewById(R.id.tabHost);
            tabHost.setup();
            TabHost.TabSpec spec;

            // tab1
            spec = tabHost.newTabSpec("Tab1")
                    .setIndicator("デバイス", ContextCompat.getDrawable(this,R.drawable.background))
                    .setContent(R.id.prepare_layout);
            tabHost.addTab(spec);

            // tab2
            spec = tabHost.newTabSpec("Tab2")
                    .setIndicator("計測",ContextCompat.getDrawable(this,R.drawable.background))
                    .setContent(R.id.measure_layout);
            tabHost.addTab(spec);

            // tab3
            spec = tabHost.newTabSpec("Tab3")
                    .setIndicator("実験条件",ContextCompat.getDrawable(this,R.drawable.background))
                    .setContent(R.id.conditions_layout);
            tabHost.addTab(spec);

            tabHost.setCurrentTab(0);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
    }

    void startPairedBluetoothDeviceScan()
    {
        try
        {
            // [APIによる装置検索用のパラメータを作成]
            ExBrainSearchParameter search_option = new ExBrainSearchParameter();
            search_option.shouldNonPairedBluetooth = false;
            search_option.shouldPairedBluetooth = true;
            search_option.shouldUSB = false;
            search_option.timeoutSecond = 0;
            search_option.setSearchKeyword(mBtScanSearchNames);

            // [APIによる装置検索]
            ExBrainDiscoveredDevice discoveredDevice = new ExBrainDiscoveredDevice();
            EnumExBrainResult result =  mExBrainApi.searchDevice( search_option, discoveredDevice );

            if( result == EnumExBrainResult.eSuccess )
            {
                Log.d("Discover Device","Success");

                for( DeviceInformationEx device : discoveredDevice.devices )
                {
                    mDeviceInfo = device;
                }

                if (mStateProgressBar.getCurrentStateNumber() == 1)
                {
                    mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    connectDevice(mDeviceInfo);
                }
            }
            else
            {
                Log.d("Discover Device","False");
            }
        }
        catch (Exception ex)
        {
            Log.d("Discover Device","Error");
        }
    }

    void connectDevice(DeviceInformationEx deviceInfo)
    {
        String handleID                    = deviceInfo.deviceId;
        EnumExBrainConnectType connectType = deviceInfo.connectType;

        // アプリ上のサービス等を確認
        if (connectType == EnumExBrainConnectType.eBluetooth)
        {
            if (!mExBrainApi.isBtServiceConnected())
            {
                // Bluetoothサービス開始
                Intent gattServiceIntent = new Intent(this, DriverBluetoothDevice.class);
                bindService(gattServiceIntent, mExBrainApi.getServiceConnection(), BIND_AUTO_CREATE);
            }
        }
        else if (connectType == EnumExBrainConnectType.eUsb)
        {

        }

        // APIによる装置接続
        Executors.newCachedThreadPool().submit(()->{
            try
            {
                EnumExBrainResult result = mExBrainApi.connectDevice(handleID, connectType, false);

                if (result == EnumExBrainResult.eSuccess)
                {
                    mConnectedHandleID = mExBrainApi.getHandleId();

                    if (mStateProgressBar.getCurrentStateNumber() == 2)
                    {
                        mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                        startDeviceGainAdjust(mConnectedHandleID);
                    }
                }
                else
                {
                    mConnectedHandleID = "";
                }

            }
            catch (Exception ex)
            {

            }
        });
    }

    void startDeviceGainAdjust(String connectHandleID)
    {
        // APIによるゲイン調整開始
        Executors.newCachedThreadPool().submit(()->{
            try
            {
                EnumExBrainResult result = mExBrainApi.startGainAdjust(connectHandleID);
                if (result == EnumExBrainResult.eSuccess)
                {
                    if (mStateProgressBar.getCurrentStateNumber() == 3)
                    {
                        mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                    }
                }
                else
                {

                }
            }
            catch (Exception ex)
            {

            }
        });
    }

    void startDeviceMeasure(String connectHandleID)
    {
        // APIによる装置計測開始
        Executors.newCachedThreadPool().submit(()->{

            mBtAddress = connectHandleID;

            try
            {
                EnumExBrainResult result = mExBrainApi.startMeasure(connectHandleID, true);

                if (result == EnumExBrainResult.eSuccess)
                {
                    // NFシステム 計測データ ストア準備開始
                    boolean logStartingResult = mNFMeasureDataManager.startStoreNFMeasureData(true,"Darts_ExpLog",mBtAddress,mPreRestTime,mFocusTime,mPostRestTime,mTrialNumber);
                    if (logStartingResult)
                    {
                        Log.d("LogStartingResult","Success");
                    }
                    else
                    {
                        Log.d("LogStartingResult","False");
                    }
                }
                else
                {

                }
            }
            catch (Exception ex)
            {

            }
        });
    }

    // 装置の測定を停止する
    void stopDeviceMeasure()
    {
        // APIによる装置計測停止
        Executors.newCachedThreadPool().submit(()->{
            try
            {
                EnumExBrainResult result = mExBrainApi.stopMeasure(mConnectedHandleID);
                if (result == EnumExBrainResult.eSuccess)
                {
                    Log.d("Measure","Stop Measure");
                }
                else
                {

                }
            }
            catch (Exception ex)
            {

            }
        });
    }

    void loggingData(DeviceMeasureData measureData)
    {
        // 指定したトライアル数に達したら、計測停止
        if (mTrialNumberCount == mTrialDataNumber)
        {
            mTrialValue.setText("終了");
            stopDeviceMeasure();
        }
        else
        {
            mTrialValue.setText(String.format(String.valueOf(mTrialNumberCount + 1)));
        }

        // ロギング用データ準備
        ArrayList<Double> brainDoubleData  = new ArrayList<>();
        ArrayList<Double> heartDoubleData  = new ArrayList<>();
        ArrayList<Double> motionDoubleData = new ArrayList<>();
        ArrayList<Short>  shortData        = new ArrayList<>();

        // 計測データ
        left3cmBrain    = measureData.bloodHbDensities[1] - left3cmBaseline;
        left1cmBrain    = measureData.bloodHbDensities[0] - left1cmBaseline;
        right3cmBrain   = measureData.bloodHbDensities[3] - right3cmBaseline;
        right1cmBrain   = measureData.bloodHbDensities[2] - right1cmBaseline;
        leftBrainIndex  = measureData.bloodHbIndexValues[0];
        rightBrainIndex = measureData.bloodHbIndexValues[1];
        pulseRate       = measureData.heartRate;
        accX            = measureData.motionAccValues[0];
        accY            = measureData.motionAccValues[1];
        accZ            = measureData.motionAccValues[2];
        gyroX           = measureData.motionGyroValues[0];
        gyroY           = measureData.motionGyroValues[1];
        gyroZ           = measureData.motionGyroValues[2];

        // Hb濃度変化の算出(皮膚血流成分除去済み)
        rawLeftBrain  = left3cmBrain - left1cmBrain;
        rawRightBrain = right3cmBrain - right1cmBrain;

        // Expモード
        if (mExpModeFlag == 1)
        {
            mExpMode = 1;
            mark     = 1;
        }
        else if (mExpModeFlag == 2)
        {
            mExpMode = 2;
            mark     = 2;
        }
        else if (mExpModeFlag == 3)
        {
            mExpMode = 3;
            mark     = 3;
        }

        if (mExpMode == 1)
        {
            // PreRest期間開始の合図
            if (mPreRestNumberCount == 0)
            {
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer1);
            }

            if (mPreRestNumberCount == mPreRestDataNumber - 1)
            {
                // Focus期間移行への準備
                mExpModeFlag = 2;

                // カウントリセット
                mPreRestNumberCount = 0;
            }
            else
            {
                mPreRestNumberCount = mPreRestNumberCount + 1;
            }
        }
        else if (mExpMode == 2)
        {
            // Focus期間開始の合図
            if (mFocusNumberCount == 0)
            {
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer2);
            }

            if (mFocusNumberCount == mFocusDataNumber - 1)
            {
                // PostRest期間移行への準備
                mExpModeFlag = 3;

                // カウントリセット
                mFocusNumberCount = 0;
            }
            else
            {
                mFocusNumberCount = mFocusNumberCount + 1;
            }
        }
        else if (mExpMode == 3)
        {
            // PostRest期間開始の合図
            if (mPostRestNumberCount == 0)
            {
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer3);
            }

            if (mPostRestNumberCount == mPostRestDataNumber - 1)
            {
                // PreRest期間移行への準備
                mExpModeFlag = 1;

                mTrialNumberCount = mTrialNumberCount + 1;

                // カウントリセット
                mPostRestNumberCount = 0;
            }
            else
            {
                mPostRestNumberCount = mPostRestNumberCount + 1;
            }
        }

        /*--------------------Logging用データ---------------------*/
        brainDoubleData.add(rawLeftBrain);    // Hb濃度変化(左, 皮膚血流成分除去済み)
        brainDoubleData.add(rawRightBrain);   // Hb濃度変化(右, 皮膚血流成分除去済み)
        brainDoubleData.add(leftBrainIndex);  // 脳活動値(左, MD-ICA適用)
        brainDoubleData.add(rightBrainIndex); // 脳活動値(右, MD-ICA適用)
        brainDoubleData.add(left3cmBrain);    // Hb濃度変化(左, SD3cm)
        brainDoubleData.add(left1cmBrain);    // Hb濃度変化(左, SD1cm)
        brainDoubleData.add(right3cmBrain);   // Hb濃度変化(右, SD3cm)
        brainDoubleData.add(right1cmBrain);   // Hb濃度変化(右, SD1cm)
        heartDoubleData.add(pulseRate);
        motionDoubleData.add(accX);
        motionDoubleData.add(accY);
        motionDoubleData.add(accZ);
        motionDoubleData.add(gyroX);
        motionDoubleData.add(gyroY);
        motionDoubleData.add(gyroZ);
        shortData.add(mark);

        boolean LogStringResult = mNFMeasureDataManager.entryStoredMeasureData(mResultCounter,brainDoubleData,heartDoubleData,motionDoubleData,shortData);
        if (LogStringResult)
        {
            Log.d("LogStringResult","Success");
        }
        else
        {
            Log.d("LogStringResult","False");
        }

        // カウンター更新
        mResultCounter = mResultCounter + 1;
    }

    void checkHOT2000Battery(double battery)
    {
        ImageView batteryImage = findViewById(R.id.batteryImage);

        // HOT2000のバッテリー残量に応じてバッテリー画像を変更
        if (90 <= battery)
        {
            batteryImage.setImageResource(R.drawable.b100);
        }
        else if (65 <= battery && battery < 90)
        {
            batteryImage.setImageResource(R.drawable.b75);
        }
        else if (40 <= battery && battery < 65)
        {
            batteryImage.setImageResource(R.drawable.b50);
        }
        else if (15 <= battery && battery < 40)
        {
            batteryImage.setImageResource(R.drawable.b25);
        }
        else if (0 <= battery && battery < 15)
        {
            batteryImage.setImageResource(R.drawable.b0);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.startSetting)
        {
            // 装置検索 → 装置接続 → ゲイン調整まで一気に行う
            startPairedBluetoothDeviceScan();
        }
        else if (v.getId() == R.id.measureStart)
        {
            // 計測開始
            startDeviceMeasure(mConnectedHandleID);
            mLoggingTrigger = true;
        }
        else if (v.getId() == R.id.measureStop)
        {
            stopDeviceMeasure();
            mLoggingTrigger = false;
        }
    }
}
