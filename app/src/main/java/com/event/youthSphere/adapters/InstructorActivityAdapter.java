package com.event.youthSphere.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityModel;

import java.util.List;

public class InstructorActivityAdapter extends RecyclerView.Adapter<InstructorActivityAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onUploadPhoto(ActivityModel activity);
        void onGiveFeedback(ActivityModel activity);
        void onViewPhotos(ActivityModel activity);
    }

    public InstructorActivityAdapter(List<ActivityModel> activities, OnItemClickListener listener) {
        this.activityList = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_instructor, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel model = activityList.get(position);
        holder.tvName.setText(model.getName());
        holder.tvDomain.setText("Domain: " + model.getDomain());
        holder.tvMonth.setText("Month: " + model.getMonth());

        holder.btnUploadPhoto.setOnClickListener(v -> listener.onUploadPhoto(model));
        holder.btnFeedback.setOnClickListener(v -> listener.onGiveFeedback(model));
        holder.btnViewPhotos.setOnClickListener(v -> listener.onViewPhotos(model));
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth;
        Button btnUploadPhoto, btnFeedback, btnViewPhotos;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnUploadPhoto = itemView.findViewById(R.id.btnUploadPhoto);
            btnFeedback = itemView.findViewById(R.id.btnFeedback);
            btnViewPhotos = itemView.findViewById(R.id.btnViewPhotos);
        }
    }
}
