package com.beta.autobookkeeping.fragment.orderDetail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beta.autobookkeeping.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FamilyOrderDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FamilyOrderDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    View rootView;
    // TODO: Rename and change types of parameters
    private String mParam1;

    public FamilyOrderDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FamilyOrderDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FamilyOrderDetailFragment newInstance(String param1) {
        FamilyOrderDetailFragment fragment = new FamilyOrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView ==null){
            rootView = inflater.inflate(R.layout.fragment_family_order_detail, container, false);

        }
        findViewById();
        // Inflate the layout for this fragment
        return rootView;
    }

    private void findViewById(){
        TextView textView = rootView.findViewById(R.id.test_);
        textView.setText(mParam1);
    }
}