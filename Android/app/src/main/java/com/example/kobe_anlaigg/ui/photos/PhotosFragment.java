package com.example.kobe_anlaigg.ui.photos;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.kobe_anlaigg.R;
import com.example.kobe_anlaigg.databinding.FragmentPhotosBinding;
import com.example.kobe_anlaigg.utils.MyRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PhotosFragment extends Fragment {

    private FragmentPhotosBinding binding;
    private int role = 1;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    ContentResolver resolver = getActivity().getContentResolver();
                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;

                    try {
                        inputStream = resolver.openInputStream(uri);
                        // 将输入流内容读取到一个新文件中
                        outputStream = new FileOutputStream(new File(getActivity().getFilesDir(), "image.jpg"));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    byte[] buffer = new byte[1024];
                    int len;
                    while (true) {
                        try {
                            if (!((len = inputStream.read(buffer)) != -1)) break;
                            outputStream.write(buffer, 0, len);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("filename",e.toString());
                            break;
                        }
                    }
                    try {
                        inputStream.close();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String filepath = new File(getActivity().getFilesDir(), "image.jpg").getAbsolutePath();
                    Log.d("filename",filepath);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id",role);
                        MyRequest request = new MyRequest();
                        request.post("http://47.108.160.172:7002/uploadPhoto",jsonObject.toString(),filepath);
                        updatePhoto();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        if (role == 1) {
            binding.buttonDelete2.setVisibility(View.GONE);
        } else {
            binding.buttonDelete1.setVisibility(View.GONE);
        }
        updatePhotoInit();
        binding.buttonDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyRequest request = new MyRequest();
                try {
                    request.post("http://47.108.160.172:7002/deletePhoto","{\"id\":\"1\"}","");
                    updatePhoto();
                    binding.imageView1.setImageResource(R.drawable.ic_baseline_photo_24);
                    binding.imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.buttonDelete2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyRequest request = new MyRequest();
                try {
                    request.post("http://47.108.160.172:7002/deletePhoto","{\"id\":\"2\"}","");
                    updatePhoto();
                    binding.imageView2.setImageResource(R.drawable.ic_baseline_photo_24);
                    binding.imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if(role==1)
        {
            binding.imageView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGetContent.launch("image/*");
                }
            });
        }
        else
        {
            binding.imageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGetContent.launch("image/*");
                }
            });
        }
        return root;
    }

    public void updatePhoto()
    {
        MyRequest myRequest = new MyRequest();
        byte[] response = new byte[0];
        try {
            response = myRequest.get("http://47.108.160.172:7002/getPhoto?id=1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String responseString = new String(response, StandardCharsets.UTF_8);
        if (!responseString.isEmpty()&&!responseString.equals("fail")) {
            // 更新image_view_1的图片为获取到的图片
            // 保持图片长宽比不变，但是要尽量顶满
            byte[] finalResponse = response;
            Bitmap bitmap = BitmapFactory.decodeByteArray(finalResponse, 0, finalResponse.length);
            // 设置图片的最长边和图片框对应边相等，且图片比例不变
            binding.imageView1.setScaleType(ImageView.ScaleType.MATRIX);
            binding.imageView1.setImageBitmap(bitmap);

            // 获取图片的宽和高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // 获取图片框的宽和高
            int imageViewWidth = binding.imageView1.getWidth();
            int imageViewHeight = binding.imageView1.getHeight();

            // 计算缩放比例
            float scale;
            if (width > height) {
                // 图片宽度大于高度，以图片框的宽度为基准
                scale = (float) imageViewWidth / width;
            } else {
                // 图片高度大于宽度，以图
                scale = (float) imageViewHeight / height;
            }
                    // 设置缩放比例
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            binding.imageView1.setImageMatrix(matrix);
        }
        else if(responseString.equals("fail"))
        {
            binding.imageView1.setImageResource(R.drawable.ic_baseline_photo_24);
            binding.imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        try {
            response = myRequest.get("http://47.108.160.172:7002/getPhoto?id=2");
        } catch (Exception e) {
            e.printStackTrace();
        }
        responseString = new String(response, StandardCharsets.UTF_8);
        if (!responseString.isEmpty()&&!responseString.equals("fail")) {
            // 更新image_view_1的图片为获取到的图片
            // 保持图片长宽比不变，但是要尽量顶满
            byte[] finalResponse = response;
            Bitmap bitmap = BitmapFactory.decodeByteArray(finalResponse, 0, finalResponse.length);
            // 设置图片的最长边和图片框对应边相等，且图片比例不变
            binding.imageView2.setScaleType(ImageView.ScaleType.MATRIX);
            binding.imageView2.setImageBitmap(bitmap);

            // 获取图片的宽和高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // 获取图片框的宽和高
            int imageViewWidth = binding.imageView2.getWidth();
            int imageViewHeight = binding.imageView2.getHeight();

            // 计算缩放比例
            float scale;
            if (width > height) {
                // 图片宽度大于高度，以图片框的宽度为基准
                scale = (float) imageViewWidth / width;
            } else {
                // 图片高度大于宽度，以图
                scale = (float) imageViewHeight / height;
            }
            // 设置缩放比例
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            binding.imageView2.setImageMatrix(matrix);
        }
        else if(responseString.equals("fail"))
        {
            binding.imageView2.setImageResource(R.drawable.ic_baseline_photo_24);
            binding.imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    public void updatePhotoInit()
    {
        MyRequest myRequest = new MyRequest();
        byte[] response = new byte[0];
        try {
            response = myRequest.get("http://47.108.160.172:7002/getPhoto?id=1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String responseString = new String(response, StandardCharsets.UTF_8);
        if (!responseString.isEmpty()&&!responseString.equals("fail")) {
            // 更新image_view_1的图片为获取到的图片
            // 保持图片长宽比不变，但是要尽量顶满
            byte[] finalResponse = response;
            binding.imageView1.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(finalResponse, 0, finalResponse.length);
                    // 设置图片的最长边和图片框对应边相等，且图片比例不变
                    binding.imageView1.setScaleType(ImageView.ScaleType.MATRIX);
                    binding.imageView1.setImageBitmap(bitmap);

                    // 获取图片的宽和高
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    // 获取图片框的宽和高
                    int imageViewWidth = binding.imageView1.getWidth();
                    int imageViewHeight = binding.imageView1.getHeight();

                    // 计算缩放比例
                    float scale;
                    if (width > height) {
                        // 图片宽度大于高度，以图片框的宽度为基准
                        scale = (float) imageViewWidth / width;
                    } else {
                        // 图片高度大于宽度，以图
                        scale = (float) imageViewHeight / height;
                    }
                    // 设置缩放比例
                    Matrix matrix = new Matrix();
                    matrix.setScale(scale, scale);
                    binding.imageView1.setImageMatrix(matrix);
                }
            });
        }
        else if(responseString.equals("fail"))
        {
            binding.imageView1.setImageResource(R.drawable.ic_baseline_photo_24);
            binding.imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        try {
            response = myRequest.get("http://47.108.160.172:7002/getPhoto?id=2");
        } catch (Exception e) {
            e.printStackTrace();
        }
        responseString = new String(response, StandardCharsets.UTF_8);
        if (!responseString.isEmpty()&&!responseString.equals("fail")) {
            // 更新image_view_1的图片为获取到的图片
            // 保持图片长宽比不变，但是要尽量顶满
            byte[] finalResponse = response;
            binding.imageView2.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(finalResponse, 0, finalResponse.length);
                    // 设置图片的最长边和图片框对应边相等，且图片比例不变
                    binding.imageView2.setScaleType(ImageView.ScaleType.MATRIX);
                    binding.imageView2.setImageBitmap(bitmap);

                    // 获取图片的宽和高
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    // 获取图片框的宽和高
                    int imageViewWidth = binding.imageView2.getWidth();
                    int imageViewHeight = binding.imageView2.getHeight();

                    // 计算缩放比例
                    float scale;
                    if (width > height) {
                        // 图片宽度大于高度，以图片框的宽度为基准
                        scale = (float) imageViewWidth / width;
                    } else {
                        // 图片高度大于宽度，以图
                        scale = (float) imageViewHeight / height;
                    }
                    // 设置缩放比例
                    Matrix matrix = new Matrix();
                    matrix.setScale(scale, scale);
                    binding.imageView2.setImageMatrix(matrix);
                }
            });
        }
        else if(responseString.equals("fail"))
        {
            binding.imageView2.setImageResource(R.drawable.ic_baseline_photo_24);
            binding.imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}