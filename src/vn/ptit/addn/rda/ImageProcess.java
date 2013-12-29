package vn.ptit.addn.rda;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageProcess {

	private int mCount = 1;
	private static final int THRESHOLD = 246;
	private static final int THRESHOLD_STATIC_OBJECT = 100;
	private static final double[] valueObject = { 0.0, 0.0, 0.0 };
	private static final double[] valueBackground = { 255.0, 255.0, 255.0 };
	private List<Mat> mats = new LinkedList<>();

	public ImageProcess() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public void process() {
		mCount = 1;
		while (mCount < 35) {
			Mat img = readProcImage(mCount);
			Mat background = Highgui.imread("data/medianBackground.jpg");

			Mat bsImg = backgroundSubtraction1(img, background);
			writeBSImage(bsImg, mCount);
			mCount++;
		}
	}

	public void cannyProcess() {
		mCount = 1;
		while (mCount <= 35) {
			Mat img = readRawImage(mCount);
			Mat outFilter = medianFilter(img);

			Mat outCanny = new Mat();
			Imgproc.Canny(outFilter, outCanny, 75, 150);
			writeProcImage(outCanny, mCount);

			mCount++;
		}
	}

	private Mat medianFilter(Mat img) {
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
				if (distance >= THRESHOLD) {
					result.put(i, j, valueObject);
				} else {
					result.put(i, j, valueBackground);
				}
			}
		}
		return result;
	}

	private Mat backgroundSubtraction1(Mat img, Mat background) {
		Mat result = img.clone();
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				int valueImg = getValue(img.get(i, j));
				int valueBG = getValue(background.get(i, j));

				if ((valueImg - valueBG) > THRESHOLD) {
					result.put(i, j, valueObject);
				} else {
					result.put(i, j, valueBackground);
				}
			}
		}
		return result;
	}

	public void medianBackground() {
		mCount = 1;
		while (mCount <= 35) {
			Mat img = readProcImage(mCount);
			mats.add(img);
			mCount++;
		}
		Mat medianMat = mats.get(0).clone();
		System.out.println("Processing computing median background...");
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				int total = 0;
				for (Mat mat : mats) {
					total += getValue(mat.get(i, j));
				}
				int median = total / mats.size();
				medianMat.put(i, j, setValue(median));
			}
		}
		Highgui.imwrite("data/medianBackground.jpg", medianMat);
		System.out.println("Completed computing median background.");
	}

	public void staticObjectProcess() {
		Mat medianBG = Highgui.imread("data/medianBackground.jpg");
		System.out.println("Dectecting static object...");
		Mat staticObjectImg = medianBG.clone();
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				if (getValue(medianBG.get(i, j)) > THRESHOLD_STATIC_OBJECT) {
					staticObjectImg.put(i, j, valueObject);
				} else {
					staticObjectImg.put(i, j, valueBackground);
				}
			}
		}
		Highgui.imwrite("data/staticObject.jpg", staticObjectImg);
		System.out.println("Completed detect static object.");
	}

	private Mat readRawImage(int num) {
		String pathImg = "data/rawImage/img" + String.format("%03d", num) + ".jpg";
		System.out.println("Completed read: " + pathImg);
		return Highgui.imread(pathImg);
	}

	private Mat readProcImage(int num) {
		String pathImg = "data/procImage/img" + String.format("%03d", num) + ".jpg";
		System.out.println("Completed read: " + pathImg);
		return Highgui.imread(pathImg);
	}

	private void writeProcImage(Mat mat, int num) {
		String pathImg = "data/procImage/img" + String.format("%03d", num) + ".jpg";
		Highgui.imwrite(pathImg, mat);
		System.out.println("Completed write: " + pathImg);
	}

	private void writeBSImage(Mat mat, int num) {
		String pathImg = "data/bsImage/img" + String.format("%03d", num) + ".jpg";
		Highgui.imwrite(pathImg, mat);
		System.out.println("Completed write: " + pathImg);
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
