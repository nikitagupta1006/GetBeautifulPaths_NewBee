package com.example.getallpossiblepaths;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.getallpossiblepaths.Modules.Route;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class BottomSheetFragment extends BottomSheetDialogFragment {
    private Context context;
    private ArrayList<Route> rentalProperties;

    public BottomSheetFragment(List<Route> routes) {
        // Required empty public constructor
        this.rentalProperties = (ArrayList<Route>) routes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_fragment_bottom_sheet);

        ArrayAdapter<Route> adapter = new MapsActivity.CustomArrayAdapter(getContext(), 0, rentalProperties);
        final ListView listView = (ListView) view.findViewById(R.id.customListView);

        final ListView directionListView = (ListView) view.findViewById(R.id.directionListView);
        directionListView.setVisibility(View.GONE);
        final TextView textView = (TextView) view.findViewById(R.id.heading) ;
        textView.setText("Routes");

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route route = (Route) listView.getItemAtPosition(position);
                listView.setVisibility(View.GONE);
                directionListView.setVisibility(View.VISIBLE);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,route.htmlInstructions);
                directionListView.setAdapter(arrayAdapter);
                textView.setText("Directions");
            }
        });

        adapter.setNotifyOnChange(true);
        return view;
    }
}
