package com.example.hackathon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class notices extends AppCompatActivity {


    private static final String BASE_URL = "https://gomap-bus-tracking-system-production.up.railway.app/";
    private static final String NOTICES_URL = BASE_URL + "notices/";
    private static final String API_URL = BASE_URL + "api/notices/";

    private ListView listView;
    TextView fetch;
    public static final int time=2000;
    private long backpressed;
    private ArrayList<PdfItem> pdfItems;
    private PdfAdapter adapter;

    ImageView arrowback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notices);

        listView = findViewById(R.id.listView);
        pdfItems = new ArrayList<>();
        adapter = new PdfAdapter(this, pdfItems);
        listView.setAdapter(adapter);

        fetch=findViewById(R.id.fetch);

        // Fetch PDF name
        fetchPdfNames();


        arrowback=findViewById(R.id.arrowback);

        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(notices.this, splashActivity.class);
                startActivity(intent);
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PdfItem pdfItem = pdfItems.get(position);
                String pdfUrl = API_URL + pdfItem.getFileName();
                openPdfInWebView(pdfUrl, pdfItem.getFileName());
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, splashActivity.class);
        startActivity(intent);
        finish();
    }
    private void fetchPdfNames() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, NOTICES_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            fetch.setVisibility(View.GONE);
                            for (int i = 0; i < response.length(); i++) {
                                String fileName = response.getJSONObject(i).getString("file");
                                String addedDate = response.getJSONObject(i).getString("addedDate");
                                pdfItems.add(new PdfItem(fileName, addedDate));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(notices.this, "Error in data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(notices.this, "Failed to fetch PDF name", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsonArrayRequest);
    }

    private void openPdfInWebView(String pdfUrl, String pdfName) {
        Intent intent = new Intent(notices.this, PdfViewerActivity.class);
        intent.putExtra("pdfUrl", pdfUrl);
        intent.putExtra("file", pdfName);
        startActivity(intent);
    }
}
