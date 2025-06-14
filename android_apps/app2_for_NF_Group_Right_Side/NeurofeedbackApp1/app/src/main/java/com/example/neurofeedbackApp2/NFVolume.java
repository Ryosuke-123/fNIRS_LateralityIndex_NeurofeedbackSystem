package com.example.neurofeedbackApp2;

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
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,8,0);
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
}
