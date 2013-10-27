package com.theiyer.whatstheplan;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.ExpenseReport;
import com.theiyer.whatstheplan.entity.ExpenseRow;
import com.thoughtworks.xstream.XStream;

public class ExpenseReportActivity extends Activity implements OnItemClickListener {

	ListView expenseReportListView;
	ExpenseListAdapter adapter;
	List<ExpenseRow> expenseRows;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.expense_report);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Expense Report");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String selectedPlan = prefs.getString("selectedPlan", "New User");
		String searchQuery = "/generateReport?planName="
				+ selectedPlan.replace(" ", "%20");

		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("ExpenseReport", ExpenseReport.class);
				xstream.alias("expenseRows", ExpenseRow.class);
				xstream.addImplicitCollection(ExpenseReport.class, "expenseRows");
				ExpenseReport expenseReport = (ExpenseReport) xstream
						.fromXML(response);
				if (expenseReport != null) {

					expenseRows = expenseReport
							.getExpenseRows();

					if (expenseRows != null && !expenseRows.isEmpty()) {

						expenseReportListView = (ListView) findViewById(R.id.viewexpensereport);

						adapter = new ExpenseListAdapter(this, this, expenseRows);
						expenseReportListView.setAdapter(adapter);
						expenseReportListView.setOnItemClickListener(this);
						TextView expListLabel = (TextView) findViewById(R.id.expenseLabel);
						String selectedGroup = prefs.getString("selectedGroup",
								"New User");
						expListLabel.setText("Group: " + selectedGroup);

					}

				}
			}
		} catch (InterruptedException e) {
			

		} catch (ExecutionException e) {
			

		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ExpenseRow expenseRow = expenseRows.get(position);
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "New User");
		if(phone.equals(expenseRow.getPhone())){
			Intent intent = new Intent(this, AddExpenseActivity.class);
			startActivity(intent);
		} else {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("selectedUser",expenseRow.getName());
			editor.putString("selectedPhone",expenseRow.getPhone());
			editor.apply();
			Intent intent = new Intent(this, ViewExpenseActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyGroupActivity.class);
	    startActivity(intent);
	}
	
	

}
