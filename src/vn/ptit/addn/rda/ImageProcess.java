package vn.ptit.addn.rda;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageProcess {

	private int mCount = 1;
	private static final int NUM_OF_IMG = 35;

	private static final int THRESHOLD_SUBTRACTION = 250;

	private static final int THRESHOLD_STATIC_OBJECT = 100;

	private static final int THRESHOLD_SPLIT = 55;

	private static final double ALPHA = 0.3;

	private static final double[] valueObject = { 255.0, 255.0, 255.0 };
	private static final double[] valueBackground = { 0.0, 0.0, 0.0 };
	private List<Mat> mats = new LinkedList<>();

	private Mat mBackground = null;

	public ImageProcess() {
	}

	public void process() {
		mCount = 1;
		while (mCount <= NUM_OF_IMG) {
			Mat img = readProcImage(mCount);
			if (mBackground == null) {
				mBackground = img;
			} else {
				Mat bsImg = backgroundSubtraction(img);
				Mat circleImg = convertCircleImage(bsImg);
				writeBSImage(circleImg, mCount);
				updateBackgound(img);
				writeBackground(mBackground, mCount);
			}
			mCount++;
		}
	}

	private void updateBackgound(Mat img) {
		int im, bg, temp;
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				im = getValue(img.get(i, j));
				bg = getValue(mBackground.get(i, j));
				if (im >= bg) {
					temp = (int) ((1 - ALPHA) * im + ALPHA * bg);
				} else {
					temp = (int) (ALPHA * im + (1 - ALPHA) * bg);
				}
				mBackground.put(i, j, setValue(temp));
			}
		}
	}

	private Mat detectObject(Mat circleImg) {
		for (int i = 0; i < circleImg.rows(); i += 100) {
			for (int j = 0; j < circleImg.cols(); j += 100) {
				if (isInsideCircle(i, j)) {
					if (getPointsObject(circleImg, i, j) > 200) {
						for (int k = 0; k < 100; k++) {
							circleImg.put(i, j + k, 255);
						}
						for (int k = 0; k < 100; k++) {
							circleImg.put(i + 100, j + k, 255);
						}
						for (int k = 0; k < 100; k++) {
							circleImg.put(i + k, j, 255);
						}
						for (int k = 0; k < 100; k++) {
							circleImg.put(i + k, j + 100, 255);
						}
					}
				}
			}
		}
		return circleImg;
	}

	private int getPointsObject(Mat circleImg, int i, int j) {
		int count = 0;
		for (int n = 0; n < 100; n++) {
			for (int m = 0; m < 100; m++) {
				if (getValue(circleImg.get(i + n, j + m)) == 255) {
					count++;
				}
			}
		}
		return count;
	}

	public void filterProcess() {
		mCount = 1;
		while (mCount <= NUM_OF_IMG) {
			Mat img = readSquareImage(mCount);
			Mat outFilter = new Mat();
			Imgproc.blur(img, outFilter, new Size(5, 5));

			Mat outSplit = new Mat();
			Imgproc.threshold(outFilter, outSplit, THRESHOLD_SPLIT, 255, Imgproc.THRESH_BINARY);

			writeProcImage(outSplit, mCount);
			mCount++;
		}
	}

	private Mat convertCircleImage(Mat square) {
		Mat circle = new Mat(3000, 3000, CvType.CV_8UC1);
		double[] outSideCircle = { 255, 255, 255 };

		for (int x = 0; x < 3000; x++) {
			for (int y = 0; y < 3000; y++) {
				if (Math.sqrt(Math.pow(x - 1500, 2) + Math.pow(y - 1500, 2)) > 1500) {
					circle.put(x, y, outSideCircle);
				}
			}
		}
		double alpha;
		int x, y;
		for (int i = 0; i < 1366; i++) {
			alpha = 2 * Math.PI * (-i) / 1365;
			for (int j = 0; j < 1500; j++) {
				x = (int) Math.round(1500 + j * Math.sin(alpha));
				y = (int) Math.round(1500 + j * Math.cos(alpha));
				circle.put(x, y, square.get(i, j));
			}
		}
		return circle;
	}

	private Mat backgroundSubtraction(Mat img) {
		Mat result = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				int valueImg = getValue(img.get(i, j));
				int valueBG = getValue(mBackground.get(i, j));

				int distance = (valueImg - valueBG) > 0 ? valueImg - valueBG : valueBG - valueImg;
				if (distance >= THRESHOLD_SUBTRACTION) {
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
		while (mCount <= NUM_OF_IMG) {
			Mat img = readProcImage(mCount);
			mats.add(img);
			mCount++;
		}
		Mat medianMat = mats.get(0).clone();
		System.out.println("Processing computing median background...");
		for (int i = 0; i < 1500; i++) {
			for (int j = 0; j < 1500; j++) {
				if (isInsideCircle(i, j)) {
					int total = 0;
					for (Mat mat : mats) {
						total += getValue(mat.get(i, j));
					}
					int median = total / mats.size();
					medianMat.put(i, j, setValue(median));
				}
			}
		}
		Highgui.imwrite("data/medianBackground.jpg", medianMat);
		System.out.println("Completed computing median background.");
	}

	public void staticObjectProcess() {
		Mat medianBG = Highgui.imread("data/medianBackground.jpg");
		System.out.println("Dectecting static object...");
		Mat staticObjectImg = medianBG.clone();
		for (int i = 0; i < 1500; i++) {
			for (int j = 0; j < 1500; j++) {
				if (isInsideCircle(i, j)) {
					if (getValue(medianBG.get(i, j)) > THRESHOLD_STATIC_OBJECT) {
						staticObjectImg.put(i, j, valueObject);
					} else {
						staticObjectImg.put(i, j, valueBackground);
					}
				}
			}
		}
		Highgui.imwrite("data/staticObject.jpg", staticObjectImg);
		System.out.println("Completed detect static object.");
	}

	private Mat readSquareImage(int num) {
		String pathImg = "data/squareImage/img" + String.format("%03d", num) + ".jpg";
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

	private void writeBackground(Mat mat, int num) {
		String pathImg = "data/background/img" + String.format("%03d", num) + ".jpg";
		Highgui.imwrite(pathImg, mat);
		System.out.println("Completed write: " + pathImg);
	}

	private int getValue(double[] doubleValue) {
		return (int) doubleValue[0];
	}

	private double[] setValue(int intValue) {
		double[] doubleValue = new double[3];
		doubleValue[0] = intValue;
		doubleValue[1] = intValue;
		doubleValue[2] = intValue;
		return doubleValue;

	}

	private boolean isInsideCircle(int x, int y) {
		if (Math.sqrt(Math.pow(x - 1500, 2) + Math.pow(y - 1500, 2)) > 1500) {
			return false;
		}
		return true;
	}

	private Mat subtractionTwoImg(Mat img, Mat nextImg) {
		Mat result = img.clone();
		for (int i = 0; i < 1500; i++) {
			for (int j = 0; j < 1500; j++) {
				if (isInsideCircle(i, j)) {
					double[] valueImg = img.get(i, j);
					double[] valueNextImg = nextImg.get(i, j);
					double distance = (valueImg[0] - valueNextImg[0]) >= 0 ? valueImg[0] - valueNextImg[0]
							: valueNextImg[0] - valueImg[0];
					if (distance >= THRESHOLD_SUBTRACTION) {
						result.put(i, j, valueObject);
					} else {
						result.put(i, j, valueBackground);
					}
				}
			}
		}
		return result;
	}
}
