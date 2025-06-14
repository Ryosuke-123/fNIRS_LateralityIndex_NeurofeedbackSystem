package com.example.neurofeedbackApp1;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class NFLogger
{
    String         mFileName;
    String         mFolderName;
    File           file;
    String         suffix;
    BufferedWriter mBufferedWriter;
    boolean        mAppend;

    public boolean open(String fileName, String folderName, boolean append) // エラー箇所(2022/09/09)
    {
        if (fileName == null)
        {
            return false;
        }

        mAppend     = append;
        mFileName   = fileName;
        mFolderName = folderName;

        // 外部ストレージの存在確認
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
        {
            Log.d("TAG","there is no external storage");
            return false;
        }

        File filePath = new File(Environment.getExternalStorageDirectory(),mFolderName);
        boolean mkdir = filePath.mkdir();

        this.setSuffix();
        this.file = new File(filePath,fileName + this.suffix);

        try
        {
            this.mBufferedWriter = new BufferedWriter(new FileWriter(this.file,append)); // 一番のエラー箇所(2022/09/09)
        }
        catch (IOException e)
        {
            return false;
        }

        this.writeHeader();

        return true;
    }

    // ヘッダー書き込み
    public void flushLine(String line)
    {
        try
        {
            mBufferedWriter.write(line);
            mBufferedWriter.write("\r\n");
            mBufferedWriter.flush();
        }
        catch (IOException e)
        {

        }
    }

    public abstract void writeHeader();
    public abstract void setSuffix();
    public abstract void write();
}
