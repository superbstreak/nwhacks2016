package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.Adapter.PhotoAdapter;
import nwhack.instrail.com.instrail.Interface.DataListener;
import nwhack.instrail.com.instrail.Model.InstData;

public class Photos extends MainActivity implements DataListener, AdapterView.OnItemClickListener{

    private Dialog photoPopup;
    private Activity context;
    private GridView gridView;
    private TextView noPtotoText;
    private PhotoAdapter adapter;
    private String incoming_tag = null;
    private int incoming_trailPos = 0;
    private LinearLayout backButton;
    private ArrayList<InstData> data;
    private int lastPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        Intent intent = getIntent();
        incoming_tag = null;

        try {
            incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
        } catch (Exception e) {
            // ignore
        }

        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            setLocalData(MainActivity.mainData);
        } else if (incoming_tag != null && incoming_tag.equals(Constant.PHOTO_TAG_TRAIL)) {
            // search specified data
            try {
                incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
                incoming_trailPos = intent.getIntExtra(Constant.TRAIL_POSITION_TAG, 0);
            } catch (Exception e) {}
        }

        noPtotoText = (TextView) this.findViewById(R.id.photo_no_photo);
        gridView = (GridView) this.findViewById(R.id.photo_gridView);
        backButton = (LinearLayout) this.findViewById(R.id.photo_header);
    }

    @Override
    public void onResume() {
        super.onResume();

        setCurrentDataListener(this);

        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            data = getLocalData();
            adapter = new PhotoAdapter(this, getLocalData());
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        } else if (incoming_tag != null && incoming_tag.equals(Constant.PHOTO_TAG_TRAIL)) {
            data =  getTrails().get(incoming_trailPos).getData();
            adapter = new PhotoAdapter(this,data);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        }

//        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == SCROLL_STATE_IDLE)  {
//                    int pagecnt = (int)gridView.getFirstVisiblePosition() +10;
//                    if (data != null && data.size() >= pagecnt) {
//                        lastPos = gridView.getFirstVisiblePosition();
//                        MainActivity.scrapper.getTagRecentMedia(MainActivity.nextActionURL, true);
//                    }
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });

        context = this;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish current activity, back to the previous one on top of stack
            }
        });
    }

    private void showPhotoPopUp(final String url) {
        if (photoPopup != null && photoPopup.isShowing()) {
            photoPopup.dismiss();
        }
        if (!this.context.isFinishing()) {
            photoPopup = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            photoPopup.setContentView(R.layout.photo_popup);

            Window window = photoPopup.getWindow();
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            int h = size.y;
            window.setLayout(w, h);

            final ImageView photo = (ImageView) photoPopup.findViewById(R.id.photo_ZoomImageView);
            final ImageView close_photo = (ImageView) photoPopup.findViewById(R.id.photo_closeZoom);

            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            photo.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            photo.setImageResource(R.drawable.ic_photo);
                        }
                    });

            MainActivity.getVolleyController().addToRequestQueue(request);

            close_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoPopup != null) {
                        photoPopup.dismiss();
                    }
                }
            });

            photoPopup.show();
        }
     }


    @Override
    public void onDataReceive(ArrayList<InstData> ddata,  String nextAction) {
        MainActivity.nextActionURL = nextAction;
        // data receive goes here
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            setLocalData(ddata);
            data = ddata;
//            adapter = new PhotoAdapter(this,data);
//            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
//            gridView.scrollTo(0,lastPos);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDataLoading(String nextAction) {
        // SHOULD NEVER REACH HERER, SOMETHING WENT WRONG IF IT DID, LOG IT
        Log.d("PHOTO","on data loading received.");
    }

    @Override
    public void onDataError() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // perfrom popup
        showPhotoPopUp(data.get(position).getLargeURL());
    }
}
