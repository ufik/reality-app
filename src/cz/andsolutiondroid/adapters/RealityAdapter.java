package cz.andsolutiondroid.adapters;

import java.util.List;

import cz.andsolutiondroid.R;
import cz.andsolutiondroid.model.Reality;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RealityAdapter extends ArrayAdapter<Reality> {

	public RealityAdapter(Context context, List<Reality> objects) {
		super(context, 0, objects);
	}

	public View getView(int position, View cv, ViewGroup parent) {
		
		Reality r = getItem(position);
		
		if (cv == null){
			cv = View.inflate(getContext(), R.layout.realityitem, null);
		}
		
		TextView name = (TextView) cv.findViewById(R.id.item_title);
		TextView synchronize = (TextView) cv.findViewById(R.id.item_synchronize);
		
		name.setText(r.getName());
		
		if(r.getSynchronize() == 1)
			synchronize.setText("Synchronizováno.");
		else
			synchronize.setText("Nesynchronizováno.");
		
		return cv;
	}

}
