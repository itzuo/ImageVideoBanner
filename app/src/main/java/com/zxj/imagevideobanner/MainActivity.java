package com.zxj.imagevideobanner;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zxj.imagevideobanner.banner.ImageVideoBanner;
import com.zxj.imagevideobanner.bean.BannerBean;
import com.zxj.imagevideobanner.utils.FileUtils;
import com.zxj.imagevideobanner.utils.PullOperationParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<BannerBean> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageVideoBanner banner = findViewById(R.id.banner);

        Log.e("zxj", Environment.getExternalStorageDirectory()+ File.separator+"test/mov_bbb.mp4");
        List<BannerBean> bannerBeans = getDatas();
        if (null == bannerBeans) {
            setDatas();
            bannerBeans = getDatas();
        }
        banner.replaceData(bannerBeans);
        banner.startBanner();

    }

    private void setDatas() {
        for (int i = 0; i < 2; i++) {
            BannerBean listBean = new BannerBean();
            if (i == 1 ) {
                listBean.setUrl("imageVideo/mov_bbb.mp4");
//                listBean.setUrl("http://www.w3school.com.cn/example/html5/mov_bbb.mp4");
//                listBean.setUrl("https://media.w3.org/2010/05/sintel/trailer.mp4");
                listBean.setType(1);//图片类型 视频
                list.add(listBean);
            } else if(i==2){
                listBean.setUrl("http://pic11.nipic.com/20101201/4452735_182232064453_2.jpg");
                listBean.setType(0);//图片类型 视频
                list.add(listBean);
            }else if(i == 3){
                listBean.setUrl("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
                listBean.setType(1);//图片类型 视频
                list.add(listBean);
            }else {
                listBean.setUrl("http://img.zcool.cn/community/01635d571ed29832f875a3994c7836.png@900w_1l_2o_100sh.jpg");
                listBean.setType(0);//图片类型 图片
                list.add(listBean);
            }
        }

        try {
            OutputStream os = FileUtils.writeSdCardXml("image_video.xml");
            PullOperationParser parser = new PullOperationParser(MainActivity.this);
            parser.serialize(list, os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BannerBean> getDatas() {
        try {
            InputStream is = FileUtils.readSdCardXml("image_video.xml");
            PullOperationParser parser = new PullOperationParser(MainActivity.this);
            List<BannerBean> bannerBeans = parser.parse(is);
            return bannerBeans;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
