package com.estsoft.muvicam.ui.editor.result;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.editor.ResultBarView;
import com.estsoft.muvicam.ui.editor.VideoPlayerTextureView;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/*
 * will show edit view
  * */
public class VideoEditorResultFragment extends Fragment {
    String TAG = "VideoEditorResultF";
    public final static String EXTRA_FRAGMENT_NUM = "VideoEditorResultFragment.fragmentNum";
    public final static String EXTRA_VIDEOS = "VideoEditorResultFragment.videoList";
    public final static String EXTRA_RESULT_VIDEOS = "VideoEditorResultFragment.resultVideoList";
    public final static String EXTRA_RESULT_VIDEO_TOTAL_TIME = "VideoEditorResultFragment.resultVideoTotalTime";
    public final static String EXTRA_MUSIC_PATH = "VideoEditorResultFragment.musicPath";
    public final static String EXTRA_MUSIC_OFFSET = "VideoEditorResultFragment.musicOffset";
    public final static String EXTRA_MUSIC_LENGTH = "VideoEditorResultFragment.musicLength";
    RecyclerView selectedVideoButtons;
    ImageView deleteButton;
    LinearLayout linearResultSpace, editorResultBlackScreen;
    ResultBarView resultBarView;
    EditorResultMediaPlayer videoResultPlayer, videoResultPlayer2, musicResultPlayer;
    VideoPlayerTextureView videoResultTextureView, videoResultTextureView2;
    FrameLayout videoSpaceFrameLayout;
    boolean flag = true;
    ProgressBar resultProgressBar;
    private ArrayList<EditorVideo> resultVideos = new ArrayList<>(), selectedVideos = new ArrayList<>();
    String musicPath;
    int musicOffset, musicLength;
    int resultVideosTotalTime;
    int nowVideoNum;
    DataPassListener mCallBack;
    VideoEditSelectedNumberAdapter.OnItemClickListener itemClickListener = new VideoEditSelectedNumberAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {

            Log.d(TAG, "onCreateView: resultVideosTotalTime6" + resultVideosTotalTime);
            int remainTime = 15000 - resultVideosTotalTime;
            if (remainTime < 1000) {
                Toast.makeText(getContext(), "can not add video less than 1 second", Toast.LENGTH_SHORT).show();
            } else {
                flag = false;
                if (videoResultPlayer.isPlaying()) {
                    videoResultPlayer.pause();
                    videoResultPlayer.stop();
                    videoResultPlayer.release();
                }
                if (videoResultPlayer2.isPlaying()) {
                    videoResultPlayer2.pause();
                    videoResultPlayer2.stop();
                    videoResultPlayer2.release();
                }
                if (musicResultPlayer.isPlaying()) {
                    musicResultPlayer.pause();
                    musicResultPlayer.stop();
                    musicResultPlayer.release();
                }
                mCallBack.passDataFToF(position + 1, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
            }
        }
    };


