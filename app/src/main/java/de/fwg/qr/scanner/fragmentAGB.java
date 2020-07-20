package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import de.fwg.qr.scanner.tools.preferencesManager;

public class fragmentAGB extends fragmentWrapper {
    private preferencesManager p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = new preferencesManager(c);
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
        /* create button onClick listeners only if first run */
        WebView content = view.findViewById(R.id.wv_content);
        content.loadUrl("file:///android_asset/agb.html");
        //content.setBackgroundColor(Color.TRANSPARENT);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);
        if (p.isFirstRun()) {
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    p.saveBoolean("firstrun", false);
                    NavHostFragment.findNavController(fragmentAGB.this).navigate(R.id.action_fragmentAGB_to_fragmentQuickGuide);
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