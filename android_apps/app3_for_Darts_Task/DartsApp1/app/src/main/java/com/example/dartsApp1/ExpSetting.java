package com.example.dartsApp1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;

public class ExpSetting extends AppCompatActivity implements View.OnClickListener
{
    // 各パラメータ
    String preRestTime  = "";
    String focusTime    = "";
    String postRestTime = "";
    String trialNumber   = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exp_setting);

        // ボタンの設定
        findViewById(R.id.nextButton).setOnClickListener(this);

        // タイトルバーの削除
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
        {
            mActionBar.hide();
        }
    }

    @Override
    public void onClick(View v)
    {
        // スピナーの設定
        Spinner preRestTimeSpinner  = findViewById(R.id.preRestTimeSpinner);
        Spinner focusTimeSpinner    = findViewById(R.id.focusTimeSpinner);
        Spinner postRestTimeSpinner = findViewById(R.id.postRestTimeSpinner);
        Spinner trialValueSpinner   = findViewById(R.id.trialValueSpinner);

        int preRestTimeItem  = preRestTimeSpinner.getSelectedItemPosition();
        int focusTimeItem    = focusTimeSpinner.getSelectedItemPosition();
        int postRestTimeItem = postRestTimeSpinner.getSelectedItemPosition();
        int trialValueItem   = trialValueSpinner.getSelectedItemPosition();

        preRestTime    = (String) preRestTimeSpinner.getSelectedItem();
        focusTime      = (String) focusTimeSpinner.getSelectedItem();
        postRestTime   = (String) postRestTimeSpinner.getSelectedItem();
        trialNumber    = (String) trialValueSpinner.getSelectedItem();

        if (v.getId() == R.id.nextButton)
        {
            // 実験条件の設定が完了したら、Nextボタンを有効化する
            if (preRestTimeItem !=0 && focusTimeItem != 0 && postRestTimeItem !=0)
            {
                final Intent intent = new Intent(this, StartExp.class);

                intent.putExtra("preRestTime",preRestTime);
                intent.putExtra("focusTime",focusTime);
                intent.putExtra("postRestTime",postRestTime);
                intent.putExtra("trialNumber",trialNumber);

                startActivity(intent);
            }
        }
    }
}
