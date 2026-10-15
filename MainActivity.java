package com.tom93.viettrungdich;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public class MainActivity extends Activity {
    private EditText input;
    private TextView chinese, pinyin, status;
    private Button translateButton, speakButton, fullscreenButton;
    private TextToSpeech tts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean landscape = false;
    private String translatedText = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(20, 112, 75));
        getWindow().setNavigationBarColor(Color.rgb(20, 112, 75));
        buildUi();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int languageResult = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    runOnUiThread(() -> status.setText("Máy chưa có giọng đọc tiếng Trung. Anh có thể cài dữ liệu giọng nói trong Cài đặt."));
                }
            }
        });
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView label(String text, int size, int color) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private Button button(String text) {
        Button b = new Button(this); b.setText(text);
        return b;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(18));
        root.setBackgroundColor(Color.rgb(247, 249, 247));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = label("DỊCH VIỆT – TRUNG", 23, Color.rgb(20, 112, 75));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView prompt = label("Nhập câu tiếng Việt:", 16, Color.DKGRAY);
        root.addView(prompt);
        input = new EditText(this);
        input.setHint("Ví dụ: Xin chào, tôi là người Việt Nam");
        input.setTextSize(17);
        input.setMinLines(3); input.setGravity(Gravity.TOP | Gravity.START);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.addView(input, new LinearLayout.LayoutParams(-1, dp(112)));

        translateButton = button("DỊCH SANG TIẾNG TRUNG");
        root.addView(translateButton, new LinearLayout.LayoutParams(-1, dp(54)));
        translateButton.setOnClickListener(v -> translate());

        TextView zhLabel = label("Tiếng Trung giản thể:", 15, Color.DKGRAY);
        zhLabel.setPadding(0, dp(14), 0, dp(4)); root.addView(zhLabel);
        chinese = label("中文翻译会显示在这里", 27, Color.BLACK);
        chinese.setGravity(Gravity.CENTER);
        chinese.setPadding(dp(10), dp(18), dp(10), dp(18));
        chinese.setBackgroundColor(Color.WHITE);
        root.addView(chinese, new LinearLayout.LayoutParams(-1, -2));

        TextView pyLabel = label("Phiên âm Pinyin:", 15, Color.DKGRAY);
        pyLabel.setPadding(0, dp(12), 0, dp(4)); root.addView(pyLabel);
        pinyin = label("Pinyin sẽ hiển thị ở đây", 18, Color.rgb(20, 112, 75));
        pinyin.setGravity(Gravity.CENTER);
        root.addView(pinyin, new LinearLayout.LayoutParams(-1, -2));

        speakButton = button("🔊  NGHE PHÁT ÂM");
        root.addView(speakButton, new LinearLayout.LayoutParams(-1, dp(52)));
        speakButton.setOnClickListener(v -> speak());

        fullscreenButton = button("⛶  PHÓNG TO – XOAY NGANG");
        root.addView(fullscreenButton, new LinearLayout.LayoutParams(-1, dp(54)));
        fullscreenButton.setOnClickListener(v -> toggleOrientation());

        status = label("Cần kết nối Internet để dịch. Phát âm dùng giọng tiếng Trung trên điện thoại.", 13, Color.GRAY);
        status.setPadding(0, dp(8), 0, 0); root.addView(status);
    }

    private void translate() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Anh nhập câu tiếng Việt trước nhé.", Toast.LENGTH_SHORT).show();
            return;
        }
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(input.getWindowToken(), 0);
        translateButton.setEnabled(false);
        status.setText("Đang dịch...");
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlText = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=vi&tl=zh-CN&dt=t&q="
                        + URLEncoder.encode(text, "UTF-8");
                URL url = new URL(urlText);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                reader.close();
                JSONArray response = new JSONArray(body.toString());
                JSONArray chunks = response.getJSONArray(0);
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < chunks.length(); i++) {
                    JSONArray chunk = chunks.optJSONArray(i);
                    if (chunk != null && !chunk.isNull(0)) result.append(chunk.optString(0, ""));
                }
                String zh = result.toString().trim();
                if (zh.isEmpty()) throw new Exception("Không nhận được bản dịch.");
                runOnUiThread(() -> {
                    translatedText = zh;
                    chinese.setText(zh);
                    pinyin.setText(toPinyin(zh));
                    status.setText("Đã dịch xong.");
                    translateButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("Dịch chưa thành công. Anh kiểm tra Internet rồi thử lại nhé.");
                    translateButton.setEnabled(true);
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private String toPinyin(String text) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            try {
                String[] py = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (py != null && py.length > 0) out.append(py[0]);
                else out.append(c);
            } catch (Exception ex) { out.append(c); }
            if (i < text.length() - 1 && Character.toString(c).matches("[\\u4e00-\\u9fff]")) out.append(' ');
        }
        return out.toString();
    }

    private void speak() {
        if (translatedText.isEmpty()) {
            Toast.makeText(this, "Anh dịch câu trước rồi hãy nghe nhé.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tts != null) {
            int result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Điện thoại chưa có dữ liệu giọng nói tiếng Trung.", Toast.LENGTH_LONG).show();
                return;
            }
            tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "viet_trung_translation");
        }
    }

    private void toggleOrientation() {
        landscape = !landscape;
        if (landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            chinese.setTextSize(landscape ? 44 : 27);
            chinese.setPadding(dp(8), dp(8), dp(8), dp(8));
            fullscreenButton.setText("↩  THU NHỎ – QUAY VỀ MÀN HÌNH DỌC");
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            chinese.setTextSize(27);
            fullscreenButton.setText("⛶  PHÓNG TO – XOAY NGANG");
        }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        executor.shutdownNow();
        super.onDestroy();
    }
}
