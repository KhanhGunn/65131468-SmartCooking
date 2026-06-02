package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1800; // 1.8 giây
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen immersive
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // Thiết lập màu status bar trong suốt
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ẩn tất cả views ban đầu
        binding.glowView.setAlpha(0f);
        binding.logoContainer.setAlpha(0f);
        binding.tvAppName.setAlpha(0f);
        binding.tvTagline.setAlpha(0f);
        binding.poweredByContainer.setAlpha(0f);

        // Load animations
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation pulseGlow = AnimationUtils.loadAnimation(this, R.anim.pulse_glow);

        // Chuỗi animation tuần tự
        binding.glowView.setAlpha(1f);
        binding.glowView.startAnimation(fadeIn);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.glowView.startAnimation(pulseGlow);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.logoContainer.setAlpha(1f);
            binding.logoContainer.startAnimation(scaleIn);
        }, 200);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tvAppName.setAlpha(1f);
            binding.tvAppName.startAnimation(slideUpFadeIn);
        }, 600);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tvTagline.setAlpha(1f);
            binding.tvTagline.startAnimation(fadeInSlow);
        }, 1000);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.poweredByContainer.setAlpha(1f);
            binding.poweredByContainer.startAnimation(fadeInSlow);
        }, 1400);

        // Sau SPLASH_DURATION, kiểm tra đăng nhập rồi chuyển hướng
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}
