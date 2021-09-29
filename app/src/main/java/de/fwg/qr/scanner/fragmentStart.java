package de.fwg.qr.scanner;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import de.fwg.qr.scanner.tools.network;

public class fragmentStart extends fragmentWrapper {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* create button onClick listener */
        view.findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.start_fragment, new fragmentAGB()).commit();
            }
        });
        TextView tvg = view.findViewById(R.id.textViewStartGithub);
        tvg.setMovementMethod(LinkMovementMethod.getInstance());

        //exit, if no network connectivity present
        if(network.getInstance(c).noNetworkAvailable(c)){
            Toast.makeText(c,getString(R.string.network_no_connection),Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }
}