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
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Expense;
import com.theiyer.whatstheplan.entity.ExpenseList;
import com.thoughtworks.xstream.XStream;

public class ViewExpenseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_expense);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" My Expense List");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String userName = prefs.getString("selectedUser", "");
		String phone = prefs.getString("selectedPhone", "");
		String selectedPlan = prefs.getString("selectedPlan", "");
		String selectedGroup = prefs.getString("selectedGroup", "");

		TextView addLabel = (TextView) findViewById(R.id.viewexpenseLabel);
		addLabel.setText(userName + "'s Expenses:");

		String searchQuery = "/fetchExpense?phone=" + phone + "&planName="
				+ selectedPlan.replace(" ", "%20") + "&groupName="
				+ selectedGroup.replace(" ", "%20");

		RestWebServiceClient restClient = new RestWebServiceClient(this);

		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("ExpenseList", ExpenseList.class);
				xstream.alias("expenses", Expense.class);
				xstream.addImplicitCollection(ExpenseList.class, "expenses");
				ExpenseList expenseList = (ExpenseList) xstream
						.fromXML(response);
				if (expenseList != null && expenseList.getExpenses() != null) {

					List<Expense> expenses = expenseList.getExpenses();

					if (expenses != null && !expenses.isEmpty()) {
						int size = expenses.size();
						Expense expense1 = expenses.get(0);
						if (expense1 != null) {
							if (expense1 != null) {
								TextView expenseTitle = (TextView) findViewById(R.id.viewexpense1Title);
								TextView expenseValue = (TextView) findViewById(R.id.viewexpense1Value);
								expenseTitle.setText(expense1.getTitle());
								expenseValue.setText(String.valueOf(expense1
										.getValue()));
							}
						}
						if (size > 1) {
							Expense expense2 = expenses.get(1);
							if (expense2 != null) {
								TextView expenseTitle = (TextView) findViewById(R.id.viewexpense2Title);
								TextView expenseValue = (TextView) findViewById(R.id.viewexpense2Value);
								expenseTitle.setText(expense2.getTitle());
								expenseValue.setText(String.valueOf(expense2
										.getValue()));
							}
						}
						if (size > 2) {
							Expense expense3 = expenses.get(2);
							if (expense3 != null) {
								TextView expenseTitle = (TextView) findViewById(R.id.viewexpense3Title);
								TextView expenseValue = (TextView) findViewById(R.id.viewexpense3Value);
								expenseTitle.setText(expense3.getTitle());
								expenseValue.setText(String.valueOf(expense3
										.getValue()));
							}
						}
						if (size > 3) {
							Expense expense4 = expenses.get(3);
							if (expense4 != null) {
								TextView expenseTitle = (TextView) findViewById(R.id.viewexpense4Title);
								TextView expenseValue = (TextView) findViewById(R.id.viewexpense4Value);
								expenseTitle.setText(expense4.getTitle());
								expenseValue.setText(String.valueOf(expense4
										.getValue()));
							}
						}
						if (size > 4) {
							Expense expense5 = expenses.get(4);
							if (expense5 != null) {
								TextView expenseTitle = (TextView) findViewById(R.id.viewexpense5Title);
								TextView expenseValue = (TextView) findViewById(R.id.viewexpense5Value);
								expenseTitle.setText(expense5.getTitle());
								expenseValue.setText(String.valueOf(expense5
										.getValue()));
							}
						}
					}

				}
			}
		} catch (InterruptedException e) {
			

		} catch (ExecutionException e) {
			

		}
	}
	
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ExpenseReportActivity.class);
	    startActivity(intent);
	}

}
