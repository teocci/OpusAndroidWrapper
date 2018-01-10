package com.github.teocci.opusWrapper.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.teocci.opusWrapper.model.ConvertParam;
import com.github.teocci.opusWrapper.R;

import java.util.NoSuchElementException;

import com.github.teocci.opusWrapper.utils.FileUtils;
import com.github.teocci.opuslib.OpusService;
import com.github.teocci.opuslib.OpusTrackInfo;
import com.github.teocci.opuslib.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentConvert#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentConvert extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener
{
    public static final String TAG = FragmentRecord.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "Title";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton btnImportWav;
    private ImageButton btnConvertConfig;
    private ImageButton btnConvert;
    private Button btnEncType;
    private Button btnEncComp;
    private Button btnEncBitrate;
    private Button btnEncFrameSize;

    private ListView lvConfig;

    private MainActivity activity = null;

    private static final int REQUEST_CODE = 123;
    private static final String CHOSEN_CONFIG_BTN = "CHOSEN_CONFIG_BTN";
    private static final String IS_WAV_IMPORTED = "IS_WAV_IMPORTED";
    private static final String WAV_FILE_PATH = "WAV_FILE_PATH";
    private static final String IS_CONFIG_BTN_CLICKED = "IS_CONFIG_BTN_CLICKED";
    private static final String CONVERT_PARAM = "CONVERT_PARAM";

    private Bundle fragmentState;
    private ConvertParam convertParam = new ConvertParam();

    private String wavFile = "";

    public FragmentConvert()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            fragmentState = savedInstanceState.getBundle(TAG);
            if (fragmentState != null) {
                convertParam = (ConvertParam) (fragmentState.getSerializable(CONVERT_PARAM));
                wavFile = fragmentState.get(WAV_FILE_PATH).toString();
            }
        } else {
            fragmentState = new Bundle();
            fragmentState.putBoolean(IS_CONFIG_BTN_CLICKED, false);
            fragmentState.putBoolean(IS_WAV_IMPORTED, false);
            fragmentState.putInt(CHOSEN_CONFIG_BTN, R.id.btnEncBitRate);
            initConvertParam();
            fragmentState.putSerializable(CONVERT_PARAM, convertParam);
            fragmentState.putString(WAV_FILE_PATH, wavFile);
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_convert, container, false);
        if (activity == null) {
            throw new NoSuchElementException();
        } else {
            activity.initConverUI(v);
        }
        initUI(v);
        return v;
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id) {
            case R.id.btnImportWav:
                showChooser();
                break;
            case R.id.btnConverConfig:
                boolean isConfigClicked = !fragmentState.getBoolean(IS_CONFIG_BTN_CLICKED);
                fragmentState.putBoolean(IS_CONFIG_BTN_CLICKED, isConfigClicked);

                if (isConfigClicked) {
                    changeVisibility(fragmentState);
                    highlightedEncBtn(fragmentState.getInt(CHOSEN_CONFIG_BTN));
                } else {
                    changeVisibility(fragmentState);
                }

                break;
            case R.id.btnEncBitRate:
                highlightedEncBtn(id);
                break;
            case R.id.btnEncComp:
                highlightedEncBtn(id);
                break;
            case R.id.btnEncFrameSize:
                highlightedEncBtn(id);
                break;
            case R.id.btnEncType:
                highlightedEncBtn(id);
                break;
            case R.id.configListV:
                break;
            case R.id.btnConvert:
                onConvertClick();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        fragmentState.putString(WAV_FILE_PATH, wavFile);
        fragmentState.putSerializable(CONVERT_PARAM, convertParam);
        outState.putBundle(TAG, fragmentState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onResume()
    {
        changeVisibility(fragmentState);
        highlightedEncBtn(fragmentState.getInt(CHOSEN_CONFIG_BTN));
        super.onResume();
    }

    @Override
    public void onDetach()
    {
        activity = null;
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(getActivity().getApplicationContext(), uri);
                            wavFile = path;
                            String msg = getString(R.string.cfg_enc_wav_file) + path;
                            ((TextView) getView().findViewById(R.id.tvWavFilePath)).setText(msg);
                        } catch (Exception e) {
                            Utils.printE(TAG, e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        int ind = fragmentState.getInt(CHOSEN_CONFIG_BTN);
        convertParam.select(ind, (int) id);
    }

    private void initUI(View v)
    {
        RelativeLayout configLayout = (RelativeLayout) v.findViewById(R.id.configLayout);
        btnImportWav = (ImageButton) v.findViewById(R.id.btnImportWav);
        btnConvertConfig = (ImageButton) v.findViewById(R.id.btnConverConfig);
        btnConvert = (ImageButton) v.findViewById(R.id.btnConvert);
        btnEncType = (Button) v.findViewById(R.id.btnEncType);
        btnEncComp = (Button) v.findViewById(R.id.btnEncComp);
        btnEncBitrate = (Button) v.findViewById(R.id.btnEncBitRate);
        btnEncFrameSize = (Button) v.findViewById(R.id.btnEncFrameSize);
        lvConfig = (ListView) v.findViewById(R.id.configListV);
        lvConfig.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        configLayout.setOnClickListener(this);
        btnImportWav.setOnClickListener(this);
        btnConvert.setOnClickListener(this);
        btnConvertConfig.setOnClickListener(this);
        btnEncType.setOnClickListener(this);
        btnEncComp.setOnClickListener(this);
        btnEncBitrate.setOnClickListener(this);
        btnEncFrameSize.setOnClickListener(this);
        lvConfig.setOnItemClickListener(this);

        if (!wavFile.isEmpty()) {
            String msg = getString(R.string.cfg_enc_wav_file) + wavFile;
            ((TextView) v.findViewById(R.id.tvWavFilePath)).setText(msg);
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentConvert.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentConvert newInstance(String param1, String param2)
    {
        FragmentConvert fragment = new FragmentConvert();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    private void showChooser()
    {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, getString(R.string.msg_choose_file));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }

    private void onConvertClick()
    {
        if (wavFile.isEmpty()) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getString(R.string.msg_err_convert_no_input),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!Utils.isWAVFile(wavFile)) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getString(R.string.msg_err_convert_not_wav),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String output = OpusTrackInfo.getInstance().getAValidFileName(Utils.getFileName(wavFile));
        String opting = convertParam.getFinalSelections();
        OpusService.encode(getActivity().getApplicationContext(), wavFile, output, opting);
    }

    private void highlightedEncBtn(int sourceID)
    {
        View v = getView();
        if (v != null) {
            highlightedEncBtn(v, sourceID);
        }
    }

    private void highlightedEncBtn(View v, int sourceID)
    {
        int prev = fragmentState.getInt(CHOSEN_CONFIG_BTN);
        v.findViewById(prev).setBackgroundResource(R.color.none);

        fragmentState.putInt(CHOSEN_CONFIG_BTN, sourceID);
        v.findViewById(sourceID).setBackgroundResource(R.color.mchoosen);

        changeListViewData(sourceID);
    }

    private void changeVisibility(Bundle b)
    {
        int visible = View.INVISIBLE;
        if (b.getBoolean(IS_CONFIG_BTN_CLICKED, false)) {
            visible = View.VISIBLE;
            btnConvertConfig.setImageResource(R.mipmap.icon_convert_config_pressed);
        } else {
            btnConvertConfig.setImageResource(R.mipmap.icon_convert_config);
        }
        btnEncType.setVisibility(visible);
        btnEncComp.setVisibility(visible);
        btnEncBitrate.setVisibility(visible);
        btnEncFrameSize.setVisibility(visible);
        lvConfig.setVisibility(visible);
    }

    private void changeListViewData(int id)
    {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_single_choice,
                convertParam.getValues(id)
        );
        lvConfig.setAdapter(mAdapter);
        int index = convertParam.getSelectedIndex(id);
        lvConfig.setSelection(index);
        lvConfig.setItemChecked(index, true);
        mAdapter.notifyDataSetChanged();
    }

    private void initConvertParam()
    {
        String[] rates = {
                " 6", " 16",
                " 32", " 64",
                " 128", " 178",
                " 256", " 320",
                " 384", " 512"
        };
        String[] comps = {" 0", " 1", " 2", " 3", " 4", " 5", " 6", " 7", " 8", " 9", " 10"};
        String[] frames = {" 2.5", " 5", " 10", " 20", " 40", " 60"};
        String[] types = {" --vbr", " --cvbr", " --hard-cbr"};

        convertParam.add(R.id.btnEncBitRate, " --bitrate", rates);
        convertParam.add(R.id.btnEncType, "", types);
        convertParam.add(R.id.btnEncComp, " --comp", comps);
        convertParam.add(R.id.btnEncFrameSize, " --framesize", frames);

        convertParam.select(R.id.btnEncType, 0);
        convertParam.select(R.id.btnEncBitRate, 3);
        convertParam.select(R.id.btnEncComp, 10);
        convertParam.select(R.id.btnEncFrameSize, 3);
    }
}
