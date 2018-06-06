package com.zxj.imagevideobanner.banner;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.zxj.imagevideobanner.R;
import com.zxj.imagevideobanner.bean.BannerBean;

/**
 * Created by jay on 2018/6/5.
 */

public class ImageVideoFragment extends Fragment {

    private static final String TAG = ImageVideoFragment.class.getSimpleName();
    private OnVideoCompletionListener listener;
    private VideoView mVideoView;
    private BannerBean bannerBean;
    private FrameLayout waitLoading;
    private int currentPosition;
    private boolean playerPaused;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        bannerBean = (BannerBean) bundle.getSerializable("bannerBean");
        Log.e(TAG, "type=" + bannerBean.getType() + ",url=" + bannerBean.getUrl());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (bannerBean != null) {
            int type = bannerBean.getType();
            if (type == 0) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_view, container, false);
                ImageView imageView = view.findViewById(R.id.iv);
                Glide.with(container.getContext()).load(bannerBean.getUrl())
                        .into(imageView);
            } else {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_video_view, container, false);
                mVideoView = view.findViewById(R.id.video_view);
                waitLoading = view.findViewById(R.id.wait_loading_layout);
            }
        } else {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_view, container, false);
        }
        initData();
        return view;
    }

    private void initData() {

        if (null != mVideoView) {
//            mVideoView.setVideoPath(bannerBean.getUrl());
            mVideoView.setVideoURI(Uri.parse(bannerBean.getUrl()));
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVideoView.stopPlayback();
                    if (null != listener) {
                        listener.onVideoCompletion(mp);
                    }
                }
            });

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e(TAG, "视频加载完成" + bannerBean.getUrl());
                    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            waitLoading.setVisibility(View.GONE);
                            mVideoView.setVisibility(View.VISIBLE);
                            /*if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                waitLoading.setVisibility(View.GONE);
                                mVideoView.setVisibility(View.VISIBLE);
                                return true;
                            }*/
                            return false;
                        }
                    });
                }
            });

            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "视频播放出错了-what="+what+",extra="+extra);
                    mVideoView.stopPlayback();
                    if(null != listener){
                        listener.onError(mp);
                    }
                    if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                        //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                        Log.e(TAG, "媒体服务器挂掉了");
                    }else if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
                        if(extra == MediaPlayer.MEDIA_ERROR_IO){
                            //文件不存在或错误，或网络不可访问错误
                            Log.e(TAG, "文件不存在或错误，或网络不可访问错误");
                        }else if(extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT){
                            //超时
                            Log.e(TAG, "超时");
                        }
                    }
                    return true;
                }
            });
        }
        startPlayer();
    }

    public void startPlayer() {
        if (null != mVideoView) {
            mVideoView.seekTo(currentPosition);
            mVideoView.start();
        }
    }

    public void circulationPlayer(){
        if (null != mVideoView) {
            mVideoView.setVideoPath(bannerBean.getUrl());
            mVideoView.start();
        }
    }

    private void stopPlayer() {
        if (null != mVideoView) {
            mVideoView.stopPlayback();
        }
    }

    public boolean isPlaying(){
        if(null != mVideoView){
            return mVideoView.isPlaying();
        }
        return false;
    }

    private void pausePlayer() {
        if (null != mVideoView) {
            playerPaused = true;
            this.currentPosition = mVideoView.getCurrentPosition();
            mVideoView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(playerPaused){
            startPlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
        Log.e(TAG, "onDestroy=" + bannerBean.getUrl());
    }
    public interface OnVideoCompletionListener {
        void onVideoCompletion(MediaPlayer mp);
        void onError(MediaPlayer mp);
    }

    public void setOnVideoCompletionListener(OnVideoCompletionListener listener) {
        this.listener = listener;
    }
}
