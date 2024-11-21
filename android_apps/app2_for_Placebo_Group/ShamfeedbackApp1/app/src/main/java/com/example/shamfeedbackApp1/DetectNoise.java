package com.example.shamfeedbackApp1;

public class DetectNoise
{
    double leftBrainValuePre1    = 0.0;
    double leftBrainValuePost1   = 0.0;
    double leftBrainValueChange1 = 0.0;  // 100ms毎の脳血流変化量1
    double leftBrainValuePre2    = 0.0;
    double leftBrainValuePost2   = 0.0;
    double leftBrainValueChange2 = 0.0;  // 100ms毎の脳血流変化量2

    double rightBrainValuePre1    = 0.0;
    double rightBrainValuePost1   = 0.0;
    double rightBrainValueChange1 = 0.0; // 100ms毎の脳血流変化量1
    double rightBrainValuePre2    = 0.0;
    double rightBrainValuePost2   = 0.0;
    double rightBrainValueChange2 = 0.0; // 100ms毎の脳血流変化量2

    double leftBrainChange  = 0.0; // 200ms毎の脳血流変化(leftBrainValueChange1 + leftBrainValueChange2)
    double rightBrainChange = 0.0; // 200ms毎の脳血流変化(rightBrainValueChange1 + rightBrainValueChange2)

    public DetectNoise()
    {

    }

    int detectMotionArtifact(double leftBrainValue, double rightBrainValue)
    {
        leftBrainValuePre1  = leftBrainValue;
        rightBrainValuePre1 = rightBrainValue;

        // 100ms毎の脳血流変化量1を計算
        leftBrainValueChange1  = leftBrainValuePre1 - leftBrainValuePost1;
        rightBrainValueChange1 = rightBrainValuePre1 - rightBrainValuePost1;

        // 脳血流データのスライド
        leftBrainValuePost1  = leftBrainValuePre1;
        rightBrainValuePost1 = rightBrainValuePre1;
        leftBrainValuePre2   = leftBrainValuePost1;
        rightBrainValuePre2  = rightBrainValuePost1;

        // 200ms毎の脳血流変化を計算
        leftBrainChange  = leftBrainValueChange1 + leftBrainValueChange2;
        rightBrainChange = rightBrainValueChange1 + rightBrainValueChange2;

        // 100ms毎の脳血流変化量2を計算
        leftBrainValueChange2  = leftBrainValuePre2 - leftBrainValuePost2;
        rightBrainValueChange2 = rightBrainValuePre2 - rightBrainValuePost2;

        // 脳血流データのスライド
        leftBrainValuePost2  = leftBrainValuePre2;
        rightBrainValuePost2 = rightBrainValuePre2;

        // 体動アーチファクトの判定(左脳血流)
        // 200ms間隔で0.2mMmmの変化があれば体動と判断する
        if (Math.abs(leftBrainChange) > 0.2)
        {
            return 1;
        }
        // 体動アーチファクトの判定(右脳血流)
        // 200ms間隔で0.2mMmmの変化があれば体動と判断する
        else if (Math.abs(rightBrainChange) > 0.2)
        {
            return 2;
        }

        else if (Math.abs(leftBrainChange) > 0.2 && Math.abs(rightBrainChange) > 0.2)
        {
            return 3;
        }

        return 0;
    }
}
