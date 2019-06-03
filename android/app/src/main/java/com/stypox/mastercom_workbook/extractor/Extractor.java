package com.stypox.mastercom_workbook.extractor;

import android.os.AsyncTask;

import com.stypox.mastercom_workbook.data.MarkData;
import com.stypox.mastercom_workbook.data.SubjectData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Extractor {
    private static final String authenticationUrl = "https://rosmini-tn.registroelettronico.com/mastercom/register_manager.php?user={user}&password={password}";
    private static final String subjectsUrl = "https://rosmini-tn.registroelettronico.com/mastercom/register_manager.php?action=get_subjects";
    private static final String marksUrl = "https://rosmini-tn.registroelettronico.com/mastercom/register_manager.php?action=get_grades_subject&id_materia={subject_id}";

    private static String authenticationCookie;

    ///////////
    // UTILS //
    ///////////

    private static String readAll(HttpURLConnection urlConnection) throws IOException {
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            urlConnection.disconnect();
        }
    }

    private static JSONObject fetchJsonAuthenticated(URL url) throws Exception {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Cookie", authenticationCookie); // auth cookie
            String response = readAll(urlConnection);

            return new JSONObject(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Network error");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception("Malformed JSON");
        }
    }


    ////////////////////
    // AUTHENTICATION //
    ////////////////////

    private static class AuthenticationTask extends AsyncTask<URL, String, Void> {
        private AuthenticationCallback callback;
        private String cookieToSet = null;
        private JSONObject jsonResponse = null;

        AuthenticationTask(AuthenticationCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                String response = readAll(urlConnection);

                cookieToSet = urlConnection.getHeaderField("Set-Cookie"); // only takes the last Set-Cookie it finds
                jsonResponse = new JSONObject(response);
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("Network error");
            } catch (JSONException e) {
                e.printStackTrace();
                publishProgress("Website returned invalid JSON");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... error) {
            callback.onError(error[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (jsonResponse != null) {
                Extractor.authenticationCallback(cookieToSet, jsonResponse, callback);
            }
        }
    }

    private static void authenticationCallback(String cookieToSet, JSONObject jsonResponse, AuthenticationCallback callback) {
        try {
            if (jsonResponse.getBoolean("auth") == false) {
                callback.onError("Wrong user or password");
                return;
            }

            authenticationCookie = cookieToSet.substring(0, "PHPSESSID=00000000000000000000000000".length());
            String fullNameUppercase = jsonResponse.getJSONObject("result").getString("full_name");
            // TODO capitalize only first letters of the name
            callback.onAuthenticationCompleted(fullNameUppercase);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onError("Malformed JSON");
        }
    }

    public static void authenticate(String user, String password, AuthenticationCallback callback) {
        AuthenticationTask authenticationTask = new AuthenticationTask(callback);
        try {
            authenticationTask.execute(new URL(authenticationUrl
                    .replace("{user}", user)
                    .replace("{password}", password)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onError("Malformed URL");
        }
    }


    //////////////
    // SUBJECTS //
    //////////////

    private static class FetchSubjectsTask extends AsyncTask<URL, String, Void> {
        private FetchSubjectsCallback callback;
        private JSONObject jsonResponse = null;

        FetchSubjectsTask(FetchSubjectsCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                jsonResponse = fetchJsonAuthenticated(urls[0]);
            } catch (Exception e) {
                publishProgress(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... error) {
            callback.onError(error[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (jsonResponse != null) {
                Extractor.fetchSubjectsCallback(jsonResponse, callback);
            }
        }
    }

    private static void fetchSubjectsCallback(JSONObject jsonResponse, FetchSubjectsCallback callback) {
        try {
            JSONArray result = jsonResponse.getJSONArray("result");

            ArrayList<SubjectData> subjects = new ArrayList<SubjectData>();
            for (int i = 0; i < result.length(); i++) {
                subjects.add(new SubjectData(result.getJSONObject(i)));
            }

            callback.onFetchSubjectsCompleted(subjects);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onError("Malformed JSON");
        }
    }

    public static void fetchSubjects(FetchSubjectsCallback callback) {
        FetchSubjectsTask fetchSubjectsTask = new FetchSubjectsTask(callback);
        try {
            fetchSubjectsTask.execute(new URL(subjectsUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onError("Malformed URL");
        }
    }



    //////////////
    // SUBJECTS //
    //////////////

    private static class FetchMarksTask extends AsyncTask<URL, String, Void> {
        private FetchMarksCallback callback;
        private JSONObject jsonResponse = null;

        FetchMarksTask(FetchMarksCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                jsonResponse = fetchJsonAuthenticated(urls[0]);
            } catch (Exception e) {
                publishProgress(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... error) {
            callback.onError(error[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (jsonResponse != null) {
                Extractor.fetchMarksCallback(jsonResponse, callback);
            }
        }
    }

    private static void fetchMarksCallback(JSONObject jsonResponse, FetchMarksCallback callback) {
        try {
            JSONArray result = jsonResponse.getJSONArray("result");

            ArrayList<MarkData> marks = new ArrayList<>();
            for (int i = 0; i < result.length(); i++) {
                marks.add(new MarkData(result.getJSONObject(i)));
            }

            callback.onFetchMarksCompleted(marks);
        } catch (Throwable e) {
            e.printStackTrace();
            callback.onError("Malformed JSON");
        }
    }

    public static void fetchMarks(String subjectId, FetchMarksCallback callback) {
        FetchMarksTask fetchMarksTask = new FetchMarksTask(callback);
        try {
            fetchMarksTask.execute(new URL(marksUrl
                    .replace("{subject_id}", subjectId)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onError("Malformed URL");
        }
    }

}