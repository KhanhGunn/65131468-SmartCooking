package gun.edu.smartcooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        if (tvLogin != null) tvLogin.setOnClickListener(v -> finish());
        if (btnRegister != null) btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etFullName.setError("Nhập họ tên"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Nhập email"); return; }
        if (password.length() < 6) { etPassword.setError("Tối thiểu 6 ký tự"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Mật khẩu không khớp"); return; }

        setLoading(true);
        Log.d(TAG, "Bắt đầu đăng ký cho: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Auth: Tạo tài khoản thành công");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 1. Cập nhật tên hiển thị (chạy ngầm)
                            updateDisplayName(user, name);
                            
                            // 2. Lưu thông tin vào Database (chạy ngầm)
                            FirebaseHelper.getInstance().saveUserProfile(user.getUid(), name, email);

                            // 3. Chuyển màn hình ngay lập tức
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công! 🎉", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        setLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Log.e(TAG, "Đăng ký thất bại: " + error);
                        Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateDisplayName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) Log.d(TAG, "Đã cập nhật tên hiển thị thành công");
        });
    }

    private void setLoading(boolean isLoading) {
        if (btnRegister != null) {
            btnRegister.setEnabled(!isLoading);
            btnRegister.setAlpha(isLoading ? 0.6f : 1.0f);
            btnRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký ngay ➔");
        }
    }
}
