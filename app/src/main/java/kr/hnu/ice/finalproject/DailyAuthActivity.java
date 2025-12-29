package kr.hnu.ice.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DailyAuthActivity extends AppCompatActivity {

    private ChallengeDatabase db;
    private EditText authMemoEdit;
    private ImageView authImageView;
    private int challengeId = -1;

    ActivityResultLauncher<Intent> takePictureLauncher;
    ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_auth);

        db = ChallengeDatabase.getDatabase(this);

        authImageView = findViewById(R.id.auth_image_view);
        authMemoEdit = findViewById(R.id.auth_memo_edit);
        Button btnTakePicture = findViewById(R.id.btn_take_picture);
        Button btnLoadGallery = findViewById(R.id.btn_load_gallery);
        Button saveButton = findViewById(R.id.save_auth_button);

        Intent intent = getIntent();
        challengeId = intent.getIntExtra("CHALLENGE_ID", -1);
        String challengeTitle = intent.getStringExtra("CHALLENGE_TITLE");

        setupToolbar(challengeTitle);

        // ✅ 페이지가 열릴 때 DB에서 기존 메모를 불러오는 기능 실행
        loadExistingAuthData();

        // (카메라, 갤러리 관련 코드는 동일)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        authImageView.setImageBitmap(imageBitmap);
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        authImageView.setImageURI(result.getData().getData());
                    }
                });

        btnTakePicture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureLauncher.launch(cameraIntent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        btnLoadGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            pickImageLauncher.launch(galleryIntent);
        });

        // ✅ '인증 저장하기' 버튼 리스너 수정
        saveButton.setOnClickListener(v -> {
            String memo = authMemoEdit.getText().toString();

            if (challengeId != -1) {
                new Thread(() -> {
                    // 1. ID로 DB에서 기존 챌린지 정보를 불러옴
                    Challenge challengeToUpdate = db.challengeDao().getChallengeById(challengeId);
                    if (challengeToUpdate != null) {
                        // 2. 불러온 객체의 메모 필드를 업데이트
                        challengeToUpdate.auth_memo = memo;
                        // TODO: 이미지 경로 저장 로직은 다음에 추가

                        // 3. 업데이트된 객체로 DB에 수정 명령 실행
                        db.challengeDao().update(challengeToUpdate);

                        // 4. UI 스레드에서 사용자에게 알리고 화면 종료
                        runOnUiThread(() -> {
                            Toast.makeText(this, "인증이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // 저장 후 이전 화면으로 돌아감
                        });
                    }
                }).start();
            }
        });
    }

    // ✅ 기존 인증 데이터를 불러오는 메서드 추가
    private void loadExistingAuthData() {
        if (challengeId != -1) {
            new Thread(() -> {
                Challenge challenge = db.challengeDao().getChallengeById(challengeId);
                // DB에서 가져온 메모가 null이 아닐 때만 EditText에 설정
                if (challenge != null && challenge.auth_memo != null) {
                    runOnUiThread(() -> authMemoEdit.setText(challenge.auth_memo));
                }
                // TODO: 이미지 경로가 있다면 이미지도 불러와서 표시하는 로직 추가
            }).start();
        }
    }

    // (Toolbar, 권한 처리 등 나머지 코드는 동일)
    private void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar_auth);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (title != null && !title.isEmpty()) {
                actionBar.setTitle(title);
            } else {
                actionBar.setTitle("챌린지 인증하기");
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureLauncher.launch(intent);
        } else {
            Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}