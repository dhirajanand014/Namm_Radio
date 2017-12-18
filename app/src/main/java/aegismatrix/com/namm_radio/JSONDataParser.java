package aegismatrix.com.namm_radio;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Dhiraj on 03-04-2017.
 */

public class JSONDataParser extends AsyncTask<String, Void, String> {

    private BufferedReader bufferedReader = null;
    private String jsonObject = null;
    private StringBuffer jsonBufferData;

    public String getJSONFromUrl(String inUrl) {
        try {
            URL url = new URL(inUrl);
            URLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            InputStream stream = httpURLConnection.getInputStream();

            bufferedReader = new BufferedReader(new InputStreamReader(stream));

            jsonBufferData = new StringBuffer();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                jsonBufferData.append(line + "\n");
            }
            return jsonBufferData.toString();
        } catch (IOException e) {
            Log.e("getJSONFromUrl()", "Server_Connection_Failed " + e.toString());
            return null;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e("getJSONFromUrl()", "Server_Connection_Failed " + e.toString());
                return null;
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        return getJSONFromUrl(params[0]);
    }
}
