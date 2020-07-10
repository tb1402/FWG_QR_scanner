package de.fwg.qr.scanner.history;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import de.fwg.qr.scanner.R;

public class historyListAdapter extends ArrayAdapter<historyEntry> {

    private final Context context;
    private final historyEntry[] Entries;

    public historyListAdapter(@NonNull Context context, historyEntry[] values) {
        super(context, -1, values);
        this.context = context;
        this.Entries = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.layout_history_list_item, parent, false);

        TextView txtStationName = (TextView) rowView.findViewById(R.id.history_list_item_txtstationname);
        TextView txtLastVisited = (TextView) rowView.findViewById(R.id.history_list_item_txttimevisited);

        txtStationName.setText(Entries[position].StationName);
        String visitedDate = (String) DateFormat.format("dd.MM.yy HH:mm", Entries[position].TimeVisited);

        txtLastVisited.setText(visitedDate);

        return rowView;
    }
}
