package com.event.youthSphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityModel;


public class ActivityAdapter extends ListAdapter<ActivityModel, ActivityAdapter.ActivityViewHolder> {

    public ActivityAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ActivityModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<ActivityModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = getItem(position);
        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("Domain: " + activity.getDomain());
        holder.tvAgeRange.setText("Age: " + activity.getMinAge() + " - " + activity.getMaxAge());
        holder.tvParticipants.setText("Participants: " + activity.getRegisteredCount() + "/" + activity.getMaxParticipants());
        holder.tvDays.setText("Days: " + String.join(", ", activity.getDays()));
        holder.tvMonth.setText("Month: " + activity.getMonth());
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvAgeRange, tvParticipants, tvDays, tvMonth;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvAgeRange = itemView.findViewById(R.id.tvAgeRange);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
}
