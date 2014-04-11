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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.ExpenseReport;
import com.theiyer.whatstheplan.entity.ExpenseRow;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ExpenseReportActivity extends Activity implements OnItemClickListener {

	ListView expenseReportListView;
	ExpenseListAdapter adapter;
	List<ExpenseRow> expenseRows;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
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

			adapter = new ExpenseListAdapter(this, this);
			expenseReportListView = (ListView) findViewById(R.id.viewexpensereport);
			expenseReportListView.setOnItemClickListener(this);
			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
			TextView expListLabel = (TextView) findViewById(R.id.expenseLabel);
			String selectedGroup = prefs.getString("selectedGroup",
					"New User");
			expListLabel.setText("Group: " + selectedGroup);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
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
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

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

						
						adapter.setData(expenseRows);
						expenseReportListView.setAdapter(adapter);
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
