package kr.hnu.ice.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ChallengeDatabase db;
    private ProgressBar dailyProgressBar;
    private TextView dailyProgressText;
    private ProgressBar weeklyProgressBar;
    private TextView weeklyProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = ChallengeDatabase.getDatabase(this);

        setupToolbar();
        dailyProgressBar = findViewById(R.id.daily_progressBar);
        dailyProgressText = findViewById(R.id.daily_progress_text);
        weeklyProgressBar = findViewById(R.id.weekly_progressBar);
        weeklyProgressText = findViewById(R.id.weekly_progress_text);

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ 이제 onResume에서는 진행도 업데이트만 호출합니다.
        updateProgress();
    }

    // ✅ 체크박스 초기화 기능은 사용하지 않기로 했으므로 관련 메서드는 삭제합니다.

    private void updateProgress() {
        new Thread(() -> {
            // ✅ 1. 일간 진행도: 모든 일간 챌린지를 기준으로 계산합니다.
            List<Challenge> dailyChallenges = db.challengeDao().getChallengesByType("daily");
            long dailyCompletedCount = dailyChallenges.stream().filter(c -> c.isCompleted).count();
            int dailyProgress = 0;
            if (dailyChallenges.size() > 0) {
                dailyProgress = (int) (((double) dailyCompletedCount / dailyChallenges.size()) * 100);
            }

            // ✅ 2. 주간 누적 진행도: 최근 7일 중 성공한 날의 비율로 계산합니다.
            List<Challenge> last7DaysChallenges = db.challengeDao().getDailyChallengesFromLast7Days();
            Map<String, List<Challenge>> challengesByDate = last7DaysChallenges.stream()
                    .collect(Collectors.groupingBy(c -> c.createdAt.substring(0, 10)));

            int completedDaysCount = 0;
            for (List<Challenge> dailyList : challengesByDate.values()) {
                // 그날 챌린지가 하나라도 있고, 그날의 모든 챌린지가 완료되었다면 성공한 날로 카운트
                boolean allTasksCompletedForDay = !dailyList.isEmpty() && dailyList.stream().allMatch(c -> c.isCompleted);
                if (allTasksCompletedForDay) {
                    completedDaysCount++;
                }
            }

            int weeklyProgress = (int) (((double) completedDaysCount / 7.0) * 100);

            // 주간 진행률이 100%가 되면, "일간 챌린지 모두 달성" 챌린지를 완료 처리
            if (weeklyProgress >= 100) {
                List<Challenge> weeklyChallengesForUpdate = db.challengeDao().getChallengesByType("weekly");
                for (Challenge challenge : weeklyChallengesForUpdate) {
                    if ("일간 챌린지 모두 달성".equals(challenge.title)) {
                        if (!challenge.isCompleted) {
                            challenge.isCompleted = true;
                            db.challengeDao().update(challenge);
                        }
                        break;
                    }
                }
            }

            // UI 업데이트
            int finalDailyProgress = dailyProgress;
            int finalWeeklyProgress = weeklyProgress;
            runOnUiThread(() -> {
                dailyProgressBar.setProgress(finalDailyProgress);
                dailyProgressText.setText("진행도: " + finalDailyProgress + "%");
                weeklyProgressBar.setProgress(finalWeeklyProgress);
                weeklyProgressText.setText("진행도: " + finalWeeklyProgress + "%");
            });
        }).start();
    }

    private void setupClickListeners() {
        LinearLayout dailyChallengeLayout = findViewById(R.id.daily_challenge_layout);
        dailyChallengeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, dailychallengeActivity.class);
            startActivity(intent);
        });

        LinearLayout weeklyChallengeLayout = findViewById(R.id.weekly_challenge_layout);
        weeklyChallengeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, weekChallengeActivity.class);
            startActivity(intent);
        });

        Button statsButton = findViewById(R.id.stats_button);
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatsActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
            String formattedDate = currentDate.format(formatter);
            actionBar.setTitle(formattedDate);
        }
    }
}