package com.zxj.imagevideobanner.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.zxj.imagevideobanner.R;
import com.zxj.imagevideobanner.bean.BannerBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jay on 2018/5/28.
 */

public class ImageVideoBanner extends FrameLayout implements  ViewPager.OnPageChangeListener, ImageVideoFragment.OnVideoCompletionListener {
    private static final int UPTATE_VIEWPAGER = 0;
    private ViewPager mViewPager;
    private List<BannerBean> mList = new ArrayList<>();
    private ViewsPagerAdapter mAdapter;
    private int autoCurrIndex = 0;//设置当前 第几个图片 被选中
    private Timer timer;
    private TimerTask timerTask;
    private long period;//轮播图展示时长,默认5秒
    private int mCurrentPosition;
    private Context context;

    public ImageVideoBanner(@NonNull Context context) {
        super(context);
        this.context = context;
        initView(context);
    }

    public ImageVideoBanner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.banner);
        period = typedArray.getInt(R.styleable.banner_period,5000);
        typedArray.recycle();
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_imge_video, this, true);
        mViewPager = view.findViewById(R.id.view_pager);
        mAdapter = new ViewsPagerAdapter(((FragmentActivity)context).getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        //ViewPager手势滑动禁用
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void replaceData(List<BannerBean> listBean){
        mAdapter.replaceData(listBean);
    }

    public void addData(List<BannerBean> listBean){
        mAdapter.addData(listBean);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        autoCurrIndex = position;
//        Toast.makeText(context,"position="+position,Toast.LENGTH_SHORT).show();
        if(mList.get(position).getType() ==1){
            //如果是视频
            stopBanner();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //定时轮播图片，需要在主线程里面修改 UI
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPTATE_VIEWPAGER:
                    if (msg.arg1 != 0) {
                        mViewPager.setCurrentItem(msg.arg1);
                    } else {
                        //false 当从末页调到首页时，不显示翻页动画效果，
                        mViewPager.setCurrentItem(msg.arg1, false);
                    }
                    break;
            }
        }
    };

    public void stopBanner(){
        if (timer != null) {
            timer.cancel();
            timer = null;
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
        }
    }
    public void startBanner(){
        startBanner(period);
    }

    public void startBanner(long delay){
        stopBanner();
        timer = new Timer();
        createTimerTask();//创建定时器
        timer.schedule(timerTask,delay,period);
    }

    public void createTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = UPTATE_VIEWPAGER;
                if (autoCurrIndex == mList.size() - 1) {
                    autoCurrIndex = -1;
                }
                message.arg1 = autoCurrIndex + 1;
                mHandler.sendMessage(message);
            }
        };
    }

    @Override
    public void onVideoCompletion(MediaPlayer mp) {
        if(mList.size() ==1){
            //当集合中只有一个视频时，循环播放
            mAdapter.getFragment().circulationPlayer();
        }
        startBanner(0);
    }

    @Override
    public void onError(MediaPlayer mp) {
        startBanner(0);
    }

    private final class ViewsPagerAdapter extends FragmentStatePagerAdapter{
        private ImageVideoFragment fragment;
        private FragmentManager fm;
        public ViewsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            fragment = new ImageVideoFragment();
            fragment.setOnVideoCompletionListener(ImageVideoBanner.this);
            BannerBean bannerBean = mList.get(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("bannerBean",bannerBean);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mList.size() > 0) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        public void replaceData(List<BannerBean> listBean){
            if(null != listBean){
                mList.clear();
                addData(listBean);
            }
        }

        public void addData(List<BannerBean> listBean){
            if(null != listBean){
                mList.addAll(listBean);
            }
            notifyDataSetChanged();
        }

        public ImageVideoFragment getFragment() {
            return fragment;
        }
    }
}
