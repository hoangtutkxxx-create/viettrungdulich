package vn.tom93.viettrung;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText input;
    private TextView chinese, pinyin, status;
    private Button translateButton, speakButton, zoomButton;
    private TextToSpeech tts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean landscape = false;
    private String translatedText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, result -> {
            if (tts != null) tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        });
        buildUi();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String text, int size) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(35, 45, 55));
        return v;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(20));
        root.setBackgroundColor(Color.rgb(248, 250, 252));
        scroll.addView(root);

        TextView title = label("DỊCH VIỆT – TRUNG", 24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = label("Tiếng Trung giản thể • Pinyin • Phát âm", 14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.bottomMargin = dp(18);
        root.addView(subtitle, subParams);

        TextView inputLabel = label("Nhập câu tiếng Việt:", 16);
        inputLabel.setTypeface(null, Typeface.BOLD);
        root.addView(inputLabel);

        input = new EditText(this);
        input.setHint("Ví dụ: Xin chào, tôi là người Việt Nam");
        input.setTextSize(17);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(3);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, -2);
        inputParams.topMargin = dp(8);
        inputParams.bottomMargin = dp(10);
        root.addView(input, inputParams);

        translateButton = button("Dịch sang tiếng Trung");
        root.addView(translateButton, new LinearLayout.LayoutParams(-1, -2));

        status = label("Bản dịch cần kết nối Internet.", 13);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(8);
        root.addView(status, statusParams);

        TextView resultLabel = label("中文 · Tiếng Trung:", 16);
        resultLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams resultLabelParams = new LinearLayout.LayoutParams(-1, -2);
        resultLabelParams.topMargin = dp(16);
        root.addView(resultLabel, resultLabelParams);

        chinese = label("Bản dịch sẽ hiện ở đây", 28);
        chinese.setTextColor(Color.rgb(12, 90, 60));
        chinese.setGravity(Gravity.CENTER);
        chinese.setPadding(dp(8), dp(20), dp(8), dp(20));
        chinese.setBackgroundColor(Color.WHITE);
        root.addView(chinese, new LinearLayout.LayoutParams(-1, -2));

        pinyin = label("Pinyin: sẽ hiển thị khi dịch", 16);
        pinyin.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pinyinParams = new LinearLayout.LayoutParams(-1, -2);
        pinyinParams.topMargin = dp(10);
        root.addView(pinyin, pinyinParams);

        speakButton = button("🔊 Phát âm tiếng Trung");
        speakButton.setEnabled(false);
        LinearLayout.LayoutParams speakParams = new LinearLayout.LayoutParams(-1, -2);
        speakParams.topMargin = dp(8);
        root.addView(speakButton, speakParams);

        zoomButton = button("⛶ Phóng to màn hình (xoay ngang)");
        LinearLayout.LayoutParams zoomParams = new LinearLayout.LayoutParams(-1, -2);
        zoomParams.topMargin = dp(8);
        root.addView(zoomButton, zoomParams);

        translateButton.setOnClickListener(v -> translate());
        speakButton.setOnClickListener(v -> {
            if (tts != null && !translatedText.isEmpty()) {
                tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "viettrung");
            }
        });
        zoomButton.setOnClickListener(v -> {
            landscape = !landscape;
            setRequestedOrientation(landscape
                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            zoomButton.setText(landscape ? "↩ Thu nhỏ (xoay dọc)" : "⛶ Phóng to màn hình (xoay ngang)");
            chinese.setTextSize(landscape ? 48 : 28);
            pinyin.setTextSize(landscape ? 24 : 16);
            chinese.setMinHeight(landscape ? dp(150) : dp(70));
            chinese.setGravity(Gravity.CENTER);
        });
        setContentView(scroll);
    }


    private String toPinyin(String text) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            try {
                String[] values = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (values != null && values.length > 0) {
                    if (output.length() > 0) output.append(' ');
                    output.append(values[0]);
                } else {
                    output.append(c);
                }
            } catch (Exception ignored) {
                output.append(c);
            }
        }
        return output.toString();
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
        status.setText("Đang dịch…");
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String query = URLEncoder.encode(text, "UTF-8");
                URL url = new URL("https://api.mymemory.translated.net/get?q=" + query + "&langpair=vi|zh-CN");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setRequestProperty("User-Agent", "VietTrungAndroid/1.0");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                reader.close();
                JSONObject json = new JSONObject(body.toString());
                JSONObject response = json.getJSONObject("responseData");
                String result = response.optString("translatedText", "").trim();
                if (result.isEmpty()) throw new Exception("Không nhận được bản dịch.");
                runOnUiThread(() -> {
                    translatedText = result;
                    chinese.setText(result);
                    pinyin.setText("Pinyin: " + toPinyin(result));
                    status.setText("Dịch xong. Kiểm tra lại các câu quan trọng trước khi sử dụng.");
                    speakButton.setEnabled(true);
                    translateButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("Không dịch được. Hãy kiểm tra Internet rồi thử lại.");
                    translateButton.setEnabled(true);
                    Toast.makeText(this, "Lỗi kết nối hoặc dịch vụ dịch đang bận.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        executor.shutdownNow();
        super.onDestroy();
    }
}
