package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> loginUser());
    }

    // KIỂM TRA ĐĂNG NHẬP SẴN CÓ: Nếu người dùng đã đăng nhập trước đó, tự động chuyển thẳng vào màn hình chính
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Lấy thông tin user hiện tại từ Firebase Auth
        if (currentUser != null) {
            goToMain(); // Chuyển trang
        }
    }

    // PHƯƠNG THỨC XỬ LÝ ĐĂNG NHẬP
    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // 1. Kiểm tra tính hợp lệ của dữ liệu đầu vào (Validation)
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Vui lòng nhập email");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Vui lòng nhập mật khẩu");
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
            binding.etPassword.requestFocus();
            return;
        }

        setLoading(true); // Hiển thị trạng thái đang xử lý đăng nhập


 // Quan trọng  2. Gọi Firebase Auth API để xác thực Email và Mật khẩu
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false); // Tắt trạng thái xử lý
                    if (task.isSuccessful()) { // Đăng nhập thành công
                        Toast.makeText(this, "Đăng nhập thành công! 🎉", Toast.LENGTH_SHORT).show();
                        goToMain(); // Chuyển sang màn hình chính
                    } else { // Đăng nhập thất bại -> Phân tích mã lỗi từ Firebase để hiển thị thông báo thân thiện
                        String errorMsg = "Đăng nhập thất bại";
                        if (task.getException() != null) {
                            String exMsg = task.getException().getMessage();
                            if (exMsg != null && exMsg.contains("password")) {
                                errorMsg = "Sai mật khẩu";
                            } else if (exMsg != null && exMsg.contains("no user")) {
                                errorMsg = "Tài khoản không tồn tại";
                            } else if (exMsg != null && exMsg.contains("badly formatted")) {
                                errorMsg = "Email không hợp lệ";
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // CHUYỂN SANG MÀN HÌNH CHÍNH (MAIN ACTIVITY)
    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Thiết lập các Flag để xóa sạch ngăn xếp (Stack) các Activity trước đó, ngăn người dùng nhấn Back để quay lại màn hình Login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Hủy Activity hiện tại
    }

    // THIẾT LẬP HIỆU ỨNG LOADING TRÊN NÚT ĐĂNG NHẬP
    private void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setAlpha(isLoading ? 0.6f : 1.0f);
        binding.btnLogin.setText(isLoading ? "Đang đăng nhập..." : getString(R.string.btn_login));
    }
}
