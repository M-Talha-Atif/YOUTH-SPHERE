package com.event.youthSphere.bottomsheet.instructor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseInstructorHelper;
import com.event.youthSphere.models.activity.ActivityModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UploadPhotoDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;

    private Uri imageUri;
    private String base64Image;
    private ActivityModel activity;
    private ImageView imgPreview;

    public UploadPhotoDialogFragment(ActivityModel activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_photo_dialog, container, false);

        imgPreview = view.findViewById(R.id.imgPreview);
        Button btnChoose = view.findViewById(R.id.btnChooseImage);
        Button btnUpload = view.findViewById(R.id.btnUploadImage);

        btnChoose.setOnClickListener(v -> showChooseDialog());

        btnUpload.setOnClickListener(v -> {
            if (base64Image != null && activity != null) {
                FirebaseInstructorHelper.uploadActivityImage(
                        activity.getId(),
                        base64Image,
                        activity.getDomain(),
                        activity.getName(),
                        task -> {
                            Toast.makeText(getContext(),
                                    task.isSuccessful() ? "Uploaded!" : "Failed to upload",
                                    Toast.LENGTH_SHORT).show();
                            if (task.isSuccessful()) dismiss();
                        });
            }
        });

        return view;
    }

    private void showChooseDialog() {
        String[] options = {"Choose from Gallery", "Take Photo"};
        new AlertDialog.Builder(getContext())
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else openCamera();
                }).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Bitmap bitmap = null;

            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                try (InputStream stream = requireContext().getContentResolver().openInputStream(data.getData())) {
                    bitmap = BitmapFactory.decodeStream(stream);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }

            if (requestCode == CAPTURE_IMAGE_REQUEST && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap);
                base64Image = encodeBitmapToBase64(bitmap);
            }
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        byte[] imageBytes = out.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
