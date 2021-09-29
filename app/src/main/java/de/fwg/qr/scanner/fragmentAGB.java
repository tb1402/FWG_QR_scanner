package de.fwg.qr.scanner;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * fragment for showing legal disclaimer
 */
public class fragmentAGB extends fragmentWrapper {
    private preferencesManager p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = preferencesManager.getInstance(c);
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

        //apply webView settings for darkmode (if supported by device)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) content.setForceDarkAllowed(true);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            //note: WebSettingsCompat.FORCE_DARK_AUTO is NOT used, as it isn't really supported well, instead darkmode is manually set with our saved value
            WebSettingsCompat.setForceDark(content.getSettings(), p.getDarkMode() != 1 ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);

            //ensures, that only css them is used (if supported)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY))
                WebSettingsCompat.setForceDarkStrategy(content.getSettings(), WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY);
        }

        content.loadUrl("file:///android_asset/agb.html");
        content.setBackgroundColor(Color.TRANSPARENT);

        //create button onClick listeners only if first run
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