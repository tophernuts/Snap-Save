package hey.rich.snapsaver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	// On Clickers
	private Button buttonPicture;
	private Button buttonVideo;
	private Button buttonBoth;

	// Strings
	private String storageLocation;
	private static final String STRING_PREFERENCES = "stringPrefs";

	// Shared Preferences
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
		prefEditor = prefs.edit();

		buttonPicture = (Button) findViewById(R.id.button_copy_picture);
		buttonVideo = (Button) findViewById(R.id.button_copy_video);
		buttonBoth = (Button) findViewById(R.id.button_both);

		this.buttonOnClick();
	}

	/** Sets up the buttons onClickListeners */
	private void buttonOnClick() {
		buttonPicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Copy Pictures
				copyDirectory("/pictures/");
				// Rename Pictures
				renameAllFiles("/pictures/");
			}
		});

		buttonVideo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Copy videos
				copyDirectory("/videos/");
				// Rename videos
				renameAllFiles("/videos/");
			}
		});

		buttonBoth.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Copy videos and pictures
			}
		});
	}

	/**
	 * Copy files to new directory
	 * 
	 * Code modifed from:
	 * http://stackoverflow.com/questions/10735273/copy-folders
	 * -in-data-data-to-sdcard-viceversa
	 */
	private void copyDirectory(String type) {
		String toastText = "";
		String copyString = "cp ";

		if (type.equals("/pictures/")) {
			toastText = getString(R.string.progress_1_copy_picture)
					+ getString(R.string.location_pictures);
			// TODO: Don't make this a hardcoded string huh
			copyString = copyString
					+ "/data/data/com.snapchat.android/cache/received_image_snaps/* ";
		} else if (type.equals("/videos/")) {
			toastText = getString(R.string.progress_1_copy_video)
					+ getString(R.string.location_videos);
		}

		// Toast the copy
		Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT)
				.show();

		// Add their storage location
		copyString = copyString + storageLocation + type;

		Log.d("Snaps_Copy", "CopyString: " + copyString);

		// Do copy
		// TODO: Need to somehow figure out how many files were copied, or if no
		// files were copied
		try {
			Process suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					suProcess.getOutputStream());

			os.writeBytes(copyString + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			int suProcessRetval = suProcess.waitFor();

			if (suProcessRetval != 255) {
				// We were given root
			} else {
				// No root :(
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.w("hey.rich.snapsaver", "Error getting root.");
			e.printStackTrace();
		}
	}

	/**
	 * Rename all of the files
	 * 
	 * Modified from: Modified from:
	 * http://www.tutorialspoint.com/java/io/file_listfiles_file_filter.htm
	 */
	private void renameAllFiles(String directory) {
		String toastText = "";
		String renameString ="rn ";
		String tempRename;
		File f;
		File[] files;

		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				// File must exist
				if (!pathName.isFile()) {
					Log.d("Snaps_Rename", "File does not exist");
					Log.d("Snaps_Rename", "File name was: " + pathName.getAbsolutePath());
					return false;
				}
				// File must end in .nomedia
				if (!pathName.getAbsolutePath().substring(
						pathName.getAbsolutePath().length() - 8).equals(".nomedia")) {
					Log.d("Snaps_Rename", "File did not end in .nomedia");
					Log.d("Snaps_Rename", "File name was: " + pathName.getAbsolutePath());
					return false;
				}
				/*
				 * File must be like: h1a81hurcs00h1368487198510.jpg.nomedia
				 */
				// TODO: Find a way to use directory that is passed into this method
				if (!pathName.getAbsolutePath().matches(storageLocation + "/pictures/" + "h1a81hurcs00h[0-9]{13,}.jpg.nomedia")) {
					Log.d("Snaps_Rename", "File wasn't cool looking");
					Log.d("Snaps_Rename", "File name was: " + pathName.getAbsolutePath());
					return false;
				}
				return true;
			}
		};

		if (directory.equals("/pictures/")) {
			toastText = getString(R.string.progress_2_rename_picture)
					+ storageLocation + "/pictures/";
			// TODO: Don't make this a hardcoded string
			renameString = renameString + "";

		} else if (directory.equals("/videos/")) {
			toastText = getString(R.string.progress_2_rename_video)
					+ storageLocation
					+ "/videos/";

		}
		try{
			f = new File(storageLocation + directory);

			files = f.listFiles(filter);

			// Toast the rename
			Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT)
			.show();
			// Do rename
			for(File path:files)
			{
				tempRename = path.getAbsolutePath();

				Log.d("Snaps_Rename", "Old name: " + tempRename);

				tempRename = tempRename.substring(0, tempRename.length() - 8);

				Log.d("Snaps_Rename", "New name: " + tempRename);

				if(!path.renameTo(new File(tempRename)))
				{
					Log.d("Snaps_Rename", "File was not renamed correctly.");
				}
				
				Log.d("Snaps_Rename", "Actual new name is: " + path.getAbsolutePath());
			}
		} catch(NullPointerException e)
		{
			e.printStackTrace();
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
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings_change_directory: {
			// Change directory
			changeDirectory(this);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Creates a dialog to allow the user to change their current directory
	 * 
	 * Modified from mkoyong:
	 * http://www.mkyong.com/android/android-custom-dialog-example/
	 * */
	private void changeDirectory(Context context) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.change_directory);
		dialog.setTitle(R.string.dialog_new_directory);

		// Set the custom dialog components
		final EditText text = (EditText) dialog
				.findViewById(R.id.dialog_edit_text);

		// Setting text to be what every it is
		text.setText(storageLocation);
		Button saveButton = (Button) dialog
				.findViewById(R.id.dialog_positive_button);
		Button cancelButton = (Button) dialog
				.findViewById(R.id.dialog_negative_button);

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: Save the text
				storageLocation = text.getText().toString();

				// Dismiss the dialog
				dialog.dismiss();

				// Set the EditText to be the storageLocation text
				text.setText(storageLocation);
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Dismiss the dialog
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs = this.getSharedPreferences("hey.rich.SnapSaver",
				Context.MODE_PRIVATE);
		prefEditor.putString("storageLocation", storageLocation);
		prefEditor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		storageLocation = prefs.getString("storageLocation",
				getString(R.string.default_location));

	}
}
