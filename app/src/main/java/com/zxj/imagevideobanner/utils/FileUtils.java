package com.zxj.imagevideobanner.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jay on 2018/6/4.
 */

public class FileUtils {

    public static InputStream readSdCardXml(String fileName){
        try {
            String path = Environment.getExternalStorageDirectory()+ File.separator + "imageVideo" + File.separator+fileName;
            Log.e("zxj","path="+path);
            File file = new File(path);
            FileInputStream is = new FileInputStream(file);
            return is;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OutputStream writeSdCardXml(String fileName){
        File fileDir = new File(Environment.getExternalStorageDirectory()+ File.separator + "imageVideo" + File.separator);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        File file = new File(fileDir,fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            return  fos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            sdpath = "不适用";
        }
        return sdpath;

    }

    /**
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
