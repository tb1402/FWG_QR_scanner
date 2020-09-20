package de.fwg.qr.scanner;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;

import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * fragment for showing legal disclaimer
 */
public class fragmentAGB extends fragmentWrapper {
    private preferencesManager p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p =preferencesManager.getInstance(c);
        if (!p.isFirstRun()) {
            showStartIcon();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agb, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView content = view.findViewById(R.id.wv_content);
        content.loadUrl(p.getDarkMode() != 1 ? "file:///android_asset/agb.html" : "file:///android_asset/agb-light.html");
        content.setBackgroundColor(Color.TRANSPARENT);
        /* create button onClick listeners only if first run */
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);
        if (p.isFirstRun()) {
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    p.saveBoolean("firstrun", false);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.start_fragment, new fragmentQuickGuide()).commit();
                }
            });
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    a.finishAffinity();
                }
            });
        } else {
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        }
    }
}