    public interface DataPassListener {
        void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos, int resultTotalTime, String musicPath, int musicOffset, int musicLength);
    }

    public VideoEditorResultFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VideoEditorResultFragment newInstance() {
        VideoEditorResultFragment fragment = new VideoEditorResultFragment();
        Bundle args = new Bundle();
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS, selectedVideos);
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS, resultEditorVideos);
//        args.putInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME,resultVideosTotalTime);
//        args.putString(VideoEditorResultFragment.EXTRA_MUSIC_PATH, musicPath);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, musicOffset);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, musicLength);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            selectedVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS);
            resultVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS);
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, 0);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);

        }
        nowVideoNum = 0;
        videoResultPlayer = new EditorResultMediaPlayer(getActivity(), 0, true, false);
        videoResultPlayer2 = new EditorResultMediaPlayer(getActivity(), 0, false, false);
        musicResultPlayer = new EditorResultMediaPlayer(getActivity(), 0, true, true);
        if (resultVideos.size() > 0) {
            try {
                FileInputStream fisMusic = new FileInputStream(musicPath);
                FileDescriptor fdMusic = fisMusic.getFD();
                musicResultPlayer.setDataSource(fdMusic);
                musicResultPlayer.setOffset(musicOffset);
                videoResultPlayer.setVolume(0, 0);
                videoResultPlayer2.setVolume(0, 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        resultThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_editor_result, container, false);
        videoSpaceFrameLayout = (FrameLayout) v.findViewById(R.id.editor_result_frame_layout);
        selectedVideoButtons = (RecyclerView) v.findViewById(R.id.editor_result_buttons);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        selectedVideoButtons.setLayoutManager(linearLayoutManager);
        deleteButton = (ImageView) v.findViewById(R.id.editor_result_delete);
        linearResultSpace = (LinearLayout) v.findViewById(R.id.editor_result_space_linear);
        resultProgressBar = (ProgressBar) v.findViewById(R.id.editor_result_progress);
        Log.d(TAG, "onCreateView: resultVideosTotalTime1" + resultVideosTotalTime);
        resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
        linearResultSpace.addView(resultBarView);
        editorResultBlackScreen = (LinearLayout) v.findViewById(R.id.editor_result_black_screen);
        if (resultVideosTotalTime > 0) {
            Log.d(TAG, "onCreateView: resultVideosTotalTime2" + resultVideosTotalTime);
            deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
            //    deleteButton.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoEditSelectedNumberAdapter videoEditSelectedNumberAdapter = new VideoEditSelectedNumberAdapter(getActivity(), selectedVideos, itemClickListener);

        selectedVideoButtons.setAdapter(videoEditSelectedNumberAdapter);

        if (resultVideos.size() > 0) {
            try {
                if (resultVideos.size() > 1) {
                    MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
                    FileInputStream fis2 = new FileInputStream(resultVideos.get(1).getVideoPath());
                    FileDescriptor fd2 = fis2.getFD();
                    videoResultPlayer2.setDataSource(fd2);
                    videoResultPlayer2.setOffset(resultVideos.get(1).getStart());
                    retriever2.setDataSource(fd2);
                    int width2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int height2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    int rotation2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                    videoResultTextureView2 = new VideoPlayerTextureView(getActivity(), videoResultPlayer2, resultVideos.get(1), width2, height2, rotation2);
                    videoSpaceFrameLayout.addView(videoResultTextureView2);
                    fis2.close();

                }
                MediaMetadataRetriever retriever1 = new MediaMetadataRetriever();
                FileInputStream fis = new FileInputStream(resultVideos.get(0).getVideoPath());
                FileDescriptor fd = fis.getFD();
                videoResultPlayer.setDataSource(fd);
                videoResultPlayer.setOffset(resultVideos.get(0).getStart());
                retriever1.setDataSource(fd);
                int width1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int height1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                int rotation1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                videoResultTextureView = new VideoPlayerTextureView(getActivity(), videoResultPlayer, resultVideos.get(0), width1, height1, rotation1);
                musicResultPlayer.prepare();
                resultProgressBar.bringToFront();
                videoSpaceFrameLayout.addView(videoResultTextureView);
                fis.close();
            } catch (IOException io) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                io.printStackTrace(pw);
                Log.d(TAG, "editadaptor exception" + sw.toString());
            }

        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditorVideo removedVideo = resultVideos.get(resultVideos.size() - 1);
                resultVideosTotalTime = resultVideosTotalTime - (removedVideo.getEnd() - removedVideo.getStart());
                Log.d(TAG, "onCreateView: resultVideosTotalTime3" + resultVideosTotalTime);
                resultVideos.remove(resultVideos.get(resultVideos.size() - 1));
                linearResultSpace.removeView(resultBarView);
                resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
                Log.d(TAG, "onCreateView: resultVideosTotalTime4" + resultVideosTotalTime);
                linearResultSpace.addView(resultBarView);
                if (resultVideos.size() > 0) {
                    deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
                    Log.d(TAG, "onCreateView: resultVideosTotalTime5" + resultVideosTotalTime);
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
            }
        });

        videoSpaceFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag && !musicResultPlayer.isPlaying()) {
                    nowVideoNum = 0;
                    deleteButton.setVisibility(View.GONE);
                    if (resultVideos.size() > 0) {
                        try {
                            if (resultVideos.size() > 1) {
                                MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
                                FileInputStream fis2 = new FileInputStream(resultVideos.get(1).getVideoPath());
                                FileDescriptor fd2 = fis2.getFD();
                                videoResultPlayer2.reset();
                                videoResultPlayer2.setDataSource(fd2);
                                videoResultPlayer2.setOffset(resultVideos.get(1).getStart());
                                retriever2.setDataSource(fd2);
                                int width2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                int height2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                int rotation2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                                videoResultTextureView2 = new VideoPlayerTextureView(getActivity(), videoResultPlayer2, resultVideos.get(1), width2, height2, rotation2);
                                videoSpaceFrameLayout.addView(videoResultTextureView2);
                                fis2.close();

                            }
                            MediaMetadataRetriever retriever1 = new MediaMetadataRetriever();
                            FileInputStream fis = new FileInputStream(resultVideos.get(0).getVideoPath());
                            FileDescriptor fd = fis.getFD();
                            videoResultPlayer.reset();
                            videoResultPlayer.setDataSource(fd);
                            videoResultPlayer.setOffset(resultVideos.get(0).getStart());
                            videoResultPlayer.setFirst(true);
                            retriever1.setDataSource(fd);
                            int width1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            int height1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                            int rotation1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                            videoResultTextureView = new VideoPlayerTextureView(getActivity(), videoResultPlayer, resultVideos.get(0), width1, height1, rotation1);
                            musicResultPlayer.start();
                            resultProgressBar.bringToFront();
                            videoSpaceFrameLayout.addView(videoResultTextureView);
                            fis.close();
                        } catch (IOException io) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            io.printStackTrace(pw);
                            Log.d(TAG, "editadaptor exception" + sw.toString());
                        }

                    }
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallBack = (DataPassListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataPassListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (videoResultPlayer != null) {
            if (flag && videoResultPlayer.isPlaying()) {
                videoResultPlayer.pause();
                videoResultPlayer.stop();
            }
            videoResultPlayer.release();
        }
        if (videoResultPlayer2 != null) {
            if (flag && videoResultPlayer2.isPlaying()) {
                videoResultPlayer2.pause();
                videoResultPlayer2.stop();
            }
            videoResultPlayer2.release();
        }
        if (musicResultPlayer != null) {
            if (flag && musicResultPlayer.isPlaying()) {
                musicResultPlayer.pause();
                musicResultPlayer.stop();
            }
            musicResultPlayer.release();
        }
        nowVideoNum = 0;
        flag = false;
    }

    private float deleteButtonLocation(float resultVideosTotalTime) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float widthPSec = (float) outMetrics.widthPixels / 15;
        int dpi = outMetrics.densityDpi / 160;
        return (resultVideosTotalTime / 1000) * widthPSec - 20 * dpi;
    }

    Thread resultThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (flag) {
                try {
                    if (flag && musicResultPlayer.isPlaying()) {
                        int progress = musicResultPlayer.getCurrentPosition() / 150;
                        resultProgressBar.setProgress(progress);
                        resultThread.sleep(50);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultProgressBar.setVisibility(View.VISIBLE);
                            }
                        });
                        if (nowVideoNum % 2 == 0 && flag && videoResultPlayer.isPlaying()) {
                            Log.d(TAG, "run first: " + nowVideoNum + " / " + resultVideos.size());
                            if (videoResultPlayer.getCurrentPosition() >= resultVideos.get(nowVideoNum).getEnd()) {
                                videoResultPlayer.pause();
                                videoResultPlayer.stop();
                                nowVideoNum = nowVideoNum + 1;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //here?
                                        if (!videoResultPlayer2.isPlaying() && nowVideoNum == 0) {
                                            editorResultBlackScreen.bringToFront();
                                            linearResultSpace.bringToFront();
                                        } else {
                                            videoResultTextureView2.bringToFront();
                                        }
                                    }
                                });

                                if (nowVideoNum < resultVideos.size()) {
                                    Log.d(TAG, "run sec: " + nowVideoNum + " / " + resultVideos.size());
                                    videoResultPlayer2.start();

                                    Log.d(TAG, "run third: " + nowVideoNum + " / " + resultVideos.size());
                                    if (nowVideoNum + 1 < resultVideos.size()) {
                                        videoResultPlayer.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer, nowVideoNum + 1, true);
                                    }

                                } else {
                                    nowVideoNum = 0;
                                    videoResultPlayer.setFirst(false);
                                    prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);

                                    if (nowVideoNum + 1 < resultVideos.size()) {
                                        videoResultPlayer2.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                    }
                                }

                            }
                        } else if (nowVideoNum % 2 == 1 && flag && videoResultPlayer2.isPlaying()) {

                            if (videoResultPlayer2.getCurrentPosition() >= resultVideos.get(nowVideoNum).getEnd()) {
                                videoResultPlayer2.pause();
                                videoResultPlayer2.stop();
                                nowVideoNum = nowVideoNum + 1;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!videoResultPlayer.isPlaying() && nowVideoNum == 0) {
                                            editorResultBlackScreen.bringToFront();
                                            linearResultSpace.bringToFront();
                                        } else {
                                            videoResultTextureView.bringToFront();
                                        }
                                    }
                                });

                                if (nowVideoNum < resultVideos.size()) {
                                    videoResultPlayer.start();
                                    if (nowVideoNum + 1 < resultVideos.size()) {
                                        videoResultPlayer2.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                    }


                                } else {
                                    nowVideoNum = 0;
                                    videoResultPlayer.setFirst(false);
                                    prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);

                                    //always at least 2 videos
                                    videoResultPlayer2.setFirst(false);
                                    prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.getStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Log.d(TAG, "thread got exception :\n" + sw.toString());
                }
            }
        }
    });

    private void prepareVideoPlayer(EditorResultMediaPlayer mediaPlayer, int videoNum, boolean isPlayer1) {

        try {

            EditorVideo video = resultVideos.get(videoNum);
            FileInputStream fis = new FileInputStream(video.getVideoPath());
            FileDescriptor fd = fis.getFD();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fd);
            mediaPlayer.setOffset(resultVideos.get(videoNum).getStart());
            mediaPlayer.prepare();
            if (isPlayer1) {
                videoResultTextureView.setResultVideo(video);
            } else {
                videoResultTextureView2.setResultVideo(video);
            }
            fis.close();

        } catch (FileNotFoundException ex) {
            ex.getStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            Log.d(TAG, "prepare got exception :\n" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
