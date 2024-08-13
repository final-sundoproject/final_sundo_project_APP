package com.example.sundo_project_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

import java.io.IOException;

public class EmailFindActivity extends AppCompatActivity {

    private EditText companyNameInput;
    private EditText companyAddressInput;
    private Button emailFindButton;
    private TextView emailResultTextView;

    private static final String EMAIL_FIND_URL = "http://10.0.2.2:8080/api/companies/find-email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_find);

        companyNameInput = findViewById(R.id.companyNameInput);
        companyAddressInput = findViewById(R.id.companyAddressInput);
        emailFindButton = findViewById(R.id.emailFindButton);
        emailResultTextView = findViewById(R.id.emailResultTextView);

        emailFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String companyName = companyNameInput.getText().toString();
                String companyAddress = companyAddressInput.getText().toString();

                if (companyName.isEmpty() || companyAddress.isEmpty()) {
                    Toast.makeText(EmailFindActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    findEmail(companyName, companyAddress);
                }
            }
        });
    }

    private void findEmail(String companyName, String companyAddress) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(EMAIL_FIND_URL).newBuilder()
                .addQueryParameter("companyName", companyName)
                .addQueryParameter("companyAddress", companyAddress)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EmailFindActivity.this, "이메일 찾기 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        emailResultTextView.setText("이메일: " + responseBody);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(EmailFindActivity.this, "이메일 찾기 실패: 다시 시도하세요", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}