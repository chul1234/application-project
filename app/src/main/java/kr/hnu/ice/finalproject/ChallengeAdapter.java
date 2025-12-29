package kr.hnu.ice.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private List<Challenge> challenges = new ArrayList<>();
    private final ChallengeDao challengeDao;

    public interface OnItemClickListener {
        void onItemClick(Challenge challenge);
        void onItemLongClick(Challenge challenge);
    }
    private OnItemClickListener listener;

    public ChallengeAdapter(ChallengeDao dao) {
        this.challengeDao = dao;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ChallengeViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheckbox;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(challenges.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(challenges.get(position));
                    return true;
                }
                return false;
            });
        }
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge currentChallenge = challenges.get(position);
        holder.itemCheckbox.setText(currentChallenge.title);
        holder.itemCheckbox.setOnCheckedChangeListener(null);
        holder.itemCheckbox.setChecked(currentChallenge.isCompleted);

        // ✅ 체크박스 상태 변경 시 DB 업데이트 기능
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentChallenge.isCompleted = isChecked;
            new Thread(() -> challengeDao.update(currentChallenge)).start();
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
        notifyDataSetChanged();
    }
}