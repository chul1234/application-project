package kr.hnu.ice.finalproject;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ✅ version을 1에서 2로 수정
@Database(entities = {Challenge.class}, version = 3)
public abstract class ChallengeDatabase extends RoomDatabase {

    public abstract ChallengeDao challengeDao();

    private static volatile ChallengeDatabase INSTANCE;

    public static ChallengeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChallengeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChallengeDatabase.class, "challenge-database")
                            // ✅ 이 코드를 추가
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}