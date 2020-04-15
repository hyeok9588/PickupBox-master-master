package app.project.com.pickupbox.Tab;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.project.com.pickupbox.R;

public class Tab1 extends androidx.fragment.app.Fragment {


    public static Tab1 newInstance() {
        Bundle args = new Bundle();

        Tab1 fragment = new Tab1();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab1, container, false);
    }

}
