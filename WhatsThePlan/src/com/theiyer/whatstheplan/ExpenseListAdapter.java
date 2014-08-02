package com.theiyer.whatstheplan;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.ExpenseRow;

public class ExpenseListAdapter extends BaseAdapter {

	private Activity activity;
	private Context context;
	private List<ExpenseRow> data;
	private static LayoutInflater inflater = null;

	public ExpenseListAdapter(Context context, Activity a, List<ExpenseRow> d) {
		this.context = context;
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public ExpenseListAdapter(Context context, Activity a) {
		this.context = context;
		activity = a;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public List<ExpenseRow> getData() {
		return data;
	}

	public void setData(List<ExpenseRow> data) {
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.expense_list_row, null);
		}
		ImageView imgView = (ImageView) view
				.findViewById(R.id.expmemberPicThumbnail);
		TextView membertextView = (TextView) view
				.findViewById(R.id.expmemberNameField);
		TextView valuetextView = (TextView) view
				.findViewById(R.id.expmembervalueField);

		ExpenseRow expenseRow = data.get(position);

		membertextView.setText(expenseRow.getName());
		int value = expenseRow.getValue();
		valuetextView.setText(String.valueOf(expenseRow.getValue()));
		if(value<0){
			
			valuetextView.setBackgroundColor(context.getResources().getColor(R.color.red));
		} else {
			valuetextView.setBackgroundColor(context.getResources().getColor(R.color.green));
		}
		

		byte[] image = expenseRow.getUserImage();
		if (image != null) {
			Bitmap img = BitmapFactory.decodeByteArray(image, 0, image.length);
			imgView.setImageBitmap(img);
		} else {
			imgView.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_launcher));
		}
		
		return view;
	}

}
