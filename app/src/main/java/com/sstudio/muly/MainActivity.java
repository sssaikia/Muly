package com.sstudio.muly;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IACRCloudListener {


    //private Button              btnACR         = null;  /* Start/Stop ID buttion */
    private TextView txtResult = null; /* Result messages */

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;


    private boolean mProcessing = false;
    private boolean initState = false;

    private String path = "";

    TextView mVolume;

    private long startTime = 0;
    private long stopTime = 0;
    boolean clicked=false;
    private String token;
    private String VideoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*My space*/

        txtResult = (TextView) findViewById(R.id.result);
        mVolume = (TextView) findViewById(R.id.vol);

        //btnACR = (Button) findViewById(R.id.test);

        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }




        Button startBtn = (Button) findViewById(R.id.strt);
        startBtn.setText("Start");


        findViewById(R.id.strt).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (clicked){
                    stop();
                    clicked=false;
                }else {
                    start();
                    clicked=true;
                }

            }
        });



        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;

        // If you implement IACRCloudResultWithAudioListener and override "onResult(ACRCloudResult result)", you can get the Audio data.
        //this.mConfig.acrcloudResultWithAudioListener = this;

        this.mConfig.context = this;
        this.mConfig.host = "identify-ap-southeast-1.acrcloud.com";
        this.mConfig.dbPath = path; // offline db path, you can change it with other path which this app can access.
        this.mConfig.accessKey = "f0049cb691ceb842b0227df5901c53b6";
        this.mConfig.accessSecret = "OMFRnUn5YoQxesrC6UZ5MD6DpjwK4DpusIoDZw4R";
        this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTPS
        this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

        this.mClient = new ACRCloudClient();
        // If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
        // the function initWithConfig is used to load offline db, and it may cost long time.
        this.initState = this.mClient.initWithConfig(this.mConfig);
        if (this.initState) {
            this.mClient.startPreRecord(3000); //start prerecord, you can call "this.mClient.stopPreRecord()" to stop prerecord.
        }


        ((Button)findViewById(R.id.down)).setVisibility(View.GONE);




        /*Auto generated stuff*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                cancel();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
/*...................................*/
public void start() {
    ((Button)findViewById(R.id.down)).setVisibility(View.GONE);
    if (!this.initState) {
        Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
        return;
    }

    if (!mProcessing) {
        mProcessing = true;
        mVolume.setText("");
        txtResult.setText("");
        if (this.mClient == null || !this.mClient.startRecognize()) {
            mProcessing = false;
            txtResult.setText("start error!");
        }
        startTime = System.currentTimeMillis();
    }
}

    protected void stop() {
        txtResult.setText("");
        if (mProcessing && this.mClient != null) {
            this.mClient.stopRecordToRecognize();
        }
        mProcessing = false;

        stopTime = System.currentTimeMillis();
    }

    protected void cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
        }
    }
    // Old api
    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }

        String tres = "\n";
        Log.d("JSON"," "+result);

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("humming")) {
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for(int i=0; i<hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres + title + "\n";
                    }
                }
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<1; i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres + "Title: " + title + "\nArtist: " + artist + "\n";
                        JSONObject link= tt.getJSONObject("external_metadata");
                        if (link.has("youtube")){
                            JSONObject you=link.getJSONObject("youtube");
                            String youtube=you.getString("vid");
                            tres=tres+"\n\n"+youtube;
                        }
                        w3hilljson(title);
                    }
                }
                if (metadata.has("streams")) {
                    JSONArray musics = metadata.getJSONArray("streams");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                        tres = tres +"Title: " + title + "Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        tres = tres +"Title: " + title + "\n";
                    }
                }


            }else{
                tres = j2+"  xxx  ";
            }
        } catch (JSONException e) {
            tres = result;
            e.printStackTrace();
        }

        txtResult.setText(tres);
    }
    RequestQueue queue;
    public void w3hilljson(String title){
         queue= Volley.newRequestQueue(MainActivity.this);
        JsonRequest request= new JsonObjectRequest(Request.Method.GET,
                "https://api.w3hills.com/youtube/search?keyword="+ URLEncoder.encode(title)+"&api_key=B04F419A-CA22-1C7C-E81F-7C57062CC9C9",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray videos=response.getJSONArray("videos");
                        JSONObject obj = videos.getJSONObject(0);
                        String thumbnail= obj.getString("thumbnail");
                        token= obj.getString("token");
                        VideoID= obj.getString("id");
                        thumbLoad(URLDecoder.decode(thumbnail));
                    ((Button)findViewById(R.id.down)).setVisibility(View.VISIBLE);
                    ((Button)findViewById(R.id.down)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            downLoad(VideoID,token);
                            Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    public  void thumbLoad(String url){
        ImageRequest request= new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                ((ImageView)findViewById(R.id.thumb)).setImageBitmap(response);
            }
        }, 100, 100, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }
    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
       //mVolume.setText("Volume: " + volume + "\n\n" + time + " s");
        GradientDrawable gn = new GradientDrawable();
        gn.setCornerRadius((float)volume*1500);
        gn.setColor(Color.RED);
        ((Button)findViewById(R.id.strt)).setHeight((int) (volume*1000));
        ((Button)findViewById(R.id.strt)).setWidth((int) (volume*1000));
        //((GradientDrawable)mVolume.getBackground()).setGradientRadius((float) volume);
    }

    public void downLoad(String id,String token){

        queue= Volley.newRequestQueue(MainActivity.this);
        JsonRequest request= new JsonObjectRequest(Request.Method.GET,
                "https://api.w3hills.com/youtube/get_video_info?video_id="+ id+"&token="+token+"&api_key=B04F419A-CA22-1C7C-E81F-7C57062CC9C9",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Download method", "onResponse: response download");
                try {
                    JSONObject obj = response.getJSONObject("links");
                    JSONArray audio=obj.getJSONArray("audios");
                    JSONObject mp3=audio.getJSONObject(0);
                    String url=mp3.getString("url");
                    mp3(url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }
    public void mp3(String u){
        WebView webView=(WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        //webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setUserAgentString("customAgent");
        webView.loadUrl("http://api.youtube6download.top/fetch/link.php?i="+VideoID);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Log.d("Mp#", "  mp3: downloading"+Uri.parse(s));
                //Download_Uri = Uri.parse("http://www.gadgetsaint.com/wp-content/uploads/2016/11/cropped-web_hi_res_512.png");

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle("Downloading " + "Sample" + ".mp3");
                request.setDescription("Downloading " + "Sample" + ".mp3");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Muly/"  + "/" + URLUtil.guessFileName(s,s1,s2));
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }else {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }
    /*....................................*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
