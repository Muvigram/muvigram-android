package com.estsoft.muvicam.ui.home.camera;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.util.RxUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jaylim on 12/17/2016.
 */

public class MusicCutFragment extends Fragment {

  private static final String ARG_MUSIC = "MusicCutFragment.arg_music";
  private static final String ARG_OFFSET = "MusicCutFragment.arg_offset";

  public static MusicCutFragment newInstance(Uri uri, int offset) {
    MusicCutFragment fragment = new MusicCutFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MUSIC, uri);
    args.putInt(ARG_OFFSET, offset);
    fragment.setArguments(args);
    return fragment;
  }

  // Millisecond
  private int mOffset;
  private int mTempOffset;

  private CameraFragment mParentFragment;

  Unbinder mUnbinder;

  @BindView(R.id.music_cut_complete_button)
  ImageButton mCompleteButton;

  @BindView(R.id.music_cut_waveform)
  WaveformView mWaveformView;

  @OnClick(R.id.music_cut_complete_button)
  public void _completeMusicCut() {
    mOffset = mTempOffset;
    FragmentManager pcfm = mParentFragment.getChildFragmentManager();
    Fragment fragment = pcfm.findFragmentById(R.id.camera_container_music_cut);

    pcfm.beginTransaction()
        .remove(fragment)
        .commit();
    mParentFragment.requestUiChange(CameraFragment.UI_LOGIC_FINISH_CUT_MUSIC);
    mParentFragment.cutMusic(mOffset);
  }

  public void _cancelMusicCut() {
    FragmentManager pcfm = mParentFragment.getChildFragmentManager();
    Fragment fragment = pcfm.findFragmentById(R.id.camera_container_music_cut);

    pcfm.beginTransaction()
        .remove(fragment)
        .commit();
    mParentFragment.requestUiChange(CameraFragment.UI_LOGIC_FINISH_CUT_MUSIC);
    mParentFragment.cutMusic(mOffset);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mParentFragment = ((CameraFragment) getParentFragment());
    ((HomeActivity) getActivity()).setCuttingVideo(true);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_cut, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Uri uri = getArguments().getParcelable(ARG_MUSIC);
    mOffset = getArguments().getInt(ARG_OFFSET);
    mWaveformView.setSoundFile(uri, millisecToSec(mOffset));
    mWaveformView.setListener(mWaveformListener);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    mParentFragment.stopSubscribePlayer();
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    ((HomeActivity) getActivity()).setCuttingVideo(false);
    super.onDestroy();
  }

  Subscription mSubscription;

  public WaveformView.WaveformListener mWaveformListener = new WaveformView.WaveformListener() {
    private float mXStart;

    @Override
    public void waveformTouchStart(float x) {
      mParentFragment.stopSubscribePlayer();
      mXStart = x;
    }

    @Override
    public void waveformTouchMove(float x) {
      mWaveformView.moveOffset(mXStart - x);
      mXStart = x;
    }

    @Override
    public void waveformTouchEnd() {
      RxUtil.unsubscribe(mSubscription);
      mTempOffset = secToMillisec(mWaveformView.fixOffset());
      mParentFragment.cutMusic(mTempOffset);
      mParentFragment.startPlayer();
      mSubscription = mParentFragment.startSubscribePlayer()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribeOn(Schedulers.newThread())
          .map(MusicCutFragment::millisecToSec)
          .filter(sec -> {
            if (mWaveformView != null && mWaveformView.isValidRunning(sec)) {
              return true;
            } else {
              mParentFragment.pausePlayer();
              mParentFragment.stopSubscribePlayer();
              return false;
            }
          })
          .subscribe(
              mWaveformView::updateUi,
              Throwable::printStackTrace,
              () -> RxUtil.unsubscribe(mSubscription)
          );

    }
  };

  public static int secToMillisec(float sec) {
    return (int) (sec * 1000);
  }

  public static float millisecToSec(int millisec) {
    return millisec / 1000.0f;
  }
}
