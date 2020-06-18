package de.fwg.qr.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import de.fwg.qr.scanner.tools.preferencesManager;

public class fragmentAGB extends fragmentWrapper {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agb, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferencesManager p = new preferencesManager(c);
        Button btn_accept = view.findViewById(R.id.btn_accept);
        Button btn_decline = view.findViewById(R.id.btn_decline);
        if (p.isFirstRun()) {
            btn_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preferencesManager pm = new preferencesManager(requireActivity().getApplicationContext());
                    pm.saveBoolean("firstrun", false);
                    Intent intent = new Intent(requireActivity(), activityMain.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            });
            btn_decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireActivity(), activityStart.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            });
        } else {
            btn_accept.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
        }
    }
}