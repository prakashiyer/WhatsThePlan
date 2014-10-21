package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
import android.app.SearchManager;
import android.app.SearchableInfo;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.CenterList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class JoinGroupActivity extends Activity implements
OnItemClickListener {

	private GridView centersGridView;
	private CentersGridAdapter adapter;
	private List<Map<String, Center>> centersList;
	private List<Map<String, Center>> filteredList;
	private String selectedCenterName;
	private Context context;
	private String selectedCenter;
	private Button button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.join_group);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Center Membership");
			context = this;

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView userNameValue = (TextView) findViewById(R.id.welcomeJoinGroupLabel);
			userNameValue.setText(userName + ", Search and join your center!");
			
			centersList = new ArrayList<Map<String, Center>>();
			centersGridView = (GridView) findViewById(R.id.joingroupList);
			adapter = new CentersGridAdapter(this);
			centersGridView.setOnItemClickListener(this);
			context = this;
			button = (Button) findViewById(R.id.joinGroupButton);
			final SearchView searchView = (SearchView) findViewById(R.id.groupSearchView);
				
			searchView.setOnQueryTextListener(new OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextSubmit(String query) {
					
					String searchQuery = "/searchCenter?name="
							+ query.replace(" ", "%20");

					WebServiceClient restClient = new WebServiceClient(context);
					restClient.execute(new String[] { searchQuery });
					return true;
				}
				
				@Override
				public boolean onQueryTextChange(String newText) {
					if (!centersList.isEmpty()) {
						
						filteredList = new ArrayList<Map<String,Center>>();
						for(Map<String, Center> centerMap: centersList){
							for(Entry<String, Center> entry : centerMap.entrySet()){
								Center center = entry.getValue();
								if(center.getName().toLowerCase(Locale.ENGLISH).contains(newText.toLowerCase(Locale.ENGLISH))){
									filteredList.add(centerMap);
								}
							}
						}
						
						adapter.setData(filteredList);
						centersGridView.setAdapter(adapter);
						//memberListLabel.setVisibility(TextView.VISIBLE);
						centersGridView.setVisibility(GridView.VISIBLE);						
					}
					return true;
				}
			});
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}	
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		for(Map<String, Center> selectedMap: centersList){
			for (Entry<String, Center> entry : selectedMap.entrySet()) {
				Center center = entry.getValue();
				if(center.isSelected()){
					center.setSelected(false);
				}
			}
		}
		
		if (filteredList != null && !filteredList.isEmpty()) {
			Map<String, Center> selectedMap = filteredList.get(position);

			for (Entry<String, Center> entry : selectedMap.entrySet()) {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				Center center = entry.getValue();
				center.setSelected(true);
				selectedCenter = String.valueOf(center.getId());
				selectedCenterName = center.getName();
				editor.putString("selectedCenterPhone", center.getAdminPhone());
				editor.apply();
				adapter.setData(filteredList);
				centersGridView.setAdapter(adapter);
				centersGridView.setVisibility(GridView.VISIBLE);
				button.setVisibility(Button.VISIBLE);
				
				break;
			}
		}
	}


	/** Called when the user clicks the button */
	public void goFromJoinGroupToViewGroups(View view) {
		Button button = (Button) findViewById(R.id.joinGroupButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");

		String joinQuery = "/joinCenter?id=" + selectedCenter
				+ "&phone=" + phone;
		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(
				new String[] { joinQuery });
		Toast.makeText(getApplicationContext(), "You have been added to the Health Center "+selectedCenterName,
				Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, ViewCenterUpcomingPlansActivity.class);
		startActivity(intent);
		
	}
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private boolean isSearchCall;

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

			if(params[0].contains("searchCenter")){
				isSearchCall = true;
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
			
			if (response != null && isSearchCall) {
				XStream centersXs = new XStream();
			    centersXs.alias("CenterList", CenterList.class);
				centersXs.addImplicitCollection(CenterList.class, "centers");
			    centersXs.alias("centers", Center.class);
				centersXs.alias("members", String.class);
				centersXs.addImplicitCollection(Center.class, "members",
						"members", String.class);
				CenterList centerList = (CenterList) centersXs.fromXML(response);
				if (centerList != null) {
					List<Center> centers = centerList.getCenters();
					if(centers != null && !centers.isEmpty())
					for(Center center: centerList.getCenters()){
						Map<String, Center> centerMap = new HashMap<String, Center>();
						centerMap.put(String.valueOf(center.getId()), center);
						centersList.add(centerMap);
					}
					
					if (!centersList.isEmpty()) {
						filteredList = new ArrayList<Map<String,Center>>();
						filteredList.addAll(centersList);
						adapter.setData(filteredList);
						centersGridView.setAdapter(adapter);
						centersGridView.setVisibility(GridView.VISIBLE);
					}
				}
			} else if(response == null && isSearchCall) {
				Toast.makeText(mContext,
						"Seems like an invalid group name", Toast.LENGTH_LONG)
						.show();
			} 
			pDlg.dismiss();
		}

	}
	
	

	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param ctx
	 * @return True if device has internet
	 * 
	 *         Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}

		return true;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, HomePlanGroupFragmentActivity.class);
		startActivity(intent);
	}
}
