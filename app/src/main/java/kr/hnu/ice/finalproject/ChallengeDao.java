package kr.hnu.ice.finalproject;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ChallengeDao {

    @Insert
    void insert(Challenge challenge);

    @Update
    void update(Challenge challenge);

    @Delete
    void delete(Challenge challenge);

    @Query("SELECT * FROM challenges WHERE type = :type")
    List<Challenge> getChallengesByType(String type);

    @Query("SELECT * FROM challenges WHERE id = :id")
    Challenge getChallengeById(int id);

    @Query("SELECT COUNT(*) FROM challenges")
    int getTotalChallengeCount();

    @Query("SELECT COUNT(*) FROM challenges WHERE is_completed = 1")
    int getCompletedChallengeCount();

    // ✅ 더 이상 사용하지 않으므로 삭제
    // @Query("UPDATE challenges SET is_completed = 0 WHERE type = 'daily'")
    // void resetDailyChallenges();

    @Query("SELECT * FROM challenges WHERE type = 'daily' AND date(created_at) = date('now', 'localtime')")
    List<Challenge> getTodayDailyChallenges();

    @Query("SELECT * FROM challenges WHERE type = 'daily' AND date(created_at) >= date('now', '-6 days', 'localtime')")
    List<Challenge> getDailyChallengesFromLast7Days();
}