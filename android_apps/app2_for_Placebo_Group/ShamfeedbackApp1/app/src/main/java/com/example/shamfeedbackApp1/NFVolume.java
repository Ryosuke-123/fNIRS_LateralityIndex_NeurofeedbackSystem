package com.example.shamfeedbackApp1;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class NFVolume
{
    public NFVolume()
    {

    }

    // Rest区間とRegulation区間の境目を合図するビープ音を鳴らす
    void trialBeepSound(AudioManager audioManager, MediaPlayer mediaPlayer)
    {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);
        mediaPlayer.start();
    }

    void startVolume(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
    }

    void stopVolume(MediaPlayer mediaPlayer)
    {
        // 音源ストップ & リセット
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    void startVolumeReal(double liBrain,AudioManager audioManager)
    {
        // LI値に対する音量の調整
        if (0.8 < liBrain && liBrain <= 1.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);
        }
        else if (0.6 < liBrain && liBrain <= 0.8)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,9,0);
        }
        else if (0.4 < liBrain && liBrain <= 0.6)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,8,0);
        }
        else if (0.2 < liBrain && liBrain <= 0.4)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,7,0);
        }
        else if (0.0 < liBrain && liBrain <= 0.2)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,6,0);
        }
        else if (-0.2 < liBrain && liBrain <= 0.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,5,0);
        }
        else if (-0.4 < liBrain && liBrain <= -0.2)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,4,0);
        }
        else if (-0.6 < liBrain && liBrain <= -0.4)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,3,0);
        }
        else if (-0.8 < liBrain && liBrain <= -0.6)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,2,0);
        }
        else if (-1.0 <= liBrain && liBrain <= -0.8)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,1,0);
        }
    }

    void startVolumeSham(double liBrain, AudioManager audioManager)
    {
        /*----------------------------- アルゴリズム -------------------------------*/
        // Math.random()*11                  → 0~10の整数をランダムに生成
        // (int)(Math.random()*11) - 5       → -5~5の整数をランダムに生成
        // ((int)(Math.random()*11) - 5)*0.1 → -0.5~0.5の値をランダムに生成

        double shamLiBrain = liBrain + (((int)(Math.random()*11) - 5)*0.1);

        if (2.0 < shamLiBrain)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,20,0);
        }
        else if (1.5 < shamLiBrain && shamLiBrain <= 2.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,18,0);
        }
        else if (1.0 < shamLiBrain && shamLiBrain <= 1.5)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,16,0);
        }
        else if (0.5 < shamLiBrain && shamLiBrain <= 1.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,14,0);
        }
        else if (0.0 < shamLiBrain && shamLiBrain <= 0.5)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,12,0);
        }
        else if (-0.5 < shamLiBrain && shamLiBrain <= 0.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);
        }
        else if (-1.0 < shamLiBrain && shamLiBrain <= -0.5)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,8,0);
        }
        else if (-1.5 < shamLiBrain && shamLiBrain <= -1.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,6,0);
        }
        else if (-2.0 < shamLiBrain && shamLiBrain <= -1.5)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,4,0);
        }
        else if (shamLiBrain <= -2.0)
        {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,2,0);
        }
    }

    void stopVolumeSham(MediaPlayer mediaPlayer)
    {
        // 音源ストップ & リセット
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
}
