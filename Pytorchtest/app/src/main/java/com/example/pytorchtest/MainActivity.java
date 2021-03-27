package com.example.pytorchtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private Button btnChoose;
    private Button btnChange;
    private static int RESULT_LOAD_IMAGE = 1;
    private String _picturePath;
    private Bitmap _bitmap = null;
    private Module module;

    private static final int PERMISSION_REQUEST = 1001;
    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE, Manifest.permission.READ_EXTERNAL_STORAGE};
    List<String> permissionsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btnChange = findViewById(R.id.btnChange);
        btnChoose = findViewById(R.id.btnChoose);


        initPermissions();
//        Module module = null;
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK);
                i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*");

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(_picturePath);
//                _bitmap = BitmapFactory.decodeFile(_picturePath);
                _bitmap = zoomBitmap(256, 256, _picturePath);
                System.out.println(_bitmap.getWidth());
                System.out.println(_bitmap.getHeight());

//                System.out.println(_bitmap.getPixel(0,0));
//                FloatBuffer mInputTensorBuffer = Tensor.allocateFloatBuffer(3*256*256);
//                Tensor inputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1, 3, 256, 256});

                module = PyTorchAndroid.loadModuleFromAsset(getAssets(), "test.pt");
                System.out.println("start converting");
                Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(_bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
                final IValue[] outputTuple = module.forward(IValue.from(inputTensor)).toTuple();
                System.out.println("finish converting");
//                System.out.println(outputTuple );
//                System.out.println(outputTuple.length);
//
//                System.out.println(outputTuple[0]);
//
//                System.out.println(outputTuple[1]);

                final Tensor outputTensor = outputTuple[0].toTensor();
                System.out.println(Arrays.toString(outputTensor.shape()));
                float[] imgArray = outputTensor.getDataAsFloatArray();
                Bitmap resultBitmap = bitmapFromRGBImageAsFloatArray(imgArray, 256, 256);

                resultBitmap = adjustPhotoRotation(resultBitmap, 90);

                if (resultBitmap != null) {
                    imageView.setImageBitmap(resultBitmap);
                }
            }
        });

    }


    public static Bitmap bitmapFromRGBImageAsFloatArray(float[] data, int width, int height) {

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < width * height; i++) {


            int r = (int) ((data[i] * 0.5 + 0.5) * 255.0f);
            int g = (int) ((data[i + width * height] * 0.5 + 0.5) * 255.0f);
            int b = (int) ((data[i + width * height * 2] * 0.5 + 0.5) * 255.0f);

            int x = i / width;
            int y = i % width;

            int color = Color.rgb(r, g, b);
            bmp.setPixel(x, y, color);

        }
        return bmp;
    }

    private Bitmap zoomBitmap(int width, int height, String imgPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.;
//        options.inPurgeable=true;
//        options.inInputShareable=true;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
//        float scaleWidth = (float) 1.0 / width;
//        float scaleHeight = (float) 1.0 / height;
        float scaleWidth = (float) width / bitmap.getWidth();
        float scaleHeight = (float) height / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return scaleBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            _picturePath = cursor.getString(columnIndex);
            cursor.close();
//            System.out.println(_picturePath);
            imageView.setImageBitmap(BitmapFactory.decodeFile(_picturePath));

        }

    }

    private Bitmap adjustPhotoRotation(Bitmap bm, int degree) {
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        m.postScale(-1,1);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

            return bm1;

        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

    private void initPermissions() {
        permissionsList.clear();

        //判断哪些权限未授予
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }

        //请求权限
        if (!permissionsList.isEmpty()) {
            String[] permissions = permissionsList.toArray(new String[permissionsList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                break;
        }
    }


}