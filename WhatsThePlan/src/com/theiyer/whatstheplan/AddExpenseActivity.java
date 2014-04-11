package com.theiyer.whatstheplan;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Expense;
import com.theiyer.whatstheplan.entity.ExpenseList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class AddExpenseActivity extends Activity {

	private boolean edit1 = false;
	private boolean edit2 = false;
	private boolean edit3 = false;
	private boolean edit4 = false;
	private boolean edit5 = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.add_expense);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" My Expense List");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);

			String userName = prefs.getString("userName", "");
			String phone = prefs.getString("phone", "");
			String selectedPlan = prefs.getString("selectedPlan", "");
			String selectedGroup = prefs.getString("selectedGroup", "");

			TextView addLabel = (TextView) findViewById(R.id.addexpenseLabel);
			addLabel.setText(userName + "'s Expenses:");

			String searchQuery = "/fetchExpense?phone=" + phone + "&planName="
					+ selectedPlan.replace(" ", "%20") + "&groupName="
					+ selectedGroup.replace(" ", "%20");

		    WebServiceClient restClient = new WebServiceClient(this);

		    restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}

	/** Called when the user clicks the Submit Expense button */
	public void submitExpense(View view) {

		Button button = (Button) findViewById(R.id.submitExpense);
		button.setTextColor(getResources().getColor(R.color.click_button_1));

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String phone = prefs.getString("phone", "");
		String selectedPlan = prefs.getString("selectedPlan", "");
		String selectedGroup = prefs.getString("selectedGroup", "");

		EditText expenseTitle1 = (EditText) findViewById(R.id.addexpense1Title);
		EditText expenseValue1 = (EditText) findViewById(R.id.addexpense1Value);
		String title1 = expenseTitle1.getText().toString();
		String exp1 = expenseValue1.getText().toString();

		if (title1 != null && !title1.isEmpty() && exp1 != null
				&& !exp1.isEmpty()) {
			updateExpense(phone, selectedPlan, selectedGroup, title1, exp1,
					edit1);
		}

		EditText expenseTitle2 = (EditText) findViewById(R.id.addexpense2Title);
		EditText expenseValue2 = (EditText) findViewById(R.id.addexpense2Value);
		String title2 = expenseTitle2.getText().toString();
		String exp2 = expenseValue2.getText().toString();

		if (title2 != null && !title2.isEmpty() && exp2 != null
				&& !exp2.isEmpty()) {
			updateExpense(phone, selectedPlan, selectedGroup, title2, exp2,
					edit2);
		}

		EditText expenseTitle3 = (EditText) findViewById(R.id.addexpense3Title);
		EditText expenseValue3 = (EditText) findViewById(R.id.addexpense3Value);
		String title3 = expenseTitle3.getText().toString();
		String exp3 = expenseValue3.getText().toString();

		if (title3 != null && !title3.isEmpty() && exp3 != null
				&& !exp3.isEmpty()) {
			updateExpense(phone, selectedPlan, selectedGroup, title3, exp3,
					edit3);
		}

		EditText expenseTitle4 = (EditText) findViewById(R.id.addexpense4Title);
		EditText expenseValue4 = (EditText) findViewById(R.id.addexpense4Value);
		String title4 = expenseTitle4.getText().toString();
		String exp4 = expenseValue4.getText().toString();

		if (title4 != null && !title4.isEmpty() && exp4 != null
				&& !exp4.isEmpty()) {
			updateExpense(phone, selectedPlan, selectedGroup, title4, exp4,
					edit4);
		}

		EditText expenseTitle5 = (EditText) findViewById(R.id.addexpense5Title);
		EditText expenseValue5 = (EditText) findViewById(R.id.addexpense5Value);
		String title5 = expenseTitle5.getText().toString();
		String exp5 = expenseValue5.getText().toString();

		if (title5 != null && !title5.isEmpty() && exp5 != null
				&& !exp5.isEmpty()) {
			updateExpense(phone, selectedPlan, selectedGroup, title5, exp5,
					edit5);
		}
		Intent intent = new Intent(this, ExpenseReportActivity.class);
		startActivity(intent);

	}

	private void updateExpense(String phone, String selectedPlan,
			String selectedGroup, String title, String exp, boolean edit) {
		String query = "";
		if (edit) {
			query = "/updateExpense?planName="
					+ selectedPlan.replace(" ", "%20") + "&phone=" + phone
					+ "&groupName=" + selectedGroup.replace(" ", "%20")
					+ "&title=" + title + "&value=" + exp;
		} else {
			query = "/addExpense?planName=" + selectedPlan.replace(" ", "%20")
					+ "&phone=" + phone + "&groupName="
					+ selectedGroup.replace(" ", "%20") + "&title=" + title
					+ "&value=" + exp;
		}

		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(new String[] { query });
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ExpenseReportActivity.class);
	    startActivity(intent);
	}
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private boolean isFetchExpense = false;

		public WebServiceClient(Context mContext) {
			this.mContext = mContext;
		}

		private void showProgressDialog() {

			pDlg = new ProgressDialog(mContext);
			pDlg.setMessage("Processing ....");
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();

		}

		@Override
		protected void onPreExecute() {
			
		   showProgressDialog();

		}

		@Override
		protected String doInBackground(String... params) {
			String path = WTPConstants.SERVICE_PATH+params[0];

			if(params[0].contains("fetchExpense")){
				isFetchExpense = true;
			}
			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {
				HttpResponse response = client.execute(target, get);
				results = response.getEntity(); 
				String result = EntityUtils.toString(results);
				return result;
			} catch (Exception e) {
				
			}
			return null;
		}

		@Override
		protected void onPostExecute(String response) {
			
			if (response != null && isFetchExpense) {
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
							EditText expenseTitle = (EditText) findViewById(R.id.addexpense1Title);
							EditText expenseValue = (EditText) findViewById(R.id.addexpense1Value);
							expenseTitle.setText(expense1.getTitle());
							expenseValue.setText(String.valueOf(expense1
									.getValue()));
							edit1 = true;
						}

						if (size > 1) {
							Expense expense2 = expenses.get(1);
							if (expense2 != null) {
								EditText expenseTitle = (EditText) findViewById(R.id.addexpense2Title);
								EditText expenseValue = (EditText) findViewById(R.id.addexpense2Value);
								expenseTitle.setText(expense2.getTitle());
								expenseValue.setText(String.valueOf(expense2
										.getValue()));
								edit2 = true;
							}
						}

						if (size > 2) {
							Expense expense3 = expenses.get(2);
							if (expense3 != null) {
								EditText expenseTitle = (EditText) findViewById(R.id.addexpense3Title);
								EditText expenseValue = (EditText) findViewById(R.id.addexpense3Value);
								expenseTitle.setText(expense3.getTitle());
								expenseValue.setText(String.valueOf(expense3
										.getValue()));
								edit3 = true;
							}
						}

						if (size > 3) {
							Expense expense4 = expenses.get(3);
							if (expense4 != null) {
								EditText expenseTitle = (EditText) findViewById(R.id.addexpense4Title);
								EditText expenseValue = (EditText) findViewById(R.id.addexpense4Value);
								expenseTitle.setText(expense4.getTitle());
								expenseValue.setText(String.valueOf(expense4
										.getValue()));
								edit4 = true;
							}
						}
						if (size > 4) {
							Expense expense5 = expenses.get(4);
							if (expense5 != null) {
								EditText expenseTitle = (EditText) findViewById(R.id.addexpense5Title);
								EditText expenseValue = (EditText) findViewById(R.id.addexpense5Value);
								expenseTitle.setText(expense5.getTitle());
								expenseValue.setText(String.valueOf(expense5
										.getValue()));
								edit5 = true;
							}
						}
					}

				}
			}
			pDlg.dismiss();
		}

	}
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * @param ctx
	 * @return True if device has internet
	 *
	 * Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
	            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    
	    return true;
	}
}
