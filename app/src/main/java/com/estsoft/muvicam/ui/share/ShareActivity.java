package com.estsoft.muvicam.ui.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;

import java.io.File;
import java.util.Locale;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jaylim on 12/15/2016.
 */

public class ShareActivity extends Activity {

  private final static String EXTRA_VIDEO_PATHS = "ShareActivity.videoPaths";
  private final static String EXTRA_VIDEO_OFFSETS = "ShareActivity.videoOffsets";
  private final static String EXTRA_MUSIC_PATH = "ShareActivity.musicPath";
  private final static String EXTRA_MUSIC_OFFSET = "ShareActivity.musicOffset";
  private final static String EXTRA_MUSIC_LENGTH = "ShareActivity.musicLength";

  public static Intent newIntent(Context packageContext, String[] videoPaths, int[] videoOffsets,
                                 String musicPath, int musicOffset, int musicLength) {
    Intent intent = new Intent(packageContext, ShareActivity.class);
    intent.putExtra(EXTRA_VIDEO_PATHS, videoPaths);
    intent.putExtra(EXTRA_VIDEO_OFFSETS, videoOffsets);
    intent.putExtra(EXTRA_MUSIC_PATH, musicPath);
    intent.putExtra(EXTRA_MUSIC_OFFSET, musicOffset);
    intent.putExtra(EXTRA_MUSIC_LENGTH, musicLength);

    return intent;
  }

  private String[] mVideoPaths;
  private int[] mVideoOffsets;
  private String mMusicPath;
  private int mMusicOffset;
  private int mMusicLength;

  @BindView(R.id.share_test_text_view)
  TextView mTestTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_share);
    ButterKnife.bind(this);

    Intent intent = getIntent();

    mVideoPaths = intent.getStringArrayExtra(EXTRA_VIDEO_PATHS);
    mVideoOffsets = intent.getIntArrayExtra(EXTRA_VIDEO_OFFSETS);
    mMusicPath = intent.getStringExtra(EXTRA_MUSIC_PATH);
    mMusicOffset = intent.getIntExtra(EXTRA_MUSIC_OFFSET, 0);
    mMusicLength = intent.getIntExtra(EXTRA_MUSIC_LENGTH, 0);

    StringBuilder sb = new StringBuilder("Video: \n");
    for (int i = 0; i < mVideoPaths.length; i++) {
      String s = String.format(Locale.US,
          "Path : %30s, [offset : %5d]\n", mVideoPaths[i], mVideoOffsets[i]);
      sb.append(s);
    }
    sb.append("\nMusic: \n");
    String s = String.format(Locale.US,
        "Path : %30s, [offset : %5d, len : %5d]\n", mMusicPath, mMusicOffset, mMusicLength);
    sb.append(s);

    mTestTextView.setText(sb.toString());
  }
}
