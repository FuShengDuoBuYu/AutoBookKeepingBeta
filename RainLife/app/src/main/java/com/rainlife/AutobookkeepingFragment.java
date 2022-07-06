package com.rainlife;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.rainlife.autobookkeeping.MainActivity;
import com.rainlife.autobookkeeping.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AutobookkeepingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutobookkeepingFragment extends Fragment {
    QMUIAlphaImageButton btn_navigate_to_autobookkeeping_main_page = null;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AutobookkeepingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutobookkeepingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutobookkeepingFragment newInstance(String param1, String param2) {
        AutobookkeepingFragment fragment = new AutobookkeepingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_autobookkeeping, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        btn_navigate_to_autobookkeeping_main_page = getActivity().findViewById(R.id.navigate_to_autobookkeeping_main_page);
//        btn_navigate_to_autobookkeeping_main_page.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}