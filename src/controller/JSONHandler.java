package controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Group;
import model.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class JSONHandler extends AsyncTask<Void, Void, ArrayList<Task>> {
	// private static final String ID_TAG = "id";
	// private static final String TITLE_TAG = "title";
	// private static final String ARRAY_TAG = "items";
	private ProgressDialog pdia;
	static boolean isFinish = false;
	private static final String CLIENT_ID = "370067889592.apps.googleusercontent.com";
	private static final String CLIENT_SEC = "bJKdrk8H5y7NSJYYT0gjt6XJ";
	private static final String API_KEY = "AIzaSyAU7IHHUUkUqprGHq_wix4fiXBAWcnn16U";
	private String token;
	private ArrayList<Group> syncGroup = new ArrayList<Group>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private ArrayList<Task> syncTasks = new ArrayList<Task>();
	private ArrayList<Task> defaultList = new ArrayList<Task>();

	public JSONHandler(Activity context, String token) {
		this.token = token;
		pdia = new ProgressDialog(context);
		
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}

	// sync down task
	private void downloadTask() {
		HttpURLConnection connect = null;
		try {
			URL url = new URL(
					"https://www.googleapis.com/tasks/v1/users/@me/lists?key="
							+ API_KEY);
			connect = (HttpURLConnection) url.openConnection();
			connect.addRequestProperty("client_id", CLIENT_ID);
			connect.addRequestProperty("client_secret", CLIENT_SEC);
			connect.setRequestProperty("Authorization", "OAuth " + token);
			connect.setReadTimeout(15000);
			connect.setConnectTimeout(15000);
			int serverCode = connect.getResponseCode();
			if (serverCode == HttpURLConnection.HTTP_OK) {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						connect.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);

				}

				JSONObject jObj = new JSONObject(sb.toString());
				syncTasks = getAllGroup(jObj);
			} else if (serverCode == 401) {
				Log.i("result", "bad authentication");
				return;
				// unknown error, do something else
			} else {
				Log.i("result", "bad task return");
				return;
			}

		} catch (Exception ex) {
			Log.e("sync task", ex.toString() + "line 97");
			ex.printStackTrace();
		} finally {
			if (connect != null) {
				connect.disconnect();
			}
		}
		return;
	}

	@SuppressWarnings("finally")
	private ArrayList<Task> getAllGroup(JSONObject jObj) {

		try {
			JSONArray jsons = jObj.getJSONArray("items");
			Log.i("result", "get group array");
			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);

				String id = o.getString("id");
				Log.i("del sync", o.getString("id"));
				Log.i("del sync", o.getString("title"));
				Group temp = new Group("");
				temp.setGID(id);
				syncGroup.add(temp);

			}
		} catch (Exception e) {
			Log.i("task sync", e.toString() + "line 125");

		} finally {
			if (syncGroup != null) {
				ArrayList<Task> tempTask = new ArrayList<Task>();
				for (int i = 0; i < syncGroup.size(); i++) {
					ArrayList<Task> temp = new ArrayList<Task>();
					temp = parseTaskPerGroup(syncGroup.get(i));
					if (i == 0) {
						defaultList.addAll(temp);
					}
					tempTask.addAll(temp);
				}

				return tempTask;
			} else {
				return null;
			}
		}
	}

	private ArrayList<Task> parseTaskPerGroup(Group groupID) {
		String listID = groupID.getGID();
		HttpURLConnection connect = null;
		try {
			URL url = new URL("https://www.googleapis.com/tasks/v1/lists/"
					+ listID + "/tasks?key=" + API_KEY);
			Log.i("sync task", url.toString());
			connect = (HttpURLConnection) url.openConnection();
			connect.addRequestProperty("client_id", CLIENT_ID);
			connect.addRequestProperty("client_secret", CLIENT_SEC);
			connect.setRequestProperty("Authorization", "OAuth " + token);
			connect.setReadTimeout(15000);
			connect.setConnectTimeout(15000);
			int serverCode = connect.getResponseCode();
			if (serverCode == HttpURLConnection.HTTP_OK) {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						connect.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				JSONObject jObj = new JSONObject(sb.toString());
				return getTaskPerGroup(jObj, groupID);

			} else if (serverCode == 401) {
				Log.i("result", "bad authentication");
				return null;
				// unknown error, do something else
			} else {
				Log.i("result", serverCode + "");
				Log.i("result", "bad task per group");
				return null;
			}

		} catch (Exception ex) {
			Log.e("sync task", ex.toString() + "line 147");
			ex.printStackTrace();
		} finally {
			if (connect != null) {
				connect.disconnect();
			}
		}
		return null;
	}

	@SuppressWarnings("finally")
	private ArrayList<Task> getTaskPerGroup(JSONObject jObj, Group group) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		try {
			JSONArray jsons = jObj.getJSONArray("items");

			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);
				String id = o.getString("id");
				Log.i("sync task", " get id task");

				String title = " ";
				try {
					title = o.getString("title");
				} catch (Exception e) {
				}
				Log.i("sync task", " get title task");

				String note = " ";
				try {
					note = o.getString("notes");
				} catch (Exception e) {
				}
				Log.i("sync task", " get note task");

				Date due = new Date();
				try {
					due = new SimpleDateFormat("yyyy-mm-dd").parse(o
							.getString("due"));
				} catch (Exception e) {
				}
				Log.i("sync task", " get due task");

				boolean isComplete = false;
				if (o.getString("status").equals("completed")) {
					isComplete = true;
				}
				Log.i("sync task", " get status task");

				boolean isDelete = false;

				try {
					if (o.getBoolean("deleted")) {
						isDelete = true;
					}
				} catch (Exception e) {
				}
				Log.i("sync task", " get delete task");

				group.setTitle("Google Sync");

				Task task = new Task(title, "High", group, due,
						new String[] {}, isComplete, note);
				task.setGID(id);
				task.setDelete(isDelete);
				tasks.add(task);

			}
		} catch (Exception e) {
			Log.i("sync task", e.toString() + "line 264");

		} finally {
			return tasks;
		}
	}

	public void synchronizeTask() {
		ArrayList<Task> mergeTask = new ArrayList<Task>();
		downloadTask();
		Log.i("insert", tasks.size() + "  size of receive task");
		if (tasks.isEmpty()) {
			mergeTask = syncTasks;
		} else if (syncTasks.isEmpty()) {
			mergeTask = tasks;
		}
		Log.i("insert", syncTasks.size() + " size of sync task");
		for (int i = 0; i < syncTasks.size(); i++) {
			for (int j = 0; j < tasks.size(); j++) {
				Task syncTask = syncTasks.get(i);
				Task task = tasks.get(j);
				if (syncTask.getGID().equals(task.getGID())) {
					if (!task.isSync()) {
						if (!syncTask.isSync()) {
							mergeTask.add(task);
							syncTasks.get(i).setSync(true);
						} else {
							mergeTask.remove(syncTask);
							mergeTask.add(task);
							tasks.get(j).setSync(true);
						}
						

					} else {
						if (!syncTask.isSync()) {
							syncTasks.get(i).setSync(true);
						} else {
							mergeTask.remove(syncTask);
						}

					}
				} else {
					if (!task.isSync()) {
						mergeTask.add(task);
						tasks.get(j).setSync(true);
					}
					if (!syncTask.isSync()) {
						mergeTask.add(syncTask);
						syncTasks.get(i).setSync(true);
					}
				}
			}
		}
		
		tasks = mergeTask;

		cleanConflict();

		Log.i("insert", tasks.size() + " size of merge");

		addTask();
	}

	private void cleanConflict() {
		for (int i = 1; i < syncGroup.size(); i++) {
			try {
				String id = syncGroup.get(i).getGID();
				URL url = new URL(
						"https://www.googleapis.com/tasks/v1/users/@me/lists/"
								+ id + "?key=" + API_KEY);
				HttpClient httpClient = new DefaultHttpClient();
				HttpDelete del = new HttpDelete(url.toURI());

				del.setHeader("client_id", CLIENT_ID);
				del.setHeader("client_secret", CLIENT_SEC);
				del.setHeader("Authorization", "OAuth " + token);
				httpClient.execute(del);

			} catch (Exception ex) {
				Log.e("sync task", ex.getMessage() + " line 339");
				ex.printStackTrace();
			}
		}

		for (int i = 0; i < defaultList.size(); i++) {
			try {
				String id = defaultList.get(i).getGroup().getGID();
				Log.i("sync task", defaultList.get(i).getGroup().getGID());
				String taskId = defaultList.get(i).getGID();
				Log.i("sync task", defaultList.get(i).getGID());
				URL url = new URL("https://www.googleapis.com/tasks/v1/lists/"
						+ id + "/tasks/" + taskId + "?key=" + API_KEY);
				HttpClient httpClient = new DefaultHttpClient();
				HttpDelete del = new HttpDelete(url.toURI());

				del.setHeader("client_id", CLIENT_ID);
				del.setHeader("client_secret", CLIENT_SEC);
				del.setHeader("Authorization", "OAuth " + token);
				httpClient.execute(del);

			} catch (Exception ex) {
				Log.e("sync task", ex.getMessage() + " line 384");
				ex.printStackTrace();
			}
		}
	}

	// sync up task
	private void addTask() {
		String listId = syncGroup.get(0).getGID();
		Log.i("insert", tasks.size() + "  size of task");
		for (int i = 0; i < tasks.size(); i++) {
			try {

				Log.i("test", tasks.get(i).getGroup().getGID() + "  "
						+ tasks.get(i).getTitle());
				String data = getString(tasks.get(i));
				URL url = new URL("https://www.googleapis.com/tasks/v1/lists/"
						+ listId + "/tasks?key=" + API_KEY);
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost post = new HttpPost(url.toURI());
				post.setHeader("client_id", CLIENT_ID);
				post.setHeader("client_secret", CLIENT_SEC);
				post.setHeader("Authorization", "OAuth " + token);
				post.setHeader("Content-Type", "application/json");

				post.setEntity(new StringEntity(data));
				HttpResponse response = httpClient.execute(post);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}

				JSONObject js = new JSONObject(sb.toString());

				tasks.get(i).setGID(js.getString("id"));

			} catch (Exception ex) {
				Log.e("sync task", ex.getMessage() + " line 405");
				ex.printStackTrace();
			}
		}
	}

	private static String getString(Task task) {
		String jsonString = "";
		String title = task.getTitle();
		String note = task.getNote();
		String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.format(task.getDueDate());
		String complete = (task.isComplete()) ? "completed" : "needsAction";
		boolean deleted = task.isDelete();
		try {
			JSONObject o = new JSONObject();
			o.put("notes", note);
			o.put("title", title);
			o.put("status", complete);
			o.put("due", dateString);
			o.put("deleted", deleted);
			jsonString = o.toString();
		} catch (Exception e) {
			Log.i("sync task", e.getMessage() + " line 427");
		}
		return jsonString;
	}

	@Override
	protected void onPostExecute(ArrayList<Task> tasks) {
		super.onPostExecute(tasks);
		if (pdia.isShowing()) {
			pdia.dismiss();
			isFinish = true;
		}
	}

	@Override
	protected void onPreExecute() {
		pdia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdia.setMessage("Synchronizing...");
		isFinish = false;
		pdia.show();
		super.onPreExecute();
		
	}

	@Override
	protected ArrayList<Task> doInBackground(Void... params) {
		synchronizeTask();
		return tasks;
	}
}
