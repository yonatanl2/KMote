package com.levyinc.android.kodimote;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ExtendedControlActivity extends Fragment {


    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.extended_controls, container, false);

        return rootView;
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button subButton = (Button) rootView.findViewById(R.id.subtitle_button);
        subButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ButtonActions.getSubs();
            }
        });
    }
}

