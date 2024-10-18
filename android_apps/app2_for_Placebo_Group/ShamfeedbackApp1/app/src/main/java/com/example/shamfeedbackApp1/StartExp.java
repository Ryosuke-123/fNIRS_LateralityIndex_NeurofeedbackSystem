package com.example.shamfeedbackApp1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;

public class StartExp extends AppCompatActivity implements View.OnClickListener {
    BluetoothAdapter mBluetoothAdapter;
    UsbManager mUsbManager;
    ExBrainApi mExBrainApi;
    String mBtScanSearchNames = "HOT,XB";
    DeviceInformationEx mDeviceInfo;
    String mConnectedHandleID;

    LineGraph mLineGraph;

    LineChart mChart1;

    boolean mLoggingTrigger = false;

    String mBtAddress = "";
    String mRestTime = ""; // プレスキャンの時間
    String mBaselineTime = ""; // Baseline定義時間
    String mMovingAvgTime = ""; // 移動平均の時間幅
    String mTrainingTime = ""; // トレーニングタイム
    double mSamplingTime = 0.1; // サンプリング時間[s]

    int mRestDataNumber = 0;
    int mBaselineDataNumber = 0;
    int mMovingAvgDataNumber = 0;
    int mTrainingDataNumber = 0;

    int mRestNumberCount = 0;
    int mBaselineNumberCount = 0;
    int mMovingAvgNumberCount = 0;
    int mTrainingNumberCount = 0;


    double[] mLeftBaseline;
    double[] mRightBaseline;
    double mLeftSumBrain = 0.0; // Baseline期間における脳活動値の合計(左)
    double mRightSumBrain = 0.0; // Baseline期間における脳活動値の合計(右)
    double preScanLeftBaseline = 0.0;
    double preScanRightBaseline = 0.0;

    NFMeasureDataManager mNFMeasureDataManager;
    int mResultCounter = 0;   // サンプリングカウンタ

    double leftBrain = 0.0; // 脳活動値(左, MD-ICA)
    double rightBrain = 0.0; // 脳活動値(右, MD-ICA)

    double leftBaseline = 0.0; // 左ベースライン脳血流
    double rightBaseline = 0.0; // 右ベースライン脳血流
    double baseline = 0.0; // ベースライン基準値
    double left3cmBaseline = 0.0; // 左3cmベースライン脳血流
    double left1cmBaseline = 0.0; // 左1cmベースライン脳血流
    double right3cmBaseline = 0.0; // 右3cmベースライン脳血流
    double right1cmBaseline = 0.0; // 右1cmベースライン脳血流
    double averageBrainGain = 0.0; // Training期間の最初のLI値を求める為の左右の脳血流変化について、その差が0となるようなゲイン値

    double rawLeftBrain = 0.0; // 脳血流変化の生データ(左)
    double rawRightBrain = 0.0; // 脳血流変化の生データ(右)
    double preScan1LeftBrain = 0.0; // PreScan1期間の脳血流変化(左)
    double preScan1RightBrain = 0.0; // PreScan1期間の脳血流変化(右)
    double preScan2LeftBrain = 0.0; // PreScan2期間の脳血流変化(左)
    double preScan2RightBrain = 0.0; // PreScan2期間の脳血流変化(右)
    double trainingLeftBrain = 0.0; // Training期間の脳血流変化(左)
    double trainingRightBrain = 0.0; // Training期間の脳血流変化(右)
    double leftAverageBrain = 0.0; // 過去t秒間の平均脳血流変化(左)
    double rightAverageBrain = 0.0; // 過去t秒間の平均脳血流変化(右)
    double leftGainingAverageBrain = 0.0; // ゲイン調整後の平均脳血流変化(左)
    double rightGainingAverageBrain = 0.0; // ゲイン調整後の平均脳血流変化(右)
    double liBrain1 = 0.0; // 側性化指標(左脳血流変化 - 右脳血流変化 / 左脳血流変化 + 右脳血流変化)
    double liBrain2 = 0.0; // 側性化指標(右脳血流変化 - 左脳血流変化 / 右脳血流変化 + 左脳血流変化)
    double scalingLiBrain1 = 0.0; // スケーリングした側性化指標(左脳血流変化 - 右脳血流変化 / 左脳血流変化 + 右脳血流変化)
    double scalingLiBrain2 = 0.0; // スケーリングした側性化指標(右脳血流変化 - 左脳血流変化 / 右脳血流変化 + 左脳血流変化)
    double left3cmBrain = 0.0; // 左3cm脳血流変化
    double left1cmBrain = 0.0; // 左1cn脳血流変化
    double right3cmBrain = 0.0; // 右3cm脳血流変化
    double right1cmBrain = 0.0; // 右1cm脳血流変化
    double pulseRate = 0.0; // 脈拍
    double accX = 0.0; // X軸加速度
    double accY = 0.0; // Y軸加速度
    double accZ = 0.0; // Z軸加速度
    double gyroX = 0.0; // X軸角速度
    double gyroY = 0.0; // Y軸角速度
    double gyroZ = 0.0; // Z軸角速度
    short  mark = 0;   // マーク

