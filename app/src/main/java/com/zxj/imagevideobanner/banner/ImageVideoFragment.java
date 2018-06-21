package com.zxj.imagevideobanner.banner;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
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
    private String mUrl;
    private final int STOP_PLAYER = 0x2000;
    private final int START_PLAYER = 0x2001;
    private final int PAUSE_PLAYER = 0x2002;
    private final int SET_VIDEO_URL = 0x2003;

    /**
     * 使用Handler是为了避免出现ANR异常
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_PLAYER:
                    stopPlayer();
                    break;
                case START_PLAYER:
                    startPlayer();
                    break;
                case PAUSE_PLAYER:
                    pausePlayer();
                    break;
                case SET_VIDEO_URL:
                    setVideoUrl();
                    startPlayer();
                    break;
            }
            super.handleMessage(msg);
        }
    };

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
                initData();
            }
        } else {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_view, container, false);
        }
        return view;
    }

    private void initData() {

        if (null != mVideoView) {
//            mVideoView.setVideoPath(bannerBean.getUrl());
            sendSetVideoUrlMsg();
            mVideoView.setMediaController(new MediaController(getActivity()));
            mVideoView.requestFocus();
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
//            startPlayer();
        }
    }

    private void setVideoUrl() {
        String url = bannerBean.getUrl();
        mUrl = url;
        //播放本地视频
        mVideoView.setVideoURI(Uri.parse(url));
    }

    public void startPlayer() {
        if (null != mVideoView) {
            mVideoView.seekTo(currentPosition);
            mVideoView.start();
        }
    }

    public void circulationPlayer(){
        /*if (null != mVideoView) {
            mVideoView.setVideoPath(bannerBean.getUrl());
            mVideoView.start();
        }*/
        sendStartVideoMsg(true);
    }

    private void stopPlayer() {
        if (null != mVideoView) {
            mVideoView.stopPlayback();
            handler.removeCallbacksAndMessages(null);
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

    private void sendStartVideoMsg() {
        sendStartVideoMsg(false);
    }

    private void sendStartVideoMsg(boolean isHasUrl) {
        removeMessages();
        if (!handler.hasMessages(START_PLAYER)) {
            if (null != mVideoView) {
                if (isHasUrl) {
                    try {
                        mVideoView.setVideoURI(Uri.parse(mUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(START_PLAYER);
            }
        }
    }

    private void sendStopVideoMsg() {
        removeMessages();
        if (!handler.hasMessages(STOP_PLAYER)) {
            if (null != mVideoView) {
                handler.sendEmptyMessage(STOP_PLAYER);
            }
        }
    }

    private void sendPauseVideoMsg() {
        removeMessages();
        if (!handler.hasMessages(PAUSE_PLAYER)) {
            if (null != mVideoView) {
                handler.sendEmptyMessage(PAUSE_PLAYER);
            }
        }
    }

    private void sendSetVideoUrlMsg() {
        removeMessages();
        if (!handler.hasMessages(SET_VIDEO_URL)) {
            if (null != mVideoView) {
                Log.e(TAG, "sendSetVideoUrlMsg------");
                handler.sendEmptyMessage(SET_VIDEO_URL);
            }
        }
    }

    private void removeMessages() {
        if (handler.hasMessages(START_PLAYER)) {
            handler.removeMessages(START_PLAYER);
        }
        if (handler.hasMessages(STOP_PLAYER)) {
            handler.removeMessages(STOP_PLAYER);
        }
        if (handler.hasMessages(PAUSE_PLAYER)) {
            handler.removeMessages(PAUSE_PLAYER);
        }
        if (handler.hasMessages(SET_VIDEO_URL)) {
            handler.removeMessages(SET_VIDEO_URL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(playerPaused){
//            startPlayer();
            sendStartVideoMsg();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        pausePlayer();
        sendPauseVideoMsg();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopPlayer();
        sendStopVideoMsg();
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
