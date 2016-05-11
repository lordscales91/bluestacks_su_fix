package io.github.lordscales91.bstktmproot;

import java.util.List;
import java.util.Locale;

import eu.chainfire.libsuperuser.Shell;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class SuperTask extends AsyncTask<String, Void, Bundle> {

	private TaskCaller caller = null;
	private String tag = null;
	
	public SuperTask(String tag, TaskCaller caller) {
		this.tag = tag;
		this.caller = caller;
	}
	
	public SuperTask(TaskCaller caller) {
		this(null, caller);
	}

	@Override
	protected Bundle doInBackground(String... params) {
		String cmd = (params.length > 0)?params[0]:"";
		Bundle results = new Bundle();
		if(cmd.equals(Constants.CMD_CHECK_STATUS)) {
			Log.d(getClass().getSimpleName(), "Checking status...");
			checkStatus(results);
		} else if(cmd.equals(Constants.CMD_DELETE_SU)) {
			Log.d(getClass().getSimpleName(), "Deleting su...");
			deleteSu(results);
		}
		return results;
	}

	@Override
	protected void onPostExecute(Bundle results) {
		if(caller!=null) {
			if(tag!=null) {
				caller.getResults(tag, results);
			} else {
				caller.getResults(results);
			}
		}
	}
	
	private void checkStatus(Bundle results) {
		// Clear always the cached results before checking the new status
		Shell.SU.clearCachedResults();
		boolean su_available = Shell.SU.available();
		results.putBoolean(Constants.SU_AVAILABLE, su_available);
		String ver = Shell.SU.version(false);
		results.putBoolean(Constants.IS_SUPERSU, ver != null 
							&& ver.toLowerCase(Locale.ENGLISH).contains("supersu"));
		results.putBoolean(Constants.BSTK_SU_AVAILABLE, ShellUtils.BSTK.available());
		if(!su_available) {
			// Maybe we have a broken su
			List<String> res = Shell.SH.run("su -v");
			if(res!=null) {
				for(String line:res) {
					if(line.toLowerCase(Locale.ENGLISH).contains("supersu")) {
						// Yep, this is a broken su. Or maybe our access was unauthorized
						// There is no way to know but anyway it is safe to assume the former.
						// We will delete the su binary ONLY if we find the BlueStacks legacy su
						results.putBoolean(Constants.SUPERSU_BROKEN, true);
					}
				}
			}
		}
	}
	
	private void deleteSu(Bundle results) {
		// First determine the path of the current su
		List<String> res = Shell.SH.run("which su");
		String su_path = null;
		if(res!=null) {
			for(String line:res) {
				if(Shell.SU.isSU(line) && !line.equals(ShellUtils.BSTK.bstkShell)) {
					su_path = line;
				}
			}
		}

		if(su_path!=null) {
			// Let's begin the show!
			res = ShellUtils.BSTK.run("mount -o rw,remount /system",
									  "rm "+su_path,
									  "echo -DONE-"); // Used to check if it executed properly
			// That was much easier than expected. Thanks to this awesome library (^_^)
			if(res!=null) {
				for(String line:res) {
					if(line.equals("-DONE-")) {
						results.putBoolean(Constants.OP_RESULT, true);
					}
				}
			}
		}
	}
	public TaskCaller getCaller() {
		return caller;
	}

	public void setCaller(TaskCaller caller) {
		this.caller = caller;
	}

}
