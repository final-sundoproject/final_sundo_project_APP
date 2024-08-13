package com.example.sundo_project_app.evaluation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.sundo_project_app.R;

import java.io.DataOutputStream;
import java.io.File;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationActivity extends AppCompatActivity {

    private SeekBar seekBar1, seekBar2, seekBar3, seekBar4;
    private Button btnSubmit;
    private TextView textViewName, textViewObserver;
    private NestedScrollView nestedScrollView;
    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluation);

        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar4 = findViewById(R.id.seekBar4);
        btnSubmit = findViewById(R.id.btn_submit);
        textViewName = findViewById(R.id.textViewName);
        textViewObserver = findViewById(R.id.textViewObserver);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        btnSubmit.setOnClickListener(v -> submitEvaluation());
    }

    private void submitEvaluation() {
        int score1 = seekBar1.getProgress();
        int score2 = seekBar2.getProgress();
        int score3 = seekBar3.getProgress();
        int score4 = seekBar4.getProgress();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL("http://10.0.2.2:8000/evaluation");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                connection.setDoOutput(true);

                try (DataOutputStream request = new DataOutputStream(connection.getOutputStream())) {
                    // 이미지 파일 저장 후 파일 전송
                    File imageFile = saveBitmapToFile();
                    addFilePart(request, "arImage", imageFile);

                    // EvaluationSaveDto 필드 전송
                    addFormField(request, "title", textViewName.getText().toString());
                    addFormField(request, "registrantName", textViewObserver.getText().toString());
                    addFormField(request, "windVolume", String.valueOf(score1));
                    addFormField(request, "noiseLevel", String.valueOf(score2));
                    addFormField(request, "scenery", String.valueOf(score3));
                    addFormField(request, "waterDepth", String.valueOf(score4));

                    request.writeBytes("--" + BOUNDARY + "--" + LINE_FEED);
                    request.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result = "평가가 성공적으로 전송되었습니다.";
                } else {
                    result = "서버 오류가 발생했습니다. 응답 코드: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "전송 중 오류가 발생했습니다: " + e.getMessage();
            }

            String finalResult = result;
            handler.post(() -> Toast.makeText(EvaluationActivity.this, finalResult, Toast.LENGTH_LONG).show());
        });
    }

    private void addFormField(DataOutputStream request, String fieldName, String fieldValue) throws Exception {
        request.writeBytes("--" + BOUNDARY + LINE_FEED);
        request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + LINE_FEED);
        request.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED); // charset=UTF-8 추가
        request.writeBytes(LINE_FEED);
        request.writeBytes(new String(fieldValue.getBytes("UTF-8"), "ISO-8859-1") + LINE_FEED); // UTF-8로 인코딩
    }

    private void addFilePart(DataOutputStream request, String fieldName, File file) throws Exception {
        String fileNameHeader = "--" + BOUNDARY + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"" + LINE_FEED +
                "Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName()) + LINE_FEED +
                "Content-Transfer-Encoding: binary" + LINE_FEED + LINE_FEED;

        Log.d("Upload", "Adding file part: " + fileNameHeader);
        request.writeBytes(fileNameHeader);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                request.write(buffer, 0, bytesRead);
            }
        }

        request.writeBytes(LINE_FEED);
    }


    private File saveBitmapToFile() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.your_image);

        File file = new File(getCacheDir(), "image.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        return file;
    }
}