    double[] preScan2LeftBrainDataset;
    double[] preScan2RightBrainDataset;
    double[] leftBrainDataset;
    double[] rightBrainDataset;
    double maxBrain;
    double liRange = 0.0; // LI値の範囲(推定値)
    double liScaling = 0.0; // LI値のスケーリング
    double leftBrainDatasetSum = 0.0; // leftBrainDatasetの足し算
    double rightBrainDatasetSum = 0.0; // rightBrainDatasetの足し算

    double batteryHot2000 = 0.0; // HOT2000電池残量[%]

    int mLineNumber  = 0; // 読み取りたいcsvファイルの行
    double mNoiseData = 0.0; // csvファイルから読み取ったノイズデータ

    int mExpModeFlag = 0;
    int mExpMode = 0;   // 実験モード(0: Rest Mode, 1: NF Mode)

    String mFilename;

    // 聴覚NF用クラス
    NFVolume mNFVolume;

    // MediaPlayerの設定
    MediaPlayer mMediaPlayer1;
    MediaPlayer mMediaPlayer2;
    boolean mMediaPlayerState = true;

    // csvファイル読み取り用
    InputStream       mInputStream;
    InputStreamReader mInputStreamReader;
    BufferedReader    mBufferedReader;

    // 白色雑音用AudioTrack
    AudioTrack mAudioTrack;
    double mWhiteNoiseVolume = 0.05; // 白色雑音の初期音量を設定

    // AudioManagerの設定
    AudioManager mAudioManager;

    String[] descriptionData = {"検索", "接続", "ゲイン調整", "完了"};
    StateProgressBar mStateProgressBar;

    // ノイズ検出クラス
    DetectNoise mDetectNoise;
    int mMotionArtifactState = 0;

