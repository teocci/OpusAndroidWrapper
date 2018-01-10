package com.github.teocci.opusWrapper.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.teocci.opusWrapper.Adapter.ListViewAdapter;
import com.github.teocci.opusWrapper.Adapter.PageViewAdapter;
import com.github.teocci.opusWrapper.R;
import com.github.teocci.opuslib.OpusService;
import com.github.teocci.opuslib.OpusTrackInfo;
import com.github.teocci.opuslib.model.AudioPlayList;
import com.github.teocci.opuslib.model.AudioTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.teocci.opuslib.OpusEvent.CONVERT_FAILED;
import static com.github.teocci.opuslib.OpusEvent.CONVERT_FINISHED;
import static com.github.teocci.opuslib.OpusEvent.CONVERT_STARTED;
import static com.github.teocci.opuslib.OpusEvent.EVENT_MSG;
import static com.github.teocci.opuslib.OpusEvent.EVENT_PLAY_DURATION;
import static com.github.teocci.opuslib.OpusEvent.EVENT_PLAY_PROGRESS_POSITION;
import static com.github.teocci.opuslib.OpusEvent.EVENT_PLAY_TRACK_INFO;
import static com.github.teocci.opuslib.OpusEvent.EVENT_RECORD_PROGRESS;
import static com.github.teocci.opuslib.OpusEvent.EVENT_TYPE;
import static com.github.teocci.opuslib.OpusEvent.PLAYING_FAILED;
import static com.github.teocci.opuslib.OpusEvent.PLAYING_FINISHED;
import static com.github.teocci.opuslib.OpusEvent.PLAYING_PAUSED;
import static com.github.teocci.opuslib.OpusEvent.PLAYING_STARTED;
import static com.github.teocci.opuslib.OpusEvent.PLAY_GET_AUDIO_TRACK_INFO;
import static com.github.teocci.opuslib.OpusEvent.PLAY_PROGRESS_UPDATE;
import static com.github.teocci.opuslib.OpusEvent.RECORD_FAILED;
import static com.github.teocci.opuslib.OpusEvent.RECORD_FINISHED;
import static com.github.teocci.opuslib.OpusEvent.RECORD_PROGRESS_UPDATE;
import static com.github.teocci.opuslib.OpusEvent.RECORD_STARTED;
import static com.github.teocci.opuslib.utils.Config.ACTION_OPUS_UI_RECEIVER;

