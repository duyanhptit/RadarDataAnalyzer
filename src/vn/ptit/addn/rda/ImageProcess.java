package vn.ptit.addn.rda;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageProcess {

	private int mCount = 1;
	private static final int THRESHOLD = 200;
	private static final double[] valueObject = { 0.0, 0.0, 0.0 };
	private static final double[] valueBackground = { 255.0, 255.0, 255.0 };

	public ImageProcess() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public void process() {
		mCount = 1;
		while (mCount < 35) {
			Mat img = readProcImage(mCount);
			Mat nextImg = readProcImage(mCount + 1);
			Mat bgImg = backgroundSubtraction(img, nextImg);
			writeBSImage(bgImg, mCount);
			mCount++;
		}
	}

	public void cannyProcess() {
		mCount = 1;
		while (mCount <= 35) {
			Mat img = readRawImage(mCount);
			Mat outFilter = medianFilter(img);
			// writeBSImage(out, mCount);

			// Mat nextImg = readRawImage(mCount + 1);
			// Mat result = backgroundSubtraction(img, nextImg);
			// writeBSImage(result, mCount);

			Mat outCanny = new Mat();
			// BackgroundSubtractorMOG bs = new BackgroundSubtractorMOG();
			Imgproc.Canny(outFilter, outCanny, 75, 150);
			writeProcImage(outCanny, mCount);

			mCount++;
		}
	}

	public Mat medianFilter(Mat img) {
		Mat out = new Mat();
		Imgproc.GaussianBlur(img, out, new Size(7.0, 7.0), 2.0);
		return out;
	}

	private Mat backgroundSubtraction(Mat img, Mat nextImg) {
		Mat result = img.clone();
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				double[] valueImg = img.get(i, j);
				double[] valueNextImg = nextImg.get(i, j);
				double distance = (valueImg[0] - valueNextImg[0]) >= 0 ? valueImg[0] - valueNextImg[0]
						: valueNextImg[0] - valueImg[0];
				if (distance > THRESHOLD) {
					result.put(i, j, valueObject);
				} else {
					result.put(i, j, valueBackground);
				}
			}
		}
		return result;
	}

	private Mat readRawImage(int num) {
		String pathImg = "data/rawImage/img" + String.format("%03d", num) + ".jpg";
		return Highgui.imread(pathImg);
	}

	private Mat readProcImage(int num) {
		String pathImg = "data/procImage/img" + String.format("%03d", num) + ".jpg";
		return Highgui.imread(pathImg);
	}

	private void writeProcImage(Mat mat, int num) {
		String pathImg = "data/procImage/img" + String.format("%03d", num) + ".jpg";
		Highgui.imwrite(pathImg, mat);
		System.out.println("Complete write: " + pathImg);
	}

	private void writeBSImage(Mat mat, int num) {
		String pathImg = "data/bsImage/img" + String.format("%03d", num) + ".jpg";
		Highgui.imwrite(pathImg, mat);
		System.out.println("Complete write: " + pathImg);
	}

	private int getValue(double[] doubleValue) {
		return (int) doubleValue[0];
	}

	private double[] setValue(int intValue) {
		double[] doubleValue = valueObject;
		doubleValue[0] = intValue;
		doubleValue[1] = intValue;
		doubleValue[2] = intValue;
		return doubleValue;

	}
}
