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

public class WeeklyAuthActivity extends AppCompatActivity {

    private ChallengeDatabase db;
    private EditText authMemoEdit;
    private ImageView authImageView;
    private int challengeId = -1;

    ActivityResultLauncher<Intent> takePictureLauncher;
    ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ activity_weekly_auth 레이아웃을 사용하는 것만 다릅니다.
        setContentView(R.layout.activity_weekly_auth);

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

        loadExistingAuthData();

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
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

        saveButton.setOnClickListener(v -> {
            String memo = authMemoEdit.getText().toString();
            if (challengeId != -1) {
                new Thread(() -> {
                    Challenge challengeToUpdate = db.challengeDao().getChallengeById(challengeId);
                    if (challengeToUpdate != null) {
                        challengeToUpdate.auth_memo = memo;
                        db.challengeDao().update(challengeToUpdate);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "인증이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                }).start();
            }
        });
    }

    private void loadExistingAuthData() {
        if (challengeId != -1) {
            new Thread(() -> {
                Challenge challenge = db.challengeDao().getChallengeById(challengeId);
                if (challenge != null && challenge.auth_memo != null) {
                    runOnUiThread(() -> authMemoEdit.setText(challenge.auth_memo));
                }
            }).start();
        }
    }

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