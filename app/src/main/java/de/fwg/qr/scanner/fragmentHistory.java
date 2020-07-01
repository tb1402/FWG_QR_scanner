package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import de.fwg.qr.scanner.history.historyEntry;
import de.fwg.qr.scanner.history.historyListAdapter;
import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.history.taskResultCallback;

public class fragmentHistory extends fragmentWrapper {

    private ListView listHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle sis) {
        // Visuelle Elemente
        listHistory = v.findViewById(R.id.history_list_view);

        // historyManager instance
        final historyManager manager = new historyManager(getContext());
        lockUI(true);
        //manager.clearHistory();

        manager.getAssociatedEntriesAsync(new taskResultCallback() {
            @Override
            public void onFinished(Object result) {
                historyEntry[] entries = (historyEntry[])result;
                historyListAdapter adapter = new historyListAdapter(getContext(), entries);
                listHistory.setAdapter(adapter);
                lockUI(false);
            }
        });
    }
}