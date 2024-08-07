package com.example.sundo_project_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DmsActivity extends AppCompatActivity {

    private EditText etLatitudeDegrees, etLatitudeMinutes, etLatitudeSeconds, etLatitudeDirection;
    private EditText etLongitudeDegrees, etLongitudeMinutes, etLongitudeSeconds, etLongitudeDirection;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dms_input);

        // XML의 EditText 및 Button 참조
        etLatitudeDegrees = findViewById(R.id.et_latitude_degrees);
        etLatitudeMinutes = findViewById(R.id.et_latitude_minutes);
        etLatitudeSeconds = findViewById(R.id.et_latitude_seconds);
        etLatitudeDirection = findViewById(R.id.et_latitude_direction);

        etLongitudeDegrees = findViewById(R.id.et_longitude_degrees);
        etLongitudeMinutes = findViewById(R.id.et_longitude_minutes);
        etLongitudeSeconds = findViewById(R.id.et_longitude_seconds);
        etLongitudeDirection = findViewById(R.id.et_longitude_direction);

        btnSubmit = findViewById(R.id.btn_submit);

        // 버튼 클릭 리스너 설정
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        try {
            // 사용자가 입력한 값을 가져오기
            double latitudeDegrees = Double.parseDouble(etLatitudeDegrees.getText().toString());
            double latitudeMinutes = Double.parseDouble(etLatitudeMinutes.getText().toString());
            double latitudeSeconds = Double.parseDouble(etLatitudeSeconds.getText().toString());
            String latitudeDirection = etLatitudeDirection.getText().toString().toUpperCase();

            double longitudeDegrees = Double.parseDouble(etLongitudeDegrees.getText().toString());
            double longitudeMinutes = Double.parseDouble(etLongitudeMinutes.getText().toString());
            double longitudeSeconds = Double.parseDouble(etLongitudeSeconds.getText().toString());
            String longitudeDirection = etLongitudeDirection.getText().toString().toUpperCase();

            // 방향 값 검증 (N/S, E/W)
            if (!latitudeDirection.equals("N") && !latitudeDirection.equals("S")) {
                throw new IllegalArgumentException("Invalid latitude direction. Use 'N' or 'S'.");
            }

            if (!longitudeDirection.equals("E") && !longitudeDirection.equals("W")) {
                throw new IllegalArgumentException("Invalid longitude direction. Use 'E' or 'W'.");
            }

            // 서버로 전송할 데이터 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitudeDegrees", latitudeDegrees);
            jsonObject.put("latitudeMinutes", latitudeMinutes);
            jsonObject.put("latitudeSeconds", latitudeSeconds);
            jsonObject.put("latitudeDirection", latitudeDirection);
            jsonObject.put("longitudeDegrees", longitudeDegrees);
            jsonObject.put("longitudeMinutes", longitudeMinutes);
            jsonObject.put("longitudeSeconds", longitudeSeconds);
            jsonObject.put("longitudeDirection", longitudeDirection);

            // 서버로 데이터 전송
            sendCoordinates(jsonObject.toString());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCoordinates(String jsonData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d("DmsActivity", "jsonData: " + jsonData); // jsonData 값을 로그로 출력

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL("http://10.0.2.2:8000/location"); // 변경된 URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setDoOutput(true);

                // 데이터 전송
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result = "좌표가 성공적으로 전송되었습니다.";
                } else {
                    result = "서버 오류가 발생했습니다. 응답 코드: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "전송 중 오류가 발생했습니다: " + e.getMessage(); // 오류 메시지 포함
            }

            // 메인 스레드에서 UI 업데이트
            String finalResult = result;
            handler.post(() -> Toast.makeText(DmsActivity.this, finalResult, Toast.LENGTH_LONG).show());
        });
    }

}
