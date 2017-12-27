package my.edu.tarc.lab45networkimage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import my.edu.tarc.lab45networkimage.Model.ImageAdaptor;
import my.edu.tarc.lab45networkimage.Model.ImageDataSource;
import my.edu.tarc.lab45networkimage.Model.ImageFile;

public class MainActivity extends AppCompatActivity {
    private ImageView imageViewPreview;
    private ListView listViewImage;
    private List<ImageFile> imageArrayList;
    private ImageDataSource imageDataSource;
    private ImageAdaptor imageAdaptor;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);

        imageArrayList = new ArrayList<>();

        //Create data source
        imageDataSource = new ImageDataSource(this);

        imageViewPreview = (ImageView) findViewById(R.id.imageViewPresent);

        listViewImage = (ListView) findViewById(R.id.listView);
        listViewImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //loadImage(imageArrayList.get(position).getId());
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        } else if (id == R.id.action_refresh) {
            downloadImages(this, getString(R.string.url_get_all_images));
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadImages(Context context, String url) {

        RequestQueue queue = Volley.newRequestQueue(context);
        if (!progressDialog.isShowing())
            progressDialog.setMessage("Syn with server...");

        progressDialog.show();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            imageArrayList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject imageResponse = (JSONObject) response.get(i);
                                int id = imageResponse.getInt("id");
                                String image = imageResponse.getString("image");
                                ImageFile imageFile = new ImageFile(id, image);
                                imageArrayList.add(imageFile);
                            }
                            loadImageFiles();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getApplicationContext(), "Error" + volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });
        queue.add(jsonObjectRequest);
    }

    private void loadImageFiles() {
        final ImageAdaptor adapter = new ImageAdaptor(this, R.layout.content_main, imageArrayList);
        listViewImage.setAdapter(adapter);
    }
}
