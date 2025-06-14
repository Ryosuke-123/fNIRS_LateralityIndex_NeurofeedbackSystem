package com.example.neurofeedbackApp2;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
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

    String               mBtAddress     = "";  // Bluetooth Address
    String               mRestTime      = "";  // Time of Rest Period
    String               mBaselineTime  = "";  // Time of Baseline Period
    String               mMovingAvgTime = "";  // Time of Moving Average
    String               mTrainingTime  = "";  // Time of Feedback Period
    double               mSamplingTime  = 0.1; // Sampling Time[s]

    int                  mRestDataNumber      = 0;
    int                  mBaselineDataNumber  = 0;
    int                  mMovingAvgDataNumber = 0;
    int                  mTrainingDataNumber  = 0;

    int                  mRestNumberCount      = 0;
    int                  mBaselineNumberCount  = 0;
    int                  mMovingAvgNumberCount = 0;
    int                  mTrainingNumberCount  = 0;


    double[]             mLeftBaseline;
    double[]             mRightBaseline;
    double               mLeftSumBrain        = 0.0;
    double               mRightSumBrain       = 0.0;
    double               preScanLeftBaseline  = 0.0;
    double               preScanRightBaseline = 0.0;

    NFMeasureDataManager mNFMeasureDataManager;
    int                  mResultCounter       = 0;

    double               leftBrain            = 0.0; // PFC Activation(Left)
    double               rightBrain           = 0.0; // PFC Activation(Right)

    double               left3cmBaseline      = 0.0;
    double               left1cmBaseline      = 0.0;
    double               right3cmBaseline     = 0.0;
    double               right1cmBaseline     = 0.0;

    double               trainingLeftBrain        = 0.0; // PFC Activation in Feedback Period(Left)
    double               trainingRightBrain       = 0.0; // PFC Activation in Feedback Period(Right)
    double               leftAverageBrain         = 0.0;
    double               rightAverageBrain        = 0.0;
    double               liBrain1                 = 0.0; // Lateralization Index (L - R / L + R)
    double               liBrain2                 = 0.0; // Lateralization Index (R - L / R + L)
    double               left3cmBrain             = 0.0; // PFC Activation (Left-3cm)
    double               left1cmBrain             = 0.0; // PFC Activation (Left-1cm)
    double               right3cmBrain            = 0.0; // PFC Activation (Right-3cm)
    double               right1cmBrain            = 0.0; // PFC Activation (Right-1cm)
    double               pulseRate                = 0.0; // Pulse Rate
    double               accX                     = 0.0; // x-axis acceleration
    double               accY                     = 0.0; // y-axis acceleration
    double               accZ                     = 0.0; // z-axis acceleration
    double               gyroX                    = 0.0; // x-axis angular velocity
    double               gyroY                    = 0.0; // y-axis angular velocity
    double               gyroZ                    = 0.0; // z-axis angular velocity
    short                mark                     = 0;   // Mark

    double[]             leftBrainDataset;
    double[]             rightBrainDataset;
    double               leftBrainDatasetSum  = 0.0;
    double               rightBrainDatasetSum = 0.0;

    double               batteryHot2000    = 0.0; // Battery level of HOT-2000 [%]

    int                  mExpModeFlag      = 0;
    int                  mExpMode          = 0;

    NFVolume             mNFVolume;

    MediaPlayer          mMediaPlayer1;
    MediaPlayer          mMediaPlayer2;
    boolean              mMediaPlayerState = true;

    AudioTrack           mAudioTrack;
    float                mWhiteNoiseVolume = 0.1f;

    AudioManager mAudioManager;

    String[] descriptionData = {"検索","接続","ゲイン調整","完了"};
    StateProgressBar mStateProgressBar;

    DetectNoise mDetectNoise;

    TextView mCurrentState;
    TextView mLeftBrainValue;
    TextView mRightBrainValue;
    TextView mLeftMotionArtifact;
    TextView mRightMotionArtifact;
    TextView mLIBrainValue;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_exp);


        mStateProgressBar = findViewById(R.id.device_setting_progress);
        mStateProgressBar.setStateDescriptionData(descriptionData);
        mStateProgressBar.checkStateCompleted(true);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
        {
            mActionBar.hide();
        }

        findViewById(R.id.startSetting).setOnClickListener(this);
        findViewById(R.id.measureStart).setOnClickListener(this);
        findViewById(R.id.measureStop).setOnClickListener(this);

        mCurrentState        = findViewById(R.id.currentState);
        mLeftBrainValue      = findViewById(R.id.leftBrainValue);
        mRightBrainValue     = findViewById(R.id.rightBrainValue);
        mLeftMotionArtifact  = findViewById(R.id.leftMotionArtifact);
        mRightMotionArtifact = findViewById(R.id.rightMotionArtifact);
        mLIBrainValue        = findViewById(R.id.liBrainValue);

        initTabs();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            finish();
        }

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mExBrainApi = ExBrainApi.instance();

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
                    batteryHot2000 = deviceMeasureData.batteryGauge;
                    checkHOT2000Battery(batteryHot2000);

                    Log.d("Battery", String.valueOf(deviceMeasureData.batteryGauge));

                    // Start Logging
                    if (mLoggingTrigger)
                    {
                        loggingData(deviceMeasureData);
                    }

                    mLineGraph.display((float)0.0);

                    if (mExpMode == 1)
                    {
                        mWhiteNoiseVolume = (float) liTransToVolume(liBrain2);
                        setWhiteNoiseVolume(mWhiteNoiseVolume);
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

            registerReceiver( mExBrainApi.getBluetoothReceiver(), mExBrainApi.getBluetoothReceiverIntentFilter() ) ;
        }
        catch (ExBrainException ex)
        {
            ex.printStackTrace();
        }

        mLineGraph = new LineGraph();
        mChart1    = findViewById(R.id.chart);
        mLineGraph.initSetting(mChart1);

        Intent expSettingIntent = getIntent();

        mRestTime      = expSettingIntent.getStringExtra("restTime");
        mBaselineTime  = expSettingIntent.getStringExtra("baselineTime");
        mMovingAvgTime = expSettingIntent.getStringExtra("movingAvgTime");
        mTrainingTime  = expSettingIntent.getStringExtra("trainingTime");

        mRestDataNumber      = (int)(Integer.parseInt(mRestTime) / mSamplingTime);
        mBaselineDataNumber  = (int)(Integer.parseInt(mBaselineTime) / mSamplingTime);
        mMovingAvgDataNumber = (int)(Integer.parseInt(mMovingAvgTime) / mSamplingTime);
        mTrainingDataNumber  = (int)(Integer.parseInt(mTrainingTime) / mSamplingTime);

        leftBrainDataset  = new double[mMovingAvgDataNumber];
        rightBrainDataset = new double[mMovingAvgDataNumber];

        mLeftBaseline  = new double[mBaselineDataNumber];
        mRightBaseline = new double[mBaselineDataNumber];

        mNFVolume = new NFVolume();

        mMediaPlayer1 = MediaPlayer.create(this,R.raw.jupiter);
        mMediaPlayer2 = MediaPlayer.create(this,R.raw.beep);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mNFMeasureDataManager = new NFMeasureDataManager();

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
            ExBrainSearchParameter search_option = new ExBrainSearchParameter();
            search_option.shouldNonPairedBluetooth = false;
            search_option.shouldPairedBluetooth = true;
            search_option.shouldUSB = false;
            search_option.timeoutSecond = 0;
            search_option.setSearchKeyword(mBtScanSearchNames);

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

        if (connectType == EnumExBrainConnectType.eBluetooth)
        {
            if (!mExBrainApi.isBtServiceConnected())
            {
                Intent gattServiceIntent = new Intent(this, DriverBluetoothDevice.class);
                bindService(gattServiceIntent, mExBrainApi.getServiceConnection(), BIND_AUTO_CREATE);
            }
        }
        else if (connectType == EnumExBrainConnectType.eUsb)
        {

        }

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
        Executors.newCachedThreadPool().submit(()->{

            mBtAddress = connectHandleID;

            try
            {
                EnumExBrainResult result = mExBrainApi.startMeasure(connectHandleID, true);

                if (result == EnumExBrainResult.eSuccess)
                {
                    boolean logStartingResult = mNFMeasureDataManager.startStoreNFMeasureData(true,"NF_ExpLog",mBtAddress,mRestTime,mBaselineTime,mMovingAvgTime,mTrainingTime);
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

    void stopDeviceMeasure()
    {
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
        ArrayList<Double> brainDoubleData  = new ArrayList<>();
        ArrayList<Double> heartDoubleData  = new ArrayList<>();
        ArrayList<Double> motionDoubleData = new ArrayList<>();
        ArrayList<Short>  shortData        = new ArrayList<>();

        leftBrain     = measureData.bloodHbIndexValues[0];
        rightBrain    = measureData.bloodHbIndexValues[1];
        left3cmBrain  = measureData.bloodHbDensities[1] - left3cmBaseline;
        left1cmBrain  = measureData.bloodHbDensities[0] - left1cmBaseline;
        right3cmBrain = measureData.bloodHbDensities[3] - right3cmBaseline;
        right1cmBrain = measureData.bloodHbDensities[2] - right1cmBaseline;
        pulseRate     = measureData.heartRate;
        accX          = measureData.motionAccValues[0];
        accY          = measureData.motionAccValues[1];
        accZ          = measureData.motionAccValues[2];
        gyroX         = measureData.motionGyroValues[0];
        gyroY         = measureData.motionGyroValues[1];
        gyroZ         = measureData.motionGyroValues[2];

        if (mExpModeFlag == 0)
        {
            mExpMode = 0;
            mark     = 0;
            mCurrentState.setText("Rest期間");
        }
        else if (mExpModeFlag == 1)
        {
            mExpMode = 1;
            mark     = 1;
            mCurrentState.setText("FB期間");
        }

        if (mExpMode == 0)
        {
            if (mRestNumberCount == 0)
            {
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer2);
            }

            if ((mRestDataNumber - mBaselineDataNumber) <= mRestNumberCount && mRestNumberCount <= mRestDataNumber - 1)
            {
                mLeftBaseline[mBaselineNumberCount]  = leftBrain;
                mRightBaseline[mBaselineNumberCount] = rightBrain;

                if ((mRestDataNumber - mMovingAvgDataNumber <= mRestNumberCount && mRestNumberCount <= mRestDataNumber - 1))
                {
                    leftBrainDataset[mMovingAvgNumberCount]  = leftBrain;
                    rightBrainDataset[mMovingAvgNumberCount] = rightBrain;

                    mMovingAvgNumberCount = mMovingAvgNumberCount + 1;
                }

                if (mBaselineNumberCount == mBaselineDataNumber - 1)
                {
                    for (int i=0;i<mBaselineDataNumber;i++)
                    {
                        mLeftSumBrain  += mLeftBaseline[i];
                        mRightSumBrain += mRightBaseline[i];
                    }

                    preScanLeftBaseline  = mLeftSumBrain / mLeftBaseline.length;
                    preScanRightBaseline = mRightSumBrain / mRightBaseline.length;

                    for (int i=0;i<mMovingAvgDataNumber;i++)
                    {
                        leftBrainDataset[i]  = leftBrainDataset[i] - preScanLeftBaseline + 1;
                        rightBrainDataset[i] = rightBrainDataset[i] - preScanRightBaseline + 1;
                    }

                    mExpModeFlag = 1;
                    mMediaPlayerState = true;
                }
                else
                {
                    mBaselineNumberCount = mBaselineNumberCount + 1;
                    mRestNumberCount     = mRestNumberCount + 1;
                }
            }
            else
            {
                mRestNumberCount = mRestNumberCount + 1;
            }
        }
        else if (mExpMode == 1)
        {
            trainingLeftBrain  = leftBrain - preScanLeftBaseline + 1;
            trainingRightBrain = rightBrain - preScanRightBaseline + 1;

            for (int i=0;i<mMovingAvgDataNumber;i++)
            {
                leftBrainDatasetSum  += leftBrainDataset[i];
                rightBrainDatasetSum += rightBrainDataset[i];
            }

            leftAverageBrain  = leftBrainDatasetSum / mMovingAvgDataNumber;
            rightAverageBrain = rightBrainDatasetSum / mMovingAvgDataNumber;

            if (mTrainingNumberCount == 0)
            {
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer2);

                mNFVolume.startVolume(mMediaPlayer1);
                createWhiteNoise();
            }

            leftBrainDatasetSum  = 0.0;
            rightBrainDatasetSum = 0.0;

            liBrain1 = (leftAverageBrain - rightAverageBrain) / (leftAverageBrain + rightAverageBrain);
            liBrain2 = (rightAverageBrain - leftAverageBrain) / (rightAverageBrain + leftAverageBrain);

            for (int i=0;i<mMovingAvgDataNumber;i++)
            {
                if (i != mMovingAvgDataNumber - 1)
                {
                    leftBrainDataset[i]  = leftBrainDataset[i+1];
                    rightBrainDataset[i] = rightBrainDataset[i+1];
                }
                else
                {
                    leftBrainDataset[i]  = 0.0;
                    rightBrainDataset[i] = 0.0;
                }
            }

            leftBrainDataset[mMovingAvgDataNumber - 1]  = trainingLeftBrain;
            rightBrainDataset[mMovingAvgDataNumber - 1] = trainingRightBrain;

            if (mTrainingNumberCount == mTrainingDataNumber - 1)
            {
                mCurrentState.setText("終了");

                mNFVolume.stopVolume(mMediaPlayer1);
                mNFVolume.trialBeepSound(mAudioManager,mMediaPlayer2);

                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;

                stopDeviceMeasure();

                deleteAPILogFolder();
            }
            else
            {
                mTrainingNumberCount = mTrainingNumberCount + 1;
            }
        }

        brainDoubleData.add(leftBrain);
        brainDoubleData.add(rightBrain);
        brainDoubleData.add(leftAverageBrain);
        brainDoubleData.add(rightAverageBrain);
        brainDoubleData.add(liBrain2);
        brainDoubleData.add(liBrain2);
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

        boolean LogStoringResult = mNFMeasureDataManager.entryStoredMeasureData(mResultCounter,brainDoubleData,heartDoubleData,motionDoubleData,shortData);
        if (LogStoringResult)
        {
            Log.d("LogStoringResult","Success");
        }
        else
        {
            Log.d("LogStoringResult","False");
        }

        mResultCounter = mResultCounter + 1;
    }

    void createWhiteNoise()
    {
        int sampleRate      = 44100;
        int duration        = Integer.parseInt(mTrainingTime);
        int numberOfSamples = sampleRate * duration;

        byte[] whiteNoise = new byte[numberOfSamples];
        Random random     = new Random();

        double p0  = 20;
        double p   = p0 * Math.pow(10,30/20);
        double rms = p / Math.sqrt(2);

        for (int i=0;i<numberOfSamples;i++)
        {
            double sample = random.nextGaussian() * rms;
            whiteNoise[i] = (byte) ((sample * 127.0) / rms);
        }

        mMediaPlayer1.setAuxEffectSendLevel(1.0f);
        int sessionId = mMediaPlayer1.getAudioSessionId();
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_8BIT,
                numberOfSamples,
                AudioTrack.MODE_STATIC,
                sessionId
        );

        mAudioTrack.write(whiteNoise,0,numberOfSamples);
        mAudioTrack.setLoopPoints(0,numberOfSamples / 2, -1);
        mAudioTrack.setVolume(mWhiteNoiseVolume);
        mAudioTrack.play();
    }

    void setWhiteNoiseVolume(float volume)
    {
        mWhiteNoiseVolume = volume;
        if (mAudioTrack != null)
        {
            mAudioTrack.setVolume(mWhiteNoiseVolume);
        }
    }

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
            startPairedBluetoothDeviceScan();
        }
        else if (v.getId() == R.id.measureStart)
        {
            startDeviceMeasure(mConnectedHandleID);
            mLoggingTrigger = true;
        }
        else if (v.getId() == R.id.measureStop)
        {
            stopDeviceMeasure();
            mLoggingTrigger = false;
        }
    }

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
