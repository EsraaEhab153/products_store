package com.example.jsonparsingdemo;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    TextView btnNext;
    TextView btnPrev;
    TextView tvTitle;
    TextView tvPrice;
    TextView tvBrand;
    TextView tvDescription;
    ImageView productImage;
    RatingBar productRating;

    ArrayList<Product> productList = new ArrayList<>();
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tvTitle = findViewById(R.id.tv_title);
        tvBrand = findViewById(R.id.tv_brand);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        productImage = findViewById(R.id.imageView);
        productRating = findViewById(R.id.rateBar);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex < productList.size() - 1) {
                    currentIndex++;
                    showProduct(currentIndex);
                }
            }
        });
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex > 0) {
                    currentIndex--;
                    showProduct(currentIndex);
                }
            }
        });

        class GetProducts extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(MainActivity.this, "Json Data Downloading..", Toast.LENGTH_LONG).show();

            }

            @Override
            protected Void doInBackground(Void... voids) {
                HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
                String url = "https://dummyjson.com/products";
                String jsonStr = httpRequestHandler.getHttpsRequest(url);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONArray products = jsonObj.getJSONArray("products");

                        for (int i = 0; i < products.length(); i++) {

                            JSONObject p = products.getJSONObject(i);

                            String title = p.getString("title");
                            double price = p.getDouble("price");
                            String brand = p.optString("brand", "No Brand");
                            String description = p.getString("description");
                            float rating = (float) p.getDouble("rating");
                            String imageUrl = p.getString("thumbnail");

                            Product product = new Product(title, price, brand, description, rating, imageUrl);

                            productList.add(product);
                        }
                        return null;
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
                return null;
            }

            protected void onPostExecute(Void unused) {
                if (!productList.isEmpty()) {
                    currentIndex = 0;
                    showProduct(currentIndex);
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new GetProducts().execute();
    }


    void showProduct(int index){
        Product product = productList.get(index);
        tvTitle.setText(product.getTitle());
        tvBrand.setText(product.getBrand());
        tvDescription.setText(product.getDescription());
        tvPrice.setText(Double.toString(product.getPrice()));
        productRating.setRating(product.getRating());
    }

    class HttpRequestHandler {
        public String getHttpsRequest(String reqURL) {
            String response = null;
            URL urlObj;
            HttpsURLConnection connection;
            InputStream inputStream = null;
            try {
                urlObj = new URL(reqURL);
                connection = (HttpsURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();

                    response = convertStreamToString(inputStream);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        public String convertStreamToString(InputStream inputStream) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();
        }
    }
}