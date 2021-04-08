package com.example.flutter_access_android;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;

import com.example.flutter_access_android.utils.ImageUtil;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;

import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;


public class MainActivity extends FlutterActivity {
    private static final String channel = "face.convert";
    private Module module;
    private Bitmap _bitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("balabaka");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), channel).setMethodCallHandler(
                (call, result) -> {
                    System.out.println("====================");
                    System.out.println(call.arguments);
                    System.out.println("====================");
                    if (call.method != null) {
                        result.success(convert((String) call.arguments));
                    } else {
                        result.notImplemented();
                    }
                }
        );
    }


    private String convert(String imgpath) {
        String filepath = this.getFilePath(imgpath);

        module = PyTorchAndroid.loadModuleFromAsset(getAssets(), "test.pt");
        System.out.println("start converting");
        _bitmap = ImageUtil.zoomBitmap(256, 256, imgpath);
        System.out.println("start converting");
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(_bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final IValue[] outputTuple = module.forward(IValue.from(inputTensor)).toTuple();
        System.out.println("finish converting");
        final Tensor outputTensor = outputTuple[0].toTensor();
        float[] imgArray = outputTensor.getDataAsFloatArray();
        Bitmap resultBitmap = ImageUtil.bitmapFromRGBImageAsFloatArray(imgArray, 256, 256);
        resultBitmap = ImageUtil.adjustPhotoRotation(resultBitmap, 90);

        ImageUtil.saveBitmap(resultBitmap, filepath + "/demo.png");
        return filepath + "/demo.png";
    }

    private static String getFilePath(String filename) {
        String filepath = "";
        String[] tmp = filename.trim().split("/");
        for (int i = 1; i < tmp.length - 1; i++) {
            filepath += "/" + tmp[i];
        }
        System.out.println("filepath:" + filepath);
        return tmp[0] + filepath;
    }

}
