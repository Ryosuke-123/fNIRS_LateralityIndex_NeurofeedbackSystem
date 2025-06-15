package com.example.neurofeedbackApp2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;

public class ExpSetting extends AppCompatActivity implements View.OnClickListener
{
    // 各パラメータ
    String restTime  = "";
    String baselineTime = "";
    String movingAvgTime      = "";
    String trainingTime = "";

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
        Spinner restTimeSpinner      = findViewById(R.id.restTimeSpinner);
        Spinner baselineTimeSpinner  = findViewById(R.id.baselineTimeSpinner);
        Spinner movingAvgTimeSpinner = findViewById(R.id.movingAvgTimeSpinner);
        Spinner trainingTimeSpinner  = findViewById(R.id.trainingTimeSpinner);

        int restTimeItem      = restTimeSpinner.getSelectedItemPosition();
        int baselineTimeItem  = baselineTimeSpinner.getSelectedItemPosition();
        int movingAvgTimeItem = movingAvgTimeSpinner.getSelectedItemPosition();
        int trainingTimeItem  = trainingTimeSpinner.getSelectedItemPosition();

        restTime      = (String) restTimeSpinner.getSelectedItem();
        baselineTime  = (String) baselineTimeSpinner.getSelectedItem();
        movingAvgTime = (String) movingAvgTimeSpinner.getSelectedItem();
        trainingTime  = (String) trainingTimeSpinner.getSelectedItem();

        if (v.getId() == R.id.nextButton)
        {
            // 実験条件の設定が完了したら、Nextボタンを有効化する
            if (restTimeItem !=0 && baselineTimeItem != 0 && movingAvgTimeItem !=0 && trainingTimeItem !=0)
            {
                final Intent intent = new Intent(this, StartExp.class);

                intent.putExtra("restTime",restTime);
                intent.putExtra("baselineTime",baselineTime);
                intent.putExtra("movingAvgTime",movingAvgTime);
                intent.putExtra("trainingTime",trainingTime);

                startActivity(intent);
            }
        }
    }
}
