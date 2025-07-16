package com.event.youthSphere.adapters;

import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.models.users.ParticipantUser;

import java.util.ArrayList;
import java.util.List;

public class ChildrenListAdapter extends RecyclerView.Adapter<ChildrenListAdapter.ChildViewHolder> {

    private List<ParticipantUser> childList = new ArrayList<>();
    private OnFeedbackClickListener feedbackClickListener;

    // functional interface
    public interface OnFeedbackClickListener {
        void onGiveFeedbackClick(ParticipantUser user);
    }

    public ChildrenListAdapter(OnFeedbackClickListener listener) {
        this.feedbackClickListener = listener;
    }

    public void submitList(List<ParticipantUser> list) {
        this.childList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ParticipantUser child = childList.get(position);
        holder.tvChildName.setText(child.getFullName());
        holder.tvChildEmail.setText(child.getEmail());

        holder.btnGiveFeedback.setOnClickListener(v -> {
            if (feedbackClickListener != null) {
                feedbackClickListener.onGiveFeedbackClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildName, tvChildEmail;

        Button btnGiveFeedback;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvChildEmail = itemView.findViewById(R.id.tvChildEmail);
            btnGiveFeedback = itemView.findViewById(R.id.btnGiveFeedback);
        }
    }
}
