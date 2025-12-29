package kr.hnu.ice.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StatsActivity extends AppCompatActivity {

    private ChallengeDatabase db;
    private TextView achievementRateText;
    private TextView completedCountText;
    private TextView totalCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = ChallengeDatabase.getDatabase(this);

        setupToolbar();

        achievementRateText = findViewById(R.id.achievement_rate_text);
        completedCountText = findViewById(R.id.completed_count_text);
        totalCountText = findViewById(R.id.total_count_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            int totalCount = db.challengeDao().getTotalChallengeCount();
            int completedCount = db.challengeDao().getCompletedChallengeCount();

            int achievementRate = 0;
            if (totalCount > 0) {
                achievementRate = (int) (((double) completedCount / totalCount) * 100);
            }

            int finalAchievementRate = achievementRate;
            runOnUiThread(() -> {
                achievementRateText.setText(finalAchievementRate + "%");
                completedCountText.setText("완료한 챌린지: " + completedCount + "개");
                totalCountText.setText("전체 챌린지: " + totalCount + "개");
            });
        }).start();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("나의 통계");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}