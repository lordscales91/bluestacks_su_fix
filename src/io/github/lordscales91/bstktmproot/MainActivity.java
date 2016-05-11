package io.github.lordscales91.bstktmproot;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements TaskCaller {

	private static final String CHECK_STATUS = "check_status";
	private static final String DELETE_SU = "delete_su";
	private static final String FIX_SU = "fix_su";
	private ProgressBar pbChecking;
	private TextView tvSuBinStatus;
	private TextView tvSuperSUStatus;
	private Button btnFixSU;
	private Button btnUnroot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pbChecking = (ProgressBar)findViewById(R.id.pbChecking);
		tvSuBinStatus = (TextView)findViewById(R.id.tvSuBinStatus);
		tvSuperSUStatus = (TextView)findViewById(R.id.tvSuperSUStatus);
		btnFixSU = (Button)findViewById(R.id.btnFixSu);
		btnUnroot = (Button)findViewById(R.id.btnUnroot);
		btnFixSU.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnFix_onClick();
			}
		});
		btnUnroot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnUnroot_onClick();
			}
		});
		triggerStatusUpdate();
	}

	protected void btnUnroot_onClick() {
		deleteSu(DELETE_SU);
	}

	protected void btnFix_onClick() {
		// For now this will just delete the broken su
		// and then call SuperSU to let it install the binary.
		// Ideally we should backup the su binary and automatically restore it
		deleteSu(FIX_SU);
	}
	
	private void deleteSu(String tag) {
		// This will delete the current su. Either if it is corrupted or not.
		// Unless the legacy su is not found
		pbChecking.setVisibility(View.VISIBLE);
		btnFixSU.setEnabled(false);
		btnUnroot.setEnabled(false);
		new SuperTask(tag, this).execute(Constants.CMD_DELETE_SU);
	}
	
	private void triggerStatusUpdate() {
		Log.d("MainActivity", "Triggering status update");
		new SuperTask(this).execute(Constants.CMD_CHECK_STATUS);
	}

	private void updateStatus(Bundle results) {
		String su_status = "unavailable";
		String supersu_status = "not found";
		boolean enable_fix = false;
		boolean enable_unroot = true;
		if(results.getBoolean(Constants.SU_AVAILABLE, false)) {
			if(results.getBoolean(Constants.IS_SUPERSU, false)) {
				su_status = "SuperSU";
				supersu_status = "Working fine!";
			} else {
				su_status = "legacy";
				enable_unroot = false;
			}
		} else {
			boolean bstk_su = results.getBoolean(Constants.BSTK_SU_AVAILABLE, false);
			if(results.getBoolean(Constants.SUPERSU_BROKEN, false)) {
				supersu_status = "broken";				
				enable_fix = bstk_su;				
			}
			if(!bstk_su) {
				Toast.makeText(this, "BlueStacks legacy su not found!", Toast.LENGTH_LONG).show();
				enable_unroot = false;
			}
		}
		btnFixSU.setEnabled(enable_fix);
		btnUnroot.setEnabled(enable_unroot);
		tvSuBinStatus.setText(su_status);
		tvSuperSUStatus.setText(supersu_status);
	}
	
	private void launchSuperSU() {
		String supersuPackage = "eu.chainfire.supersu";
		try {
			PackageManager pm = getPackageManager();
			Intent launch = pm.getLaunchIntentForPackage(supersuPackage);
			if(launch != null) {
				startActivity(launch);
			}
		} catch(Exception ex) {
			// For some reason I can't catch the NameNotFoundException since it is not
			// declared in the method signature. WHAT IS THIS SORCERY!?
			// Nevertheless, we can safely assume that if the code reaches here then
			// SuperSU is not installed. Let's direct the user to Play Store
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + supersuPackage)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + supersuPackage)));
			}
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void getResults(String tag, Bundle results) {
		pbChecking.setVisibility(View.GONE);
		if(tag.equals(CHECK_STATUS)) {
			updateStatus(results);
		} else if(tag.equals(DELETE_SU)) {
			if(results.getBoolean(Constants.OP_RESULT, false)) {
				Toast.makeText(this, "SuperSU binary deleted successfully", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "There was a problem deleting the binary", Toast.LENGTH_LONG).show();
			}
			
		} else if(tag.equals(FIX_SU)) {
			if(results.getBoolean(Constants.OP_RESULT, false)) {
				launchSuperSU();
			} else {
				Toast.makeText(this, "There was a problem fixing the binary", Toast.LENGTH_LONG).show();
			}
		}
		if(!tag.equals(CHECK_STATUS)) {
			triggerStatusUpdate();
		}
	}

	@Override
	public void getResults(Bundle results) {
		this.getResults(CHECK_STATUS, results);
	}	
}
