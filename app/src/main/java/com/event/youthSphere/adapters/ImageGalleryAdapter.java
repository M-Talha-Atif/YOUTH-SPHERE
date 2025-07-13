package com.event.youthSphere.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityImageModel;

import java.util.List;
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

    private final List<ActivityImageModel> images;

    public ImageGalleryAdapter(List<ActivityImageModel> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ActivityImageModel image = images.get(position);
        holder.tvDate.setText("Uploaded: " + image.getUploadDate());
        holder.tvDomain.setText("Domain: " + image.getDomain());
        holder.tvName.setText("Activity: " + image.getName());

        if (image.getImageUrl() != null) {
            byte[] decodedBytes = Base64.decode(image.getImageUrl(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imgPhoto.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView tvDate, tvDomain, tvName;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            tvDate = itemView.findViewById(R.id.tvUploadDate);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvName = itemView.findViewById(R.id.tvActivityName);
        }
    }
}

