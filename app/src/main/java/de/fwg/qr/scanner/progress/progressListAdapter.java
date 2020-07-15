package de.fwg.qr.scanner.progress;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import de.fwg.qr.scanner.R;

public class progressListAdapter extends ArrayAdapter<visitedStation> {

    private final Context context;
    private final visitedStation[] Entries;

    public progressListAdapter(@NonNull Context ctx, visitedStation[] values) {
        super(ctx, -1, values); // PLS NEVER FORGET THAT LAST PARAMETER EVER F*CKIN AGAIN
        context = ctx;
        Entries = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.layout_progress_list_item, parent, false);

        TextView txtStationName = (TextView) rowView.findViewById(R.id.progress_list_item_txtstationname);

        txtStationName.setText(Entries[position].StationName);
        txtStationName.setTextColor( Entries[position].Visited ? ContextCompat.getColor(context, R.color.progress_item_visited) : ContextCompat.getColor(context, R.color.progress_item_not_visited));

        return rowView;
    }
}
