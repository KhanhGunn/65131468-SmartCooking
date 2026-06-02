package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import gun.edu.smartcooking.databinding.ActivityRegisterBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        if (binding.tvLogin != null) binding.tvLogin.setOnClickListener(v -> finish());
        if (binding.btnRegister != null) binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { binding.etFullName.setError("Nhập họ tên"); return; }
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Nhập email"); return; }
        if (password.length() < 6) { binding.etPassword.setError("Tối thiểu 6 ký tự"); return; }
        if (!password.equals(confirmPassword)) { binding.etConfirmPassword.setError("Mật khẩu không khớp"); return; }

        setLoading(true);
        Log.d(TAG, "Bắt đầu đăng ký cho: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Auth: Tạo tài khoản thành công");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateDisplayName(user, name);
                            FirebaseHelper.getInstance().saveUserProfile(user.getUid(), name, email);

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
        if (binding.btnRegister != null) {
            binding.btnRegister.setEnabled(!isLoading);
            binding.btnRegister.setAlpha(isLoading ? 0.6f : 1.0f);
            binding.btnRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký ngay ➔");
        }
    }
}
