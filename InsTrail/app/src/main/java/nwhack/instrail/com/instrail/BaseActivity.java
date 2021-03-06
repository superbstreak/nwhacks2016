package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Window;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramRequest;
import net.londatiga.android.instagram.InstagramSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nwhack.instrail.com.instrail.Controller.BaseController;
import nwhack.instrail.com.instrail.Controller.InstagramController;
import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.Interface.DataListener;
import nwhack.instrail.com.instrail.Interface.UpdateListener;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;
import nwhack.instrail.com.instrail.Model.User;

/**
 * Created by Rob on 2/29/2016.
 */
public class BaseActivity extends FragmentActivity {

    public static Activity baseContext;
    public static DataListener currentDataListener;
    public static UpdateListener currentUpdateListener;
    public Context appContext;
    public static int currentMax = Constant.MAX_CALL;
    public static int currentCount = 1;
    public static int currentFilter = 0;
    public static boolean isFirstLoad = true;
    public static String nextActionURL = Constant.FIRST_URL;
    protected Instagram mInstagram;
    protected InstagramSession mInstagramSession;
    protected InstagramRequest instagramRequest;
    public static User user;
    public static List<Trail> userTrails;
    public Dialog LoadingDialog;
    public static ImageLoader il;
    public static VolleyController requestController;
    public static List<InstData> mainData = new ArrayList<>();
    public List<InstData> localData = new ArrayList<>();
    public static List<Trail> trails = new ArrayList<>();
    public static HashMap<String, Integer> trailMapper = new HashMap<>();
    public static InstagramController scrapper;
    public static int[] metric;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeImageLoader();
        baseContext = this;
        mInstagram = new Instagram(this, Constant.CLIENT_ID, Constant.CLIENT_SECRET, Constant.REDIRECT_URI);
    }

    @Override
    public void onResume() {
        super.onResume();
        baseContext = this;
        appContext = this.getApplicationContext();
        BaseController.appContext = getApplicationContext();
        getVolleyController(); // place here to make sure it never dies
        initializeImageLoader();
    }

    public InstagramController getScrapper() {
        if (scrapper == null) {
            scrapper = new InstagramController();
        }
        return scrapper;
    }

    private void initializeImageLoader() {
        if (il == null) {
            il = ImageLoader.getInstance();
            il.init(ImageLoaderConfiguration.createDefault(this));
        }
    }

    public void ShowLoadingDialog() {
        if (LoadingDialog != null && LoadingDialog.isShowing()) {
        } else {
            LoadingDialog = new Dialog(this, android.R.style.Theme_Translucent);
            LoadingDialog.setCancelable(false);
            LoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            LoadingDialog.setContentView(R.layout.load_hub);
            Window window = LoadingDialog.getWindow();
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            window.setLayout((int) (w * 0.30), (int) (w * 0.30));
            LoadingDialog.show();
        }
    }

    // Singleton getters
    public Context getAppContext() {
        return this.appContext;
    }

    public static DataListener getCurrentDataListener() {
        return currentDataListener;
    }

    public static void setCurrentDataListener(DataListener datalist) {
        currentDataListener = datalist;
    }


    public static VolleyController getVolleyController() {
        if (requestController == null) {
            requestController = new VolleyController();
        }
        return requestController;
    }

    public static int getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(int select) {
        currentFilter = select;
    }

    public List<Trail> getTrails() {
        if (trails == null) {
            trails = new ArrayList<>();
        }
        return trails;
    }

    public void setLocalData(List<InstData> data) {
        this.localData = data;
    }

    public List<InstData> getLocalData() {
        return this.localData;
    }

    public static void notifyObserver() {
        if (currentUpdateListener != null) {
            try {
                currentUpdateListener.onDataUpdate();
            } catch (Exception e) {
            }
        }
    }

    public void scrapeNextURL() {
        if (nextActionURL != null) {
            if (isFirstLoad) {
                isFirstLoad = false;
                getScrapper().getTagRecentMedia(nextActionURL, false);
            } else if (nextActionURL != null && currentCount < currentMax) {
                getScrapper().getTagRecentMedia(nextActionURL, true);
            }
        }
    }

    public static int[] getDeviceMetric() {
        if (metric == null && baseContext != null) {
            Display display = baseContext.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            metric = new int[2];
            metric[0] = size.x;
            metric[1] = size.y;
        }

        return metric;
    }
}
