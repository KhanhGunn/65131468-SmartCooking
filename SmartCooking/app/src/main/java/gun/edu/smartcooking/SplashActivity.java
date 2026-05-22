package gun.edu.smartcooking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 giây

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

        setContentView(R.layout.activity_splash);

        // Khởi tạo views
        View glowView = findViewById(R.id.glowView);
        LinearLayout logoContainer = findViewById(R.id.logoContainer);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);
        LinearLayout poweredByContainer = findViewById(R.id.poweredByContainer);

        // Ẩn tất cả views ban đầu
        glowView.setAlpha(0f);
        logoContainer.setAlpha(0f);
        tvAppName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        poweredByContainer.setAlpha(0f);

        // Load animations
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation pulseGlow = AnimationUtils.loadAnimation(this, R.anim.pulse_glow);

        // Chuỗi animation tuần tự
        glowView.setAlpha(1f);
        glowView.startAnimation(fadeIn);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                glowView.startAnimation(pulseGlow);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            logoContainer.setAlpha(1f);
            logoContainer.startAnimation(scaleIn);
        }, 200);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvAppName.setAlpha(1f);
            tvAppName.startAnimation(slideUpFadeIn);
        }, 600);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvTagline.setAlpha(1f);
            tvTagline.startAnimation(fadeInSlow);
        }, 1000);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            poweredByContainer.setAlpha(1f);
            poweredByContainer.startAnimation(fadeInSlow);
        }, 1400);

        // Sau SPLASH_DURATION, kiểm tra đăng nhập rồi chuyển hướng
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // Đã đăng nhập → vào thẳng MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Chưa đăng nhập → vào LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}