public class MainActivity extends FragmentActivity implements
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener, SeekBar.OnSeekBarChangeListener
{
    private static final String TAG = MainActivity.class.getName();

    private static final String SCROLL_LIST_POSITION = "SCROLL_LIST_POSITION";
    private static final String TRACK_PLAYING_POSITION = "TRACK_PLAYING_POSITION";
    private static final String SONG_LIST = "SONG_LIST";
    private static final String PHASE_PLAY = "PHASE_PLAY";
    private static final String PHASE_RECORD = "PHASE_RECORD";
    private static final String PHASE_CONVERT = "PHASE_CONVERT";

    private final int CURRENT_TRACK = 0;
    private final int NEXT_TRACK = 1;
    private final int PREV_TRACK = -1;

    private ImageButton buttonPlay = null;
    private ImageButton buttonStop = null;
    private ImageButton buttonRecord = null;
    private ImageButton buttonConvert = null;

    private SeekBar playerSeekBar = null;

    private ListView tracks = null;

    private TextView recordTimeField = null;
    private TextView positionField = null;
    private TextView durationField = null;

    private ListViewAdapter trackAdapter;
    private AudioPlayList trackList;

    private int listScrollPosition = -1;

    private boolean phaseBtnPlay = false;
    private boolean phaseBtnRecord = false;
    private boolean phaseBtnConvert = false;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            Bundle bundle = intent.getExtras();
            int type = bundle.getInt(EVENT_TYPE, 0);
            switch (type) {
                case CONVERT_FINISHED:
                    String msg = bundle.getString(EVENT_MSG);
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.msg_convert_success) + msg,
                            Toast.LENGTH_LONG
                    ).show();
                    changeBtnConvertStatus(false);
                    break;
                case CONVERT_FAILED:
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.msg_err_convert_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                    changeBtnConvertStatus(false);
                    break;
                case CONVERT_STARTED:
                    changeBtnConvertStatus(true);
                    break;
                case RECORD_FAILED:
                    changeBtnRecordStatus(false);
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.msg_err_record_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
                case RECORD_FINISHED:
                    changeBtnRecordStatus(false);
                    msg = bundle.getString(EVENT_MSG);
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.msg_record_success) + msg,
                            Toast.LENGTH_LONG
                    ).show();
                    break;
                case RECORD_STARTED:
                    changeBtnRecordStatus(true);
                    break;
                case RECORD_PROGRESS_UPDATE:
                    String time = bundle.getString(EVENT_RECORD_PROGRESS);
                    recordTimeField.setText(time);
                    break;
                case PLAY_PROGRESS_UPDATE:
                    long position = bundle.getLong(EVENT_PLAY_PROGRESS_POSITION);
                    long duration = bundle.getLong(EVENT_PLAY_DURATION);
                    AudioTime t = new AudioTime();
                    t.setTimeInSecond(position);
                    positionField.setText(t.getTime());
                    t.setTimeInSecond(duration);
                    durationField.setText(t.getTime());
                    if (duration != 0) {
                        int progress = (int) (100 * position / duration);
                        playerSeekBar.setProgress(progress);
                    }
                    break;
                case PLAY_GET_AUDIO_TRACK_INFO:
                    List<Map<String, Object>> trackList = null;
                    if (bundle.getSerializable(EVENT_PLAY_TRACK_INFO) != null) {
                        trackList = ((AudioPlayList) (bundle.getSerializable(
                                EVENT_PLAY_TRACK_INFO
                        ))).getList();
                    }
                    MainActivity.this.trackList.clear();
                    if (trackList == null) return;
                    for (Map<String, Object> map : trackList) {
                        //TODO this is a test
                        if (map.get(OpusTrackInfo.TITLE_IMG).equals(0)) {
                            map.put(OpusTrackInfo.TITLE_IMG, R.drawable.default_music_icon);
                            MainActivity.this.trackList.add(map);
                        }
                    }
                    trackAdapter.highlightedItem(trackAdapter.getHighlightedItemPosition());
                    trackAdapter.notifyDataSetChanged();
                    break;
                case PLAYING_FAILED:
                    changeBtnPlayStatus(false);
                    break;
                case PLAYING_FINISHED:
                    changeBtnPlayStatus(false);
                    positionField.setText(new AudioTime().getTime());
                    playerSeekBar.setProgress(0);
                    break;
                case PLAYING_PAUSED:
                    changeBtnPlayStatus(false);
                    break;
                case PLAYING_STARTED:
                    changeBtnPlayStatus(true);
                    break;
                default:
                    Log.d(TAG, intent.toString() + "Invalid request,discarded");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int songPlayingPosition = -1;
        if (savedInstanceState == null) {
            trackList = new AudioPlayList();
        } else {
            listScrollPosition = savedInstanceState.getInt(SCROLL_LIST_POSITION);
            trackList = (AudioPlayList) (savedInstanceState.getSerializable(SONG_LIST));
            phaseBtnConvert = savedInstanceState.getBoolean(PHASE_CONVERT);
            phaseBtnPlay = savedInstanceState.getBoolean(PHASE_PLAY);
            phaseBtnRecord = savedInstanceState.getBoolean(PHASE_RECORD);
            songPlayingPosition = savedInstanceState.getInt(TRACK_PLAYING_POSITION);
        }
        trackAdapter = new ListViewAdapter(
                getApplicationContext(),
                trackList.getList(),
                R.layout.playlist_view,
                new String[]{
                        OpusTrackInfo.TITLE_TITLE,
                        OpusTrackInfo.TITLE_DURATION,
                        OpusTrackInfo.TITLE_IMG,
                        OpusTrackInfo.TITLE_ABS_PATH,
                        OpusTrackInfo.TITLE_IS_CHECKED
                },
                new int[]{
                        R.id.title,
                        R.id.duration,
                        R.id.img,
                        R.id.absPath,
                        R.id.isChecked
                }
        );
        trackAdapter.setHighlightedItemPosition(songPlayingPosition);
        initUI();
        initBroadcast();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            listScrollPosition = tracks.getFirstVisiblePosition();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        trackAdapter.highlightedItem(position);
        String fileName = trackList.getList().get(position).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
        OpusService.play(getApplicationContext(), fileName);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser) {
            float scale = ((float) progress) / playerSeekBar.getMax();
            OpusService.seekFile(getApplicationContext(), scale);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(SCROLL_LIST_POSITION, listScrollPosition);
        outState.putInt(TRACK_PLAYING_POSITION, trackAdapter.getHighlightedItemPosition());
        outState.putSerializable(SONG_LIST, trackList);
        outState.putBoolean(PHASE_CONVERT, phaseBtnConvert);
        outState.putBoolean(PHASE_RECORD, phaseBtnRecord);
        outState.putBoolean(PHASE_PLAY, phaseBtnPlay);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initBroadcast()
    {
        // Register a broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OPUS_UI_RECEIVER);
        registerReceiver(receiver, filter);
    }

    public void initRecordUI(View v)
    {
        buttonRecord = (ImageButton) v.findViewById(R.id.btnRecord);
        recordTimeField = (TextView) v.findViewById(R.id.tvRecordTime);
        changeBtnRecordStatus(phaseBtnRecord);
    }

    public void initConverUI(View v)
    {
        buttonConvert = (ImageButton) v.findViewById(R.id.btnConvert);
        changeBtnConvertStatus(phaseBtnConvert);
    }

    public void initPlayUI(View v)
    {
        buttonPlay = (ImageButton) v.findViewById(R.id.btnPlay);
        tracks = (ListView) v.findViewById(R.id.lvTracks);

        positionField = (TextView) v.findViewById(R.id.tvPosition);
        durationField = (TextView) v.findViewById(R.id.tvDuration);

        // Init seekBar
        playerSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        playerSeekBar.setOnSeekBarChangeListener(this);
        playerSeekBar.setMax(100);

        tracks.setAdapter(trackAdapter);
        tracks.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tracks.setOnItemClickListener(this);
        tracks.setOnScrollListener(this);

        if (listScrollPosition != -1) {
            tracks.setSelection(listScrollPosition);
        }

        // Only to start service
        OpusService.getTrackInfo(getApplicationContext());
        changeBtnPlayStatus(phaseBtnPlay);
    }

    private void initUI()
    {
        // Set the pager with an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        ArrayList<Fragment> fragments = new ArrayList<>();

        FragmentPlay fragmentPlay = FragmentPlay.newInstance(getString(R.string.fragment_play), "");
        FragmentRecord fragmentRecord = FragmentRecord.newInstance(getString(R.string.fragment_record), "");
//        FragmentConvert fragmentConvert = FragmentConvert.newInstance(getString(R.string.fragment_convert), "");

        fragments.add(fragmentRecord);
        fragments.add(fragmentPlay);
//        fragments.add(fragmentConvert);

        PageViewAdapter pageAdaptor = new PageViewAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(pageAdaptor);
        pager.setCurrentItem(1);

        // Bind the title indicator to the adapter
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.indicator);
//        tabLayout.setupWithViewPager(pager);
    }

    public boolean isTrackListEmpty()
    {
        if (trackList.size() == 0) {
            Toast.makeText(this, getString(R.string.msg_err_playlist_empty), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void onBtnPlayClick(View view)
    {
        playTrack(CURRENT_TRACK);
    }

    public void onBtnPrevClick(View view)
    {
        playTrack(PREV_TRACK);
    }

    public void onBtnNextClick(View view)
    {
        playTrack(NEXT_TRACK);
    }

    public void onBtnStopClick(View view)
    {
        OpusService.stopPlaying(getApplicationContext());
    }

    public void onBtnRecordClick(View v)
    {
        String fileName = "";
        OpusService.recordToggle(getApplicationContext(), fileName);
    }

    public void playTrack(int trackPosition)
    {
        if (isTrackListEmpty()) return;
        switch (trackPosition) {
            case NEXT_TRACK:
            case PREV_TRACK:
                if (!trackAdapter.highlightedItemByOffset(trackPosition)) return;
                break;
        }

        Map<String, Object> item = trackAdapter.getHighlightedItem();
        if (item != null) {
            tracks.setItemChecked(trackAdapter.getHighlightedItemPosition(), true);
            String fileName = item.get(OpusTrackInfo.TITLE_ABS_PATH).toString();
            OpusService.play(getApplicationContext(), fileName);
        }
    }

    private void changeBtnConvertStatus(boolean active)
    {
        if (active) { // converting
            Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.converting);
            buttonConvert.setImageResource(R.mipmap.icon_converting);
            buttonConvert.startAnimation(rotate);
            buttonConvert.setClickable(false);
        } else { // idle
            Animation anim = buttonConvert.getAnimation();
            if (anim != null) {
                buttonConvert.getAnimation().cancel();
                buttonConvert.clearAnimation();
            }
            buttonConvert.setImageResource(R.drawable.btn_convert_selector);
            buttonConvert.setClickable(true);
        }
        phaseBtnConvert = active;
    }

    private void changeBtnPlayStatus(boolean active)
    {
        if (active) { // playing
            buttonPlay.setImageResource(R.drawable.btn_pause_selector);
        } else { // idle
            buttonPlay.setImageResource(R.drawable.btn_play_selector);
        }
        phaseBtnPlay = active;
    }

    private void changeBtnRecordStatus(boolean active)
    {
        if (active) { // recording
            buttonRecord.setImageResource(R.drawable.btn_stop_recording_selector);
        } else { // idle
            buttonRecord.setImageResource(R.drawable.btn_record_selector);
        }
        phaseBtnConvert = active;
    }
}