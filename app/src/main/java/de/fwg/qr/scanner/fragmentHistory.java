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

/**
 * fragment to show the history
 */
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
        //Visuelle Elemente
        listHistory = v.findViewById(R.id.history_list_view);

        // historyManager instance
        final historyManager manager = new historyManager(c);
        //lockUI(true);
        //manager.clearHistory();
        //manager.addEntry(new historyEntry("dYjeGwGiIm"));
        //manager.addEntry(new historyEntry("EQUgDFPunm"));
        //manager.addEntry(new historyEntry("EsluEnKeHJ"));

        manager.getAssociatedEntriesAsync(new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {
                // Rearrange the Array to list the entries descending;
                historyEntry[] hstBuff = new historyEntry[result.length];
                for (int i = 0, j = result.length - 1; i < result.length; i++, j--) {
                    hstBuff[i] = result[j];
                }
                historyListAdapter adapter = new historyListAdapter(c, hstBuff);
                listHistory.setAdapter(adapter);
                //lockUI(false);
            }
        }, historyManager.MaxEntries);

    }
}