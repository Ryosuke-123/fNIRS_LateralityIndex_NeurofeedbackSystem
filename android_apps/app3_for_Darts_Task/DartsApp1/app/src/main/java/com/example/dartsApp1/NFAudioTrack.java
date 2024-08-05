package com.example.dartsApp1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class NFAudioTrack
{
    AudioTrack mAudioTrack;

    int mSampleRate = 0;
    int mSampleRateMin = 4000;
    int mSampleRateMax = 96000;

    int preFreq  = 0; // 前の周波数データ
    int postFreq = 0; // 後の周波数データ

    public NFAudioTrack()
    {

    }

    void startAudioTrackReal(int time,int period,double liBrain)
    {
        postFreq = transBrainIndexFreq(liBrain);

        if (postFreq != preFreq)
        {
            // サンプルレートの算出
            mSampleRate  = calcSampleRate(postFreq,period,mSampleRateMin,mSampleRateMax);
            byte[] mData = createWaves(mSampleRate,time);

            // Audioコンストラクタ
            mAudioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    mSampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    mData.length,
                    AudioTrack.MODE_STATIC
            );

            // 再生完了のリスナー設定
            try
            {
                mAudioTrack.setNotificationMarkerPosition(mData.length);
                mAudioTrack.setPlaybackPositionUpdateListener(
                        new AudioTrack.OnPlaybackPositionUpdateListener() {
                            @Override
                            public void onMarkerReached(AudioTrack track)
                            {
                                if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                                {
                                    track.stop();
                                    track.release();
                                }
                            }

                            @Override
                            public void onPeriodicNotification(AudioTrack track)
                            {

                            }
                        }
                );

                // 波形データの書き込みと再生
                mAudioTrack.reloadStaticData();
                mAudioTrack.write(mData,0,mData.length);
                mAudioTrack.play();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                mAudioTrack.stop();
                mAudioTrack.release();
            }
        }
    }

    int transBrainIndexFreq(double liBrain)
    {
        int freq = 0;

        if (2.0 < liBrain)
        {
            freq = 600;
        }
        else if (1.6 < liBrain && liBrain <= 2.0)
        {
            freq = 550;
        }
        else if (1.2 < liBrain && liBrain <= 1.6)
        {
            freq = 500;
        }
        else if (0.8 < liBrain && liBrain <= 1.2)
        {
            freq = 450;
        }
        else if (0.4 < liBrain && liBrain <= 0.8)
        {
            freq = 400;
        }
        else if (0.0 < liBrain && liBrain <= 0.4)
        {
            freq = 350;
        }
        else if (-0.4 < liBrain && liBrain <= 0.0)
        {
            freq = 300;
        }
        else if (-0.8 < liBrain && liBrain <= -0.4)
        {
            freq = 250;
        }
        else if (-1.2 < liBrain && liBrain <= -0.8)
        {
            freq = 200;
        }
        else if (-1.6 < liBrain && liBrain <= -1.2)
        {
            freq = 150;
        }
        else if (-2.0 < liBrain && liBrain <= -1.6)
        {
            freq = 100;
        }
        else if (liBrain <= -2.0)
        {
            freq = 50;
        }

        return  freq;
    }

    int calcSampleRate(int freq,int period,int sampleRateMin,int sampleRateMax)
    {
        int sampleRate = freq * period;

        if (sampleRate < sampleRateMin)
        {
            sampleRate = sampleRateMin;
        }
        else if (sampleRate > sampleRateMax)
        {
            sampleRate = sampleRateMax;
        }

        return sampleRate;
    }

    // 波形データ生成
    // 音の波形 → 最小値: 0, 中央値: 128, 最大値: 255
    byte[] createWaves(int sampleRate,int time)
    {
        int dataNum = (int)((double)sampleRate + ((double)time / 1000.0));
        byte[] data = new byte[dataNum];
        int flag    = 0;

        for (int i=0;i<dataNum;i=i+2)
        {
            if (flag == 0)
            {
                data[i]   = (byte)0xff;
                data[i+1] = (byte)0xff;
                flag++;
            }
            else
            {
                data[i]   = (byte)0x00;
                data[i+1] = (byte)0x00;
                flag--;
            }
        }
        return data;
    }
}
