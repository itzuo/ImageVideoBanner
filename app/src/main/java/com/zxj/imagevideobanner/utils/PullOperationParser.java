package com.zxj.imagevideobanner.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.zxj.imagevideobanner.bean.BannerBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by itservice on 2017/9/14.
 */

public class PullOperationParser implements OperationParser {
    private Context context;
    public PullOperationParser(Context context){
        this.context = context;
    }
    @Override
    public List<BannerBean> parse(InputStream is) throws Exception {
        List<BannerBean> bannerBeans = null;
        BannerBean bannerBean = null;
        XmlPullParser parser = Xml.newPullParser();	//由android.util.Xml创建一个XmlPullParser实例
        parser.setInput(is, "UTF-8");				//设置输入流 并指明编码方式

        int eventType = parser.getEventType();
        Log.e("zxj","eventType="+eventType);
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    bannerBeans = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    if("imageVideo".equals(name)){
                        bannerBean = new BannerBean();
                    }else if("url".equals(name)){
                        String path = parser.nextText();
                         if(!path.startsWith("http")){
                            path = Environment.getExternalStorageDirectory() + File.separator + path;
                        }
                        bannerBean.setUrl(path);
                    }else if("type".equals(name)){
                        bannerBean.setType(Integer.parseInt(parser.nextText()));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if("imageVideo".equals(name)){
                        bannerBeans.add(bannerBean);
                        bannerBean = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return bannerBeans;
    }

    @Override
    public String serialize(List<BannerBean> bannerBeans,OutputStream os) throws Exception {
        XmlSerializer serializer = Xml.newSerializer();	//由android.util.Xml创建一个XmlSerializer实例
        StringWriter writer = new StringWriter();
        serializer.setOutput(os,"UTF-8");
//        serializer.setOutput(writer);	//设置输出方向为writer
        serializer.startDocument("UTF-8", true);
        serializer.text("\n");
        serializer.startTag("", "imageVideos");
        for(BannerBean bannerBean :bannerBeans){
            serializer.text("\n\t");
            serializer.startTag("","imageVideo");
            serializer.text("\n\t\t");
            serializer.startTag("","url");
            serializer.text(bannerBean.getUrl());
            serializer.endTag("","url");
            serializer.text("\n\t\t");

            serializer.startTag("","type");
            serializer.text(String.valueOf(bannerBean.getType()));
            serializer.endTag("","type");
            serializer.text("\n\t");

            serializer.endTag("","imageVideo");
            serializer.text("\n");
        }
        serializer.endTag("","imageVideos");
        serializer.endDocument();
        os.flush();
        os.close();
        return null;
    }
}
