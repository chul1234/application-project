package kr.hnu.ice.finalproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "challenges")
public class Challenge {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    public boolean isCompleted;

    @ColumnInfo(name = "auth_memo")
    public String auth_memo;

    @ColumnInfo(name = "auth_image_path")
    public String auth_image_path;

    // ✅ 생성 날짜를 저장할 컬럼 추가
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    public String createdAt;
}