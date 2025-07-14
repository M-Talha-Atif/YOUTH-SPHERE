package com.event.youthSphere.bottomsheet.instructor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseInstructorHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.utils.CameraPermissionHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
public class UploadPhotoDialogFragment extends DialogFragment {

    private static final int CAPTURE_IMAGE_REQUEST = 2;

    private String base64Image;
    private ActivityModel activity;
    private ImageView imgPreview;

    private Uri imageUri;

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

        btnChoose.setOnClickListener(v -> openCamera());

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



    private void openCamera() {
        try {
            File imageFile = new File(requireContext().getCacheDir(), "captured_image.jpg");
            imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    imageFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraPermissionHelper.CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // retry after permission granted
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_IMAGE_REQUEST && imageUri != null) {
            try (InputStream stream = requireContext().getContentResolver().openInputStream(imageUri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                imgPreview.setImageBitmap(bitmap);
                base64Image = encodeBitmapToBase64(bitmap);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load captured image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        byte[] imageBytes = out.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels < 1280 ? metrics.widthPixels : 1280;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }
}

