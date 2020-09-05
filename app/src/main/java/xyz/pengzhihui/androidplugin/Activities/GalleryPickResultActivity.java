package xyz.pengzhihui.androidplugin.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.security.PublicKey;
import java.util.List;

import xyz.pengzhihui.androidplugin.Envs.GalleryPickResultActivityBase;
import xyz.pengzhihui.lib_neural_engine.SnpeEngine;

public class GalleryPickResultActivity extends GalleryPickResultActivityBase
{
    private SnpeEngine stylizer_model;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        switch (GalleryPickActivity.getCurrentSceneIndex())
        {
            case 0:
                break;

            case 1:
                stylizer_model = new SnpeEngine("sdcard/InfinityCam/stylizer_model/quantize_reno.dlc");
                stylizer_model.init(SnpeEngine.Runtime.RUNTIME_DSP);
                // in case if you have multiple output-nodes ↓
                // model.addOutputNode(node_name1)
                //      .addOutputNode(node_name2)
                //      .init(SnpeEngine.Runtime.RUNTIME_GPU);

                break;

            case 2:

                break;

            //...
        }
    }


    @Override
    protected void onImageSelected(String _imagePath)
    {
        switch (GalleryPickActivity.getCurrentSceneIndex())
        {
            case 0:
                break;

            case 1:
                break;

            //...
        }
    }


    @Override
    protected void onSliderTriggered(String _imagePath)
    {
        switch (GalleryPickActivity.getCurrentSceneIndex())
        {
            case 0:
                // Read image from path
                Mat rgb = Imgcodecs.imread(_imagePath);

                // Process the image
                Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2GRAY);

                // Convert mat image to bitmap
                Bitmap bitmap = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(rgb, bitmap);

                // Update the canvas and set the slider progress
                updateCanvas(bitmap, _imagePath);
                notifyProcessingProgress(1);

                break;

            case 1:
                Mat image = SnpeEngine.loadMat(_imagePath);
                // or you can load Bitmap as well ↓
                // Bitmap image = SnpeEngine.loadBitmap(_imagePath);

                Imgproc.resize(image, image, new Size(240, 320));

                stylizer_model.feed(image, 1 / 255.0f, 0);
                // 1.you can feed either Mat or Bitmap
                // 2.you can add normalize params like this ↓
                //      model.feed(image); //default alpha=1, beta=0
                // the image will be applied [image*alpha + beta] before excute.

                int time = stylizer_model.execute();
                // return inference time in ms.

                List<SnpeEngine.TensorBuffer> outputs = stylizer_model.getOutputTensors();

                Mat out = outputs.get(0).getMat(320, 240, 3, 255, 0);
                // 1.you can use either getMat()/getBitmap()/getFloatArray(), no copy is occured.
                // 2.you can add normalize params like this ↓
                //      tensor.getMat(640, 480, 3); //default alpha=1, beta=0
                // the image will be applied [image*alpha + beta].

                SnpeEngine.saveMat(out, "sdcard/sketch.jpg");
                // there is also SnpeEngine.saveBitmap();


                mDropDownMessage.setMessage("推理时间: " + time + "ms").show(1000);
                updateCanvas(outputs.get(0).getBitmap(320, 240, 3, 255, 0)
                        , _imagePath);
                notifyProcessingProgress(1);

                break;

            case 2:

                break;

            //...
        }
    }
}
