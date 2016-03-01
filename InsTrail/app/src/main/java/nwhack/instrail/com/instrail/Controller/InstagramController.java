package nwhack.instrail.com.instrail.Controller;

import android.util.Log;
import android.widget.BaseAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nwhack.instrail.com.instrail.BaseActivity;
import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.MainActivity;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;

/**
 * Created by Borislav on 2/28/2016.
 */
public class InstagramController {

    private static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    private static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    private static final String REDIRECT_URI = "com-instrail://instagramredirect";
    protected static final String ZAMA_ZINGO_ACCESS_TOKEN = "2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";
    protected static final String ZAMA_ZINGO_USER_ID = "2257996576";
    protected static final String TAG = "vancouvertrails";
    protected static final int CALLS = 3;
    private List<JSONObject> jsons = new ArrayList<>();

    public InstagramController() {
        BaseActivity.trailMapper = new HashMap<>();
        BaseActivity.mainData = new ArrayList<InstData>();
        BaseActivity.trails = new ArrayList<>();
    }

    public void getTagRecentMedia(String url, boolean isLast) {
            jsons = new ArrayList<>();
            final boolean isLastone = isLast;
            // Request a string response from the provided URL.
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("INSTAGRAM DATA", response.toString());
                            jsons.add(response);
                            String nextAction = processReturnData(response);
                            BaseActivity.currentCount += 1;
                            try {
                                if (nextAction != null) {
                                    BaseActivity.nextActionURL = nextAction;
                                    if (BaseActivity.getCurrentDataListener() != null) {
                                        if (isLastone) {
                                            BaseActivity.getCurrentDataListener().onDataReceive(processAllJson(),nextAction);
                                        } else {
                                            BaseActivity.getCurrentDataListener().onDataLoading(nextAction);
                                        }
                                    }
                                    BaseActivity.notifyObserver();
                                } else {
                                    if (BaseActivity.getCurrentDataListener() != null) {
                                        BaseActivity.getCurrentDataListener().onDataError();
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR", error.toString());

                }
            });
        BaseActivity.getVolleyController().addToRequestQueue(stringRequest);

    }

    public String processReturnData(JSONObject object){
        String result = null;
        if (object != null) {
            try {
                JSONObject page = object.getJSONObject("pagination");
                result = page.getString("next_url");
            } catch (Exception e) {
                Log.e("TEST", "19"+e);
            }
        }
        return  result;
    }

    private ArrayList<Trail> processAllJson(){
        ArrayList<Trail> tempData = new ArrayList<>();
        if (this.jsons != null) {
            int size = jsons.size();
            for (int i = 0; i< size; i++){
                try {
                    JSONObject main = jsons.get(i);
                    JSONArray data = main.getJSONArray("data");
                    int jarrSize = data.length();
                    for (int j = 0; j < jarrSize; j++) {
                        JSONObject perUser = data.getJSONObject(j);

                        // Location
                        JSONObject location;
                        String name = "";
                        double latD = 0;
                        double lonD = 0;

                        // Image
                        JSONObject images = perUser.getJSONObject("images");
                        JSONObject low = images.getJSONObject("thumbnail");
                        JSONObject mid = images.getJSONObject("low_resolution");
                        JSONObject high = images.getJSONObject("standard_resolution");
                        InstData image = new InstData(low.getString("url"), mid.getString("url"), high.getString("url"));

                        try {
                            location = perUser.getJSONObject("location");
                            String lat = location.getString("latitude");
                            String lon = location.getString("longitude");
                            name = location.getString("name")+"";
                            latD = Double.parseDouble(lat);
                            lonD = Double.parseDouble(lon);
                            if (BaseActivity.trailMapper.containsKey(name)) {
                                Trail tr = BaseActivity.trails.get(BaseActivity.trailMapper.get(name));
                                tr.addData(image);
                            } else {
                                ArrayList<InstData> dataList = new ArrayList<InstData>();
                                dataList.add(image);
                                Trail newTrail = new Trail(name,dataList,mid.getString("url"),latD,lonD);
                                BaseActivity.trailMapper.put(name, BaseActivity.trails.size());
                                BaseActivity.trails.add(newTrail);
                                tempData.add(newTrail);
                            }
                        } catch(Exception e){}

                        BaseActivity.mainData.add(image);
                    }
                } catch (Exception e){
                    Log.e("EXCE", e+"");
                }
            }
        }
        return tempData;
    }

    public List<JSONObject> getJson(){
        return this.jsons;
    }
}
