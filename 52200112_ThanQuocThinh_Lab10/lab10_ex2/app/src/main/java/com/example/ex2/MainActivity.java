package com.example.ex2;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextView globalConfirmed, globalRecovered, globalCritical, globalDeaths;
    private TextView countryName, countryConfirmed, countryRecovered, countryCritical, countryDeaths;
    private TextView lastUpdateTextView;
    private Spinner countrySpinner;
    private ArrayList<String> countryList;
    private ArrayAdapter<String> countryAdapter;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các TextView cho toàn cầu
        globalConfirmed = findViewById(R.id.globalConfirmed);
        globalRecovered = findViewById(R.id.globalRecovered);
        globalCritical = findViewById(R.id.globalCritical);
        globalDeaths = findViewById(R.id.globalDeaths);

        // Khởi tạo các TextView cho quốc gia
        countryName = findViewById(R.id.countryName);
        countryConfirmed = findViewById(R.id.countryConfirmed);
        countryRecovered = findViewById(R.id.countryRecovered);
        countryCritical = findViewById(R.id.countryCritical);
        countryDeaths = findViewById(R.id.countryDeaths);

        // Khởi tạo thời gian cập nhật
        lastUpdateTextView = findViewById(R.id.lastUpdate);

        // Khởi tạo Spinner
        countrySpinner = findViewById(R.id.countrySpinner);
        requestQueue = Volley.newRequestQueue(this);
        countryList = new ArrayList<>();
        countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        // Lấy dữ liệu toàn cầu và danh sách quốc gia
        fetchGlobalStatsAndCountries();

        // Xử lý khi chọn quốc gia
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countryList.get(position);
                if (!selectedCountry.equals("Chọn quốc gia")) {
                    fetchCountryStats(selectedCountry);
                } else {
                    countryName.setText("Select a Country");
                    countryConfirmed.setText("0");
                    countryRecovered.setText("0");
                    countryCritical.setText("0");
                    countryDeaths.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchGlobalStatsAndCountries() {
        String url = "https://disease.sh/v3/covid-19/all";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Lấy dữ liệu toàn cầu
                            String confirmed = String.valueOf(response.getInt("cases"));
                            String recovered = String.valueOf(response.getInt("recovered"));
                            String critical = String.valueOf(response.getInt("critical"));
                            String deaths = String.valueOf(response.getInt("deaths"));
                            long timestamp = response.getLong("updated");
                            String lastUpdate = convertTimestampToDate(timestamp);

                            globalConfirmed.setText(confirmed);
                            globalRecovered.setText(recovered);
                            globalCritical.setText(critical);
                            globalDeaths.setText(deaths);
                            lastUpdateTextView.setText(lastUpdate);

                            // Lấy danh sách quốc gia
                            fetchCountryList();
                        } catch (Exception e) {
                            globalConfirmed.setText("Lỗi");
                            globalRecovered.setText("Lỗi");
                            globalCritical.setText("Lỗi");
                            globalDeaths.setText("Lỗi");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        globalConfirmed.setText("Lỗi kết nối");
                        globalRecovered.setText("Lỗi kết nối");
                        globalCritical.setText("Lỗi kết nối");
                        globalDeaths.setText("Lỗi kết nối");
                    }
                });

        requestQueue.add(request);
    }

    private void fetchCountryList() {
        String url = "https://disease.sh/v3/covid-19/countries";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            countryList.clear();
                            countryList.add("Chọn quốc gia");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject country = response.getJSONObject(i);
                                String countryNameStr = country.getString("country");
                                countryList.add(countryNameStr);
                            }
                            countryAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            countryName.setText("Lỗi khi lấy danh sách quốc gia");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        countryName.setText("Lỗi kết nối: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    private void fetchCountryStats(String countryNameStr) {
        String url = "https://disease.sh/v3/covid-19/countries/" + countryNameStr;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String confirmed = String.valueOf(response.getInt("cases"));
                            String recovered = String.valueOf(response.getInt("recovered"));
                            String critical = String.valueOf(response.getInt("critical"));
                            String deaths = String.valueOf(response.getInt("deaths"));
                            long timestamp = response.getLong("updated");
                            String lastUpdate = convertTimestampToDate(timestamp);

                            countryName.setText(countryNameStr);
                            countryConfirmed.setText(confirmed);
                            countryRecovered.setText(recovered);
                            countryCritical.setText(critical);
                            countryDeaths.setText(deaths);
                            lastUpdateTextView.setText(lastUpdate);
                        } catch (Exception e) {
                            countryName.setText("Lỗi khi lấy dữ liệu quốc gia");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        countryName.setText("Lỗi kết nối: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    private String convertTimestampToDate(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception e) {
            return "Chưa có dữ liệu";
        }
    }
}