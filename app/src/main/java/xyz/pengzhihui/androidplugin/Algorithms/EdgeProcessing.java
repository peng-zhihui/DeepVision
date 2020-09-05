package xyz.pengzhihui.androidplugin.Algorithms;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class EdgeProcessing
{
    public Mat doProcessing(Mat frame)
    {
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(frame, frame, new Size(5, 5));
        Imgproc.threshold(frame, frame, 120, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(frame, frame, 30, 100);

        return frame;
    }
}
