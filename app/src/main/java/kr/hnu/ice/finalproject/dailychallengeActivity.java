package kr.hnu.ice.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class dailychallengeActivity extends AppCompatActivity {

    private ChallengeDatabase db;
    private ChallengeAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailychallenge);

        db = ChallengeDatabase.getDatabase(this);

        setupToolbar();
        recyclerView = findViewById(R.id.challenge_recycler_view);
        adapter = new ChallengeAdapter(db.challengeDao());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ 클릭 및 롱클릭(삭제) 리스너를 설정합니다.
        adapter.setOnItemClickListener(new ChallengeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Challenge challenge) {
                // 항목 클릭 시 인증 페이지로 이동
                Intent intent = new Intent(dailychallengeActivity.this, DailyAuthActivity.class);
                intent.putExtra("CHALLENGE_ID", challenge.id);
                intent.putExtra("CHALLENGE_TITLE", challenge.title);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Challenge challenge) {
                // 항목을 길게 눌렀을 때 삭제 확인 다이얼로그 띄우기
                new AlertDialog.Builder(dailychallengeActivity.this)
                        .setTitle("챌린지 삭제")
                        .setMessage("'" + challenge.title + "' 챌린지를 정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            new Thread(() -> {
                                db.challengeDao().delete(challenge);
                                loadChallenges(); // 삭제 후 목록 새로고침
                            }).start();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        Button addChallengeButton = findViewById(R.id.add_challenge_button);
        addChallengeButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("새 챌린지 추가");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("추가", (dialog, which) -> {
                String challengeTitle = input.getText().toString();
                if (!challengeTitle.isEmpty()) {
                    Challenge newChallenge = new Challenge();
                    newChallenge.title = challengeTitle;
                    newChallenge.type = "daily";
                    new Thread(() -> {
                        db.challengeDao().insert(newChallenge);
                        loadChallenges();
                    }).start();
                }
            });
            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChallenges();
    }

    private void loadChallenges() {
        new Thread(() -> {
            List<Challenge> challengeList = db.challengeDao().getChallengesByType("daily");
            runOnUiThread(() -> {
                adapter.setChallenges(challengeList);
            });
        }).start();
    }

    private void setupToolbar() {
        Toolbar toolbar2 = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("일간 챌린지");
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