    TextView mCurrentState;
    TextView mLeftBrainValue;
    TextView mRightBrainValue;
    TextView mLeftMotionArtifact;
    TextView mRightMotionArtifact;
    TextView mLIBrainValue;
    TextView mTrialValue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_exp);

        mStateProgressBar = findViewById(R.id.device_setting_progress);
        mStateProgressBar.setStateDescriptionData(descriptionData);
        mStateProgressBar.checkStateCompleted(true);

        // タイトルバーの削除
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.hide();
        }

        // ボタンの設定
        findViewById(R.id.startSetting).setOnClickListener(this);
        findViewById(R.id.measureStart).setOnClickListener(this);
        findViewById(R.id.measureStop).setOnClickListener(this);

        // テキストの設定
        mCurrentState        = findViewById(R.id.currentState);
        mLeftBrainValue      = findViewById(R.id.leftBrainValue);
        mRightBrainValue     = findViewById(R.id.rightBrainValue);
        mLeftMotionArtifact  = findViewById(R.id.leftMotionArtifact);
        mRightMotionArtifact = findViewById(R.id.rightMotionArtifact);
        mLIBrainValue        = findViewById(R.id.liBrainValue);

        // tabHostの初期化および設定処理
        initTabs();

        // Bluetoothサービス有無確認
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }

        // 外部ストレージ設定(ログファイル保存用)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // [Bluetoothアダプタ生成＆確認]
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            finish();
        }

        // [USBサービス生成]
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // ドライバAPI生成/コールバック登録
        mExBrainApi = ExBrainApi.instance();

        // BluetoothとUSBサービスおよび通信ログ出力ありとしてドライバAPIを初期化 デバイス1用
        try {
            EnumExBrainResult result = mExBrainApi.initApi(mBluetoothManager, mUsbManager, false, false, "ExBrainSdk1", new IExBrainSdkCallbacks() {
                @Override
                public void onNotifyStatusChanged(String s, EnumExBrainStatus enumExBrainStatus) {

                }

                @Override
                public void onNotifyActionEvents(String s, EnumExBrainEvents enumExBrainEvents) {

                }

                @Override
                public void onNotifyMeasureData(String s, ExBrainMeasureData exBrainMeasureData) {

                }

                @Override
                public void onNotifyMeasureRawData(String s, DeviceMeasureData deviceMeasureData)
                {
                    // HOT2000バッテリー残量確認
                    batteryHot2000 = deviceMeasureData.batteryGauge;
                    checkHOT2000Battery(batteryHot2000);

                    // プラセボノイズデータ(csvファイルの読み取り)
                    mLineNumber = mResultCounter;
                    mNoiseData = readNoiseCSV(mLineNumber + 1);

                    // Log.d("noiseData", String.valueOf(mNoiseData));
                    // Log.d("LineNumber", String.valueOf(mLineNumber));

                    // Log.d("Battery", String.valueOf(deviceMeasureData.batteryGauge));

                    // 計測データ ロギング
                    if (mLoggingTrigger)
                    {
                        loggingData(deviceMeasureData);
                    }



                    // グラフ描画
                    // mLineGraph.display((float) liBrain1);
                    mLineGraph.display((float)0.0);

                    if (mExpMode == 1) // LIと白色雑音の音量を対応付けるフィードバックの開始
                    {

                        mWhiteNoiseVolume = (float) liTransToVolume(mNoiseData);

                        // mWhiteNoiseVolume = mNoiseData;

                        // mWhiteNoiseVolume = (float) liTransToVolume(liBrain1);
                        setWhiteNoiseVolume((float) mWhiteNoiseVolume);

                        // テキスト表示
                        // mLeftBrainValue.setText(String.format("%.2f", leftAverageBrain));
                        // mRightBrainValue.setText(String.format("%.2f", rightAverageBrain));
                        // mLIBrainValue.setText(String.format("%.2f", liBrain1));
                    }

                    if (mExpMode == 1) {
                        // mLeftBrainValue.setText(String.format("%.2f", preScan1LeftBrain));
                        // mRightBrainValue.setText(String.format("%.2f", preScan1RightBrain));
                        // mLIBrainValue.setText(String.format("%.2f", scalingLiBrain1));
                    }

                    // テキスト表示
                    /*if (mMotionArtifactState == 1)
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
                        mRightMotionArtifact.setText("正常");
                    }*/
                }

                @Override
                public void onNotifySearchResult(EnumExBrainSearchEvent enumExBrainSearchEvent, DeviceInformationEx deviceInformationEx) {

                }

                @Override
                public void onUsbPermissionRequest(DeviceInformationEx deviceInformationEx) {

                }
            });

            if (result == EnumExBrainResult.eSuccess) {
                Log.d("Product Name", mExBrainApi.getProductName());
                Log.d("Product Version", mExBrainApi.getProductVersion());
            }

            // インテントフィルターとBroadcastReceiverの登録
            registerReceiver(mExBrainApi.getBluetoothReceiver(), mExBrainApi.getBluetoothReceiverIntentFilter());
        } catch (ExBrainException ex) {
            ex.printStackTrace();
        }

        // グラフ描画準備
        mLineGraph = new LineGraph();
        mChart1 = findViewById(R.id.chart);
        mLineGraph.initSetting(mChart1);

        // 実験条件準備
        Intent expSettingIntent = getIntent();

        mRestTime = expSettingIntent.getStringExtra("restTime");
        mBaselineTime = expSettingIntent.getStringExtra("baselineTime");
        mMovingAvgTime = expSettingIntent.getStringExtra("movingAvgTime");
        mTrainingTime = expSettingIntent.getStringExtra("trainingTime");

        /*-------------- 実験条件の反映 ------------*/
        mRestDataNumber = (int) (Integer.parseInt(mRestTime) / mSamplingTime);
        mBaselineDataNumber = (int) (Integer.parseInt(mBaselineTime) / mSamplingTime);
        mMovingAvgDataNumber = (int) (Integer.parseInt(mMovingAvgTime) / mSamplingTime);
        mTrainingDataNumber = (int) (Integer.parseInt(mTrainingTime) / mSamplingTime);

        leftBrainDataset = new double[mMovingAvgDataNumber]; // 左平均脳活動値を算出する為のデータセットを準備
        rightBrainDataset = new double[mMovingAvgDataNumber]; // 右平均脳活動値を算出する為のデータセットを準備

        mLeftBaseline = new double[mBaselineDataNumber];
        mRightBaseline = new double[mBaselineDataNumber];

        mNFVolume = new NFVolume();

        /*---------------- MediaPlayerの準備 ------------------*/
        mMediaPlayer1 = MediaPlayer.create(this, R.raw.jupiter);
        mMediaPlayer2 = MediaPlayer.create(this, R.raw.beep);

        /*-------------------- AudioManagerの準備 -------------------*/
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /*--------------------- ビープ音の準備 ---------------------*/

        // データロガー準備
        mNFMeasureDataManager = new NFMeasureDataManager();

        // ノイズ検出クラス準備
        mDetectNoise = new DetectNoise();

        // 白色ノイズをランダムに選択
        Random rand = new Random();
        int num = rand.nextInt(20) + 1; // 1~20の乱数を生成

        switch (num)
        {
            case 1:
                mFilename = "NoiseData1.csv";
                break;
            case 2:
                mFilename = "NoiseData2.csv";
                break;
            case 3:
                mFilename = "NoiseData3.csv";
                break;
            case 4:
                mFilename = "NoiseData4.csv";
                break;
            case 5:
                mFilename = "NoiseData5.csv";
                break;
            case 6:
                mFilename = "NoiseData6.csv";
                break;
            case 7:
                mFilename = "NoiseData7.csv";
                break;
            case 8:
                mFilename = "NoiseData8.csv";
                break;
            case 9:
                mFilename = "NoiseData9.csv";
                break;
            case 10:
                mFilename = "NoiseData10.csv";
                break;
            case 11:
                mFilename = "NoiseData11.csv";
                break;
            case 12:
                mFilename = "NoiseData12.csv";
                break;
            case 13:
                mFilename = "NoiseData13.csv";
                break;
            case 14:
                mFilename = "NoiseData14.csv";
                break;
            case 15:
                mFilename = "NoiseData15.csv";
                break;
            case 16:
                mFilename = "NoiseData16.csv";
                break;
            case 17:
                mFilename = "NoiseData17.csv";
                break;
            case 18:
                mFilename = "NoiseData18.csv";
                break;
            case 19:
                mFilename = "NoiseData19.csv";
                break;
            case 20:
                mFilename = "NoiseData20.csv";
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + num);
        }

        Log.d("File Name: ",mFilename);
    }

    void initTabs() {
        try {
            TabHost tabHost = findViewById(R.id.tabHost);
            tabHost.setup();
            TabHost.TabSpec spec;

            // tab1
            spec = tabHost.newTabSpec("Tab1")
                    .setIndicator("デバイス")
                    .setContent(R.id.prepare_layout);
            tabHost.addTab(spec);

            // tab2
            spec = tabHost.newTabSpec("Tab2")
                    .setIndicator("計測")
                    .setContent(R.id.measure_layout);
            tabHost.addTab(spec);

            // tab3
            spec = tabHost.newTabSpec("Tab3")
                    .setIndicator("実験条件")
                    .setContent(R.id.conditions_layout);
            tabHost.addTab(spec);

            tabHost.setCurrentTab(0);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    void startPairedBluetoothDeviceScan() {
        try {
            // [APIによる装置検索用のパラメータを作成]
            ExBrainSearchParameter search_option = new ExBrainSearchParameter();
            search_option.shouldNonPairedBluetooth = false;
            search_option.shouldPairedBluetooth = true;
            search_option.shouldUSB = false;
            search_option.timeoutSecond = 0;
            search_option.setSearchKeyword(mBtScanSearchNames);

            // [APIによる装置検索]
            ExBrainDiscoveredDevice discoveredDevice = new ExBrainDiscoveredDevice();
            EnumExBrainResult result = mExBrainApi.searchDevice(search_option, discoveredDevice);

            if (result == EnumExBrainResult.eSuccess) {
                Log.d("Discover Device", "Success");

                for (DeviceInformationEx device : discoveredDevice.devices) {
                    mDeviceInfo = device;
                }

                if (mStateProgressBar.getCurrentStateNumber() == 1) {
                    mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    connectDevice(mDeviceInfo);
                }
            } else {
                Log.d("Discover Device", "False");
            }
        } catch (Exception ex) {
            Log.d("Discover Device", "Error");
        }
    }

    void connectDevice(DeviceInformationEx deviceInfo) {
        String handleID = deviceInfo.deviceId;
        EnumExBrainConnectType connectType = deviceInfo.connectType;

        // アプリ上のサービス等を確認
        if (connectType == EnumExBrainConnectType.eBluetooth) {
            if (!mExBrainApi.isBtServiceConnected()) {
                // Bluetoothサービス開始
                Intent gattServiceIntent = new Intent(this, DriverBluetoothDevice.class);
                bindService(gattServiceIntent, mExBrainApi.getServiceConnection(), BIND_AUTO_CREATE);
            }
        } else if (connectType == EnumExBrainConnectType.eUsb) {

        }

        // APIによる装置接続
        Executors.newCachedThreadPool().submit(() -> {
            try {
                EnumExBrainResult result = mExBrainApi.connectDevice(handleID, connectType, false);

                if (result == EnumExBrainResult.eSuccess) {
                    mConnectedHandleID = mExBrainApi.getHandleId();

                    if (mStateProgressBar.getCurrentStateNumber() == 2) {
                        mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                        startDeviceGainAdjust(mConnectedHandleID);
                    }
                } else {
                    mConnectedHandleID = "";
                }

            } catch (Exception ex) {

            }
        });
    }

    void startDeviceGainAdjust(String connectHandleID) {
        // APIによるゲイン調整開始
        Executors.newCachedThreadPool().submit(() -> {
            try {
                EnumExBrainResult result = mExBrainApi.startGainAdjust(connectHandleID);
                if (result == EnumExBrainResult.eSuccess) {
                    if (mStateProgressBar.getCurrentStateNumber() == 3) {
                        mStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                    }
                } else {

                }
            } catch (Exception ex) {

            }
        });
    }

    void startDeviceMeasure(String connectHandleID) {
        // APIによる装置計測開始
        Executors.newCachedThreadPool().submit(() -> {

            mBtAddress = connectHandleID;

            try {
                EnumExBrainResult result = mExBrainApi.startMeasure(connectHandleID, true);

                if (result == EnumExBrainResult.eSuccess) {
                    // NFシステム 計測データ ストア準備開始
                    boolean logStartingResult = mNFMeasureDataManager.startStoreNFMeasureData(true, "NF_ExpLog", mBtAddress, mRestTime, mBaselineTime, mMovingAvgTime, mTrainingTime);
                    if (logStartingResult) {
                        Log.d("LogStartingResult", "Success");
                    } else {
                        Log.d("LogStartingResult", "False");
                    }
                } else {

                }
            } catch (Exception ex) {

            }
        });
    }

    // 装置の測定を停止する
    void stopDeviceMeasure() {
        // APIによる装置計測停止
        Executors.newCachedThreadPool().submit(() -> {
            try {
                EnumExBrainResult result = mExBrainApi.stopMeasure(mConnectedHandleID);
                if (result == EnumExBrainResult.eSuccess) {
                    Log.d("Measure", "Stop Measure");
                } else {

                }
            } catch (Exception ex) {

            }
        });
    }

    void loggingData(DeviceMeasureData measureData) {
        // ロギング用データ準備
        ArrayList<Double> brainDoubleData = new ArrayList<>();
        ArrayList<Double> heartDoubleData = new ArrayList<>();
        ArrayList<Double> motionDoubleData = new ArrayList<>();
        ArrayList<Short> shortData = new ArrayList<>();

        // 計測データ
        leftBrain = measureData.bloodHbIndexValues[0]; // 脳活動値(左, MD-ICA)
        rightBrain = measureData.bloodHbIndexValues[1]; // 脳活動値(右, MD-ICA)
        left3cmBrain = measureData.bloodHbDensities[1] - left3cmBaseline;
        left1cmBrain = measureData.bloodHbDensities[0] - left1cmBaseline;
        right3cmBrain = measureData.bloodHbDensities[3] - right3cmBaseline;
        right1cmBrain = measureData.bloodHbDensities[2] - right1cmBaseline;
        pulseRate = measureData.heartRate;
        accX = measureData.motionAccValues[0];
        accY = measureData.motionAccValues[1];
        accZ = measureData.motionAccValues[2];
        gyroX = measureData.motionGyroValues[0];
        gyroY = measureData.motionGyroValues[1];
        gyroZ = measureData.motionGyroValues[2];

        Log.d("leftBrain", String.valueOf(leftBrain));
        Log.d("RightData", String.valueOf(rightBrain));

        // Expモード(1: PreScan1, 2: PreScan2, 3: Training)の変更
        if (mExpModeFlag == 0)
        {
            mExpMode = 0;
            mark = 0;
            mCurrentState.setText("Rest期間");
        }
        else if (mExpModeFlag == 1)
        {
            mExpMode = 1;
            mark = 1;
            mCurrentState.setText("NF期間");
        }

        if (mExpMode == 0)      // Rest Mode
        {
            if (mRestNumberCount == 0) {
                mNFVolume.trialBeepSound(mAudioManager, mMediaPlayer2);
            }

            // Baseline期間の場合
            if ((mRestDataNumber - mBaselineDataNumber) <= mRestNumberCount && mRestNumberCount <= mRestDataNumber - 1) {
                // Baseline期間の脳活動値を格納
                mLeftBaseline[mBaselineNumberCount] = leftBrain;
                mRightBaseline[mBaselineNumberCount] = rightBrain;

                if ((mRestDataNumber - mMovingAvgDataNumber <= mRestNumberCount && mRestNumberCount <= mRestDataNumber - 1)) {
                    leftBrainDataset[mMovingAvgNumberCount] = leftBrain;
                    rightBrainDataset[mMovingAvgNumberCount] = rightBrain;

                    mMovingAvgNumberCount = mMovingAvgNumberCount + 1;
                }

                // Baseline期間の終了時
                if (mBaselineNumberCount == mBaselineDataNumber - 1) {
                    // Baseline期間における平均脳活動値(Baseline脳活動値)を算出
                    for (int i = 0; i < mBaselineDataNumber; i++) {
                        mLeftSumBrain += mLeftBaseline[i];
                        mRightSumBrain += mRightBaseline[i];
                    }

                    preScanLeftBaseline = mLeftSumBrain / mLeftBaseline.length;
                    preScanRightBaseline = mRightSumBrain / mRightBaseline.length;

                    // NF期間前t秒間の脳活動値をBaseline補正(Baselineは1)
                    for (int i = 0; i < mMovingAvgDataNumber; i++) {
                        leftBrainDataset[i] = leftBrainDataset[i] - preScanLeftBaseline + 1;
                        rightBrainDataset[i] = rightBrainDataset[i] - preScanRightBaseline + 1;
                    }

                    // NF Modeへの移行準備
                    mExpModeFlag = 1;
                    mMediaPlayerState = true;


                    // カウントリセット

                } else {
                    mBaselineNumberCount = mBaselineNumberCount + 1;
                    mRestNumberCount = mRestNumberCount + 1;
                }
            } else {
                mRestNumberCount = mRestNumberCount + 1;
            }
        } else if (mExpMode == 1) // NF Mode
        {
            // 脳活動値の算出(Baseline = 1)
            trainingLeftBrain = leftBrain - preScanLeftBaseline + 1;
            trainingRightBrain = rightBrain - preScanRightBaseline + 1;

            // t秒前の平均脳活動値を算出
            for (int i = 0; i < mMovingAvgDataNumber; i++) {
                leftBrainDatasetSum += leftBrainDataset[i];
                rightBrainDatasetSum += rightBrainDataset[i];
            }

            leftAverageBrain = leftBrainDatasetSum / mMovingAvgDataNumber;
            rightAverageBrain = rightBrainDatasetSum / mMovingAvgDataNumber;

            // NF Mode開始の合図
            if (mTrainingNumberCount == 0) {
                mNFVolume.trialBeepSound(mAudioManager, mMediaPlayer2);

                mNFVolume.startVolume(mMediaPlayer1);
                createWhiteNoise(); // 白色雑音作成
            }

            // リセット
            leftBrainDatasetSum = 0.0;
            rightBrainDatasetSum = 0.0;

            // LI値の算出
            liBrain1 = (leftAverageBrain - rightAverageBrain) / (leftAverageBrain + rightAverageBrain);
            liBrain2 = (rightAverageBrain - leftAverageBrain) / (rightAverageBrain + leftAverageBrain);

            // t秒前の脳活動データセットをスライド
            for (int i = 0; i < mMovingAvgDataNumber; i++) {
                if (i != mMovingAvgDataNumber - 1) {
                    leftBrainDataset[i] = leftBrainDataset[i + 1];
                    rightBrainDataset[i] = rightBrainDataset[i + 1];
                } else {
                    leftBrainDataset[i] = 0.0;
                    rightBrainDataset[i] = 0.0;
                }
            }

            // t秒前の脳活動データセットに新しいデータを追加
            leftBrainDataset[mMovingAvgDataNumber - 1] = trainingLeftBrain;
            rightBrainDataset[mMovingAvgDataNumber - 1] = trainingRightBrain;

            if (mTrainingNumberCount == mTrainingDataNumber - 1)
            {
                mCurrentState.setText("終了");

                // 音源ストップ
                mNFVolume.stopVolume(mMediaPlayer1);
                mNFVolume.trialBeepSound(mAudioManager, mMediaPlayer2);

                // 白色雑音ストップ
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;

                // 計測ストップ
                stopDeviceMeasure();

                //** ------ APIログフォルダの削除 ---- //
                deleteAPILogFolder();

            } else {
                mTrainingNumberCount = mTrainingNumberCount + 1;
            }
        }

        /*----------------------Logging用データ-----------------------*/
        brainDoubleData.add(leftBrain);         // 脳活動値(左, MD-ICA)
        brainDoubleData.add(rightBrain);        // 脳活動値(右, MD-ICA)
        brainDoubleData.add(leftAverageBrain);  // 過去t秒間の平均脳活動値(左)
        brainDoubleData.add(rightAverageBrain); // 過去t秒間の平均脳活動値(右)
        brainDoubleData.add(liBrain1);          // 算出されたLI値
        brainDoubleData.add(mNoiseData);        // 実際にFBされたLI値
        brainDoubleData.add(left3cmBrain);
        brainDoubleData.add(left1cmBrain);
        brainDoubleData.add(right3cmBrain);
        brainDoubleData.add(right1cmBrain);
        heartDoubleData.add(pulseRate);
        motionDoubleData.add(accX);
        motionDoubleData.add(accY);
        motionDoubleData.add(accZ);
        motionDoubleData.add(gyroX);
        motionDoubleData.add(gyroY);
        motionDoubleData.add(gyroZ);
        shortData.add(mark);

        boolean LogStoringResult = mNFMeasureDataManager.entryStoredMeasureData(mResultCounter, brainDoubleData, heartDoubleData, motionDoubleData, shortData);
        if (LogStoringResult) {
            Log.d("LogStoringResult", "Success");
        } else {
            Log.d("LogStoringResult", "False");
        }

        /*------ カウンター更新 ------*/
        mResultCounter = mResultCounter + 1;
    }

    // ノイズデータの読み取り
    double readNoiseCSV(int lineNumber)
    {
        double noiseData = 0.0;
        try {
            InputStream inputStream = getResources().getAssets().open(mFilename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            int row = 1;

            while ((line = bufferedReader.readLine()) != null)
            {
                System.out.println(line);
                if (row == lineNumber)
                {
                    String data = line.replaceAll("\\uFEFF","");
                    noiseData = Double.parseDouble(data);
                    System.out.println(noiseData);
                    break;
                }
                row++;
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noiseData;
    }

    void createWhiteNoise() {
        int sampleRate = 44100;
        int duration = Integer.parseInt(mTrainingTime); // in seconds, 再生時間
        int numberOfSamples = sampleRate * duration;

        byte[] whiteNoise = new byte[numberOfSamples];
        Random random = new Random();

        // SPL: 音圧レベル
        // P:   音圧の値
        // P0:  参照音圧(通常20μPa)

        // (SPL=30, P0=20μPa)とした時の、音圧Pと実効値RMSを算出
        double p0 = 20;
        double p = p0 * Math.pow(10, 30 / 20);
        double rms = p / Math.sqrt(2);

        // 再生時間分のノイズデータを作成
        for (int i = 0; i < numberOfSamples; i++) {
            // ホワイトノイズを作成する
            double sample = random.nextGaussian() * rms;
            whiteNoise[i] = (byte) ((sample * 127.0) / rms);
        }

        mMediaPlayer1.setAuxEffectSendLevel(1.0f);
        int sessionId = mMediaPlayer1.getAudioSessionId();
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,      // 再生する音声の種類を指定するためのパラメータ
                sampleRate,                     // 再生する音声のサンプリングレートを指定するためのパラメータ
                AudioFormat.CHANNEL_OUT_STEREO, // 再生する音声のチャンネル数と配置を指定するためのパラメータ
                AudioFormat.ENCODING_PCM_8BIT,  // 再生する音声のフォーマットを指定するためのパラメータ
                numberOfSamples,                // 再生用のバッファサイズを指定するためのパラメータ
                AudioTrack.MODE_STATIC,         // 再生モードを指定するためのパラメータ
                sessionId                       // 再生する音声のセッションIDを指定するためのパラメータ
        );

        mAudioTrack.write(whiteNoise, 0, numberOfSamples);
        mAudioTrack.setLoopPoints(0, numberOfSamples / 2, -1);
        mAudioTrack.setVolume((float) mWhiteNoiseVolume); // 初期音量
        mAudioTrack.play();
    }

    // 白色雑音の音量を設定する
    void setWhiteNoiseVolume(float volume) {
        mWhiteNoiseVolume = volume;
        if (mAudioTrack != null) {
            mAudioTrack.setVolume((float) mWhiteNoiseVolume);
        }
    }

    // LIの値から白色雑音の音量を決定
    double liTransToVolume(double liValue)
    {
        double volume = 0.0;

        if (0.2 <= liValue)
        {
            volume = 0.0;
        }
        else if (0.15 <= liValue && liValue < 0.2)
        {
            volume = 0.02;
        }
        else if (0.10 <= liValue && liValue < 0.15)
        {
            volume = 0.04;
        }
        else if (0.05 <= liValue && liValue < 0.10)
        {
            volume = 0.06;
        }
        else if (0.00 <= liValue && liValue < 0.05)
        {
            volume = 0.08;
        }
        else if (-0.05 <= liValue && liValue < 0.00)
        {
            volume = 0.10;
        }
        else if (-0.10 <= liValue && liValue < -0.05)
        {
            volume = 0.12;
        }
        else if (-0.15 <= liValue && liValue < -0.10)
        {
            volume = 0.14;
        }
        else if (-0.20 <= liValue && liValue < -0.15)
        {
            volume = 0.16;
        }
        else if (liValue < -0.20)
        {
            volume = 0.18;
        }

        return volume;
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

    // フォルダ削除コード
    private void deleteAPILogFolder()
    {
        // 内部共有ストレージのパスを取得
        File apiLogFolder = new File(Environment.getExternalStorageDirectory(),"ExBrainSdk1");

        if (apiLogFolder.exists())
        {
            deleteRecursive((apiLogFolder));
        }
    }

    // 再帰的にフォルダ内の全てのファイルとフォルダを削除するメソッド
    private void deleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}
