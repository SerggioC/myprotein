package com.cruz.sergio.myproteinpricechecker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ratan on 7/29/2015.
 */
public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText find_deal = (EditText) getActivity().findViewById(R.id.find_deal);
        find_deal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Toast theToast = Toast.makeText(getContext(), "Performing search", Toast.LENGTH_SHORT);
                    theToast.setGravity(Gravity.CENTER, 0, 0);
                    theToast.show();
                    return true;
                }
                return false;
            }
        });

        find_deal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Toast theToast = Toast.makeText(getContext(), "key entered " + s, Toast.LENGTH_SHORT);
                theToast.setGravity(Gravity.CENTER, 0, 0);
                theToast.show();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

}
