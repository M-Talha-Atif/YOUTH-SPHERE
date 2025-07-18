package com.event.youthSphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticipantActivityAdapter extends RecyclerView.Adapter< ParticipantActivityAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList = new ArrayList<>();



    public void setActivityList(List<ActivityModel> list) {
        this.activityList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }


    private Set<String> enrolledActivityIds = new HashSet<>();
    private OnEnrollClickListener enrollListener;

    public interface OnEnrollClickListener {
        void onEnroll(ActivityModel model, boolean isCurrentlyEnrolled);
    }

    public ParticipantActivityAdapter() {
        super();
    }

    public void setOnEnrollClickListener(OnEnrollClickListener listener) {
        this.enrollListener = listener;
    }

    public void setEnrolledActivityIds(Set<String> ids) {
        this.enrolledActivityIds = ids != null ? ids : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_participant, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);
        boolean isEnrolled = enrolledActivityIds.contains(activity.getId());

        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText(holder.itemView.getContext().getString(R.string.domain_label, activity.getDomain()));
        holder.tvAgeRange.setText(holder.itemView.getContext().getString(R.string.age_range_label, activity.getMinAge(), activity.getMaxAge()));
        holder.tvParticipants.setText(holder.itemView.getContext().getString(R.string.participants_label, activity.getRegisteredCount(), activity.getMaxParticipants()));
        holder.tvDays.setText(holder.itemView.getContext().getString(R.string.days_label, String.join(", ", activity.getDays())));
        holder.tvMonth.setText(holder.itemView.getContext().getString(R.string.month_label, activity.getMonth()));
        holder.tvDescription.setText(activity.getDescription() != null ? activity.getDescription() : holder.itemView.getContext().getString(R.string.no_description));
        holder.tvDescription.setVisibility(View.GONE);

        holder.btnToggleDesc.setImageResource(android.R.drawable.arrow_down_float);
        holder.btnToggleDesc.setOnClickListener(v -> {
            boolean visible = holder.tvDescription.getVisibility() == View.VISIBLE;
            holder.tvDescription.setVisibility(visible ? View.GONE : View.VISIBLE);
            holder.btnToggleDesc.setImageResource(visible ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float);
        });

        holder.btnEnroll.setText(isEnrolled ? "Unregister" : "Enroll");
        holder.btnEnroll.setOnClickListener(v -> {
            if (enrollListener != null) enrollListener.onEnroll(activity, isEnrolled);
        });
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvAgeRange, tvParticipants, tvDays, tvMonth, tvDescription;
        ImageView btnToggleDesc;
        Button btnEnroll;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvAgeRange = itemView.findViewById(R.id.tvAgeRange);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
            btnEnroll = itemView.findViewById(R.id.btnEnroll);
        }
    }
}
