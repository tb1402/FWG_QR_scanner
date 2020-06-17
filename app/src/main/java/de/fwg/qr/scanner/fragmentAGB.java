package de.fwg.qr.scanner;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import de.fwg.qr.scanner.tools.preferencesManager;

public class fragmentAGB extends fragmentWrapper {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agb, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferencesManager p=new preferencesManager(c);

        Button btn_accept=view.findViewById(R.id.btn_accept);
        if(p.isFirstRun()) {
            btn_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(fragmentAGB.this)
                            .navigate(R.id.action_fragmentAGB_to_fragmentScan);
                }
            });
        }
        else{
            btn_accept.setVisibility(View.GONE);
        }
    }
}