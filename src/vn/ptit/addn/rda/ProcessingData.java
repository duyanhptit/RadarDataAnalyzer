package vn.ptit.addn.rda;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ProcessingData {

	private String mPathData;
	private TreeMap<Integer, int[][]> mImg = new TreeMap<Integer, int[][]>();
	private int[][] mRadarImg = new int[1366][3000]; // 16383 / 12 = 1365
	private int[] mPulseData = new int[6 * 510]; // actual is 3000
	private int[] mPreFrame = new int[3 + 510];
	private int[][] mQueue = new int[5][3000];

	private int[][] mMatrixGauss = { { 2, 4, 5, 4, 2 },//
			{ 4, 9, 12, 9, 4 },//
			{ 5, 12, 15, 12, 5 },//
			{ 2, 4, 5, 4, 2 },//
			{ 4, 9, 12, 9, 4 } };
	private static final double THRESHOLD = 5;

	private boolean mOverHalf = false;
	private int mCount = 1;

	public ProcessingData(String pathData) {
		mPathData = pathData;
	}

	public void readData() {
		try {
			InputStream in = new FileInputStream(mPathData);
			byte[] frame = new byte[1024];
			int firstPulseID = 0;
			while (in.read(frame) > 1) {
				int[] frameInt = processingFrame(frame);
				// Check condition create new radar image
				int PulseID = frameInt[2] / 12;
				if (PulseID == firstPulseID && mOverHalf) { // condition create new radar image
					// Save Rada Image and create New Array
					saveRadarImg(mRadarImg);
					// mRadarImg = new int[1500][3000];
					mOverHalf = false;
					// in ảnh và reset mRadarImg
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] processingFrame(byte[] frame) {
		int[] frameInt = getFrameValue(frame);
		int FID = frameInt[0]; // Mã frame 0-5
		int PID = frameInt[1]; // Dấu hiệu đổi xung
		int PulseCode = frameInt[2]; // Mã góc

		if (frameInt[1] != mPreFrame[1]) { // so sánh PID: nếu khác xung
			int position = PulseCode / 12;
			addPulseToRadaImg(mPulseData, position);
			mPulseData = new int[6 * 510];
			System.out.println("Mã góc: " + PulseCode);
		}

		mPreFrame = frameInt;
		// push frame to pulse data
		for (int i = 3; i < frameInt.length; i++) {
			mPulseData[FID * 510 + (i - 3)] = frameInt[i];
		}
		return frameInt;
	}

	private void addPulseToRadaImg(int[] pulseData, int position) {
		mRadarImg[position] = filterData(pulseData);

		System.out.print("Vị trí mảng: " + position + " - ");
		if (675 < position && position < 690 && !mOverHalf) {
			mOverHalf = true;
		}
	}

	private int[] filterData(int[] pulseData) {
		for (int i = 0; i < 5; i++) {
			if (i == 4) {
				mQueue[i] = pulseData;
			} else {
				mQueue[i] = mQueue[i + 1];
			}
		}
		int[] temp = pulseData;
		for (int k = 0; k < (3000 - 5); k++) {
			double avg = 0;
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					avg += mQueue[j][k + i] / 25;
				}
			}
			double distance = (mQueue[2][k + 2] - avg) > 0 ? (mQueue[2][k + 2] - avg) : (avg - mQueue[2][k + 2]);
			if (distance > THRESHOLD) {
				temp[k + 2] = (int) Math.round(avg);
			}
		}
		return temp;
	}

	private void saveRadarImg(int[][] mRadarImg) {
		// saveCircleRadarImg(mRadarImg);
		saveSquareRadarImg(mRadarImg);
		mCount++;
	}

	private void saveCircleRadarImg(int[][] mRadarImg) {
		Mat radarMat = convertCircleImage(mRadarImg);
		radarMat = downSizeImage(radarMat);

		String pathImg = "data/circleImage/img" + String.format("%03d", mCount) + ".jpg";
		Highgui.imwrite(pathImg, radarMat);
		System.out.println("Completed write: " + pathImg);

		// File fileOut = new File("data/circleImage/img" + String.format("%03d", mCount) + ".jpg");
		// BufferedImage buffImage = new BufferedImage(6000, 6000, BufferedImage.TYPE_INT_RGB);
		// for (int i = 0; i < 6000; i++) {
		// for (int j = 0; j < 6000; j++) {
		// buffImage.setRGB(j, i, radaImg[i][j]);
		// }
		// }
		// try {
		// boolean status = ImageIO.write(buffImage, "jpg", fileOut);
		// System.out.println("Write file " + fileOut.getName() + " : " + status);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	// Giảm kích thước ảnh bằng 1 nửa. ĐK |dsize * 2 - ssize| <= 2
	private Mat downSizeImage(Mat mat) {
		Mat smallMat = new Mat();
		Imgproc.pyrDown(mat, smallMat, new Size(mat.cols() / 2, mat.rows() / 2));
		return smallMat;
	}

	private Mat convertCircleImage(int[][] mRadarImg) {
		Mat mat = new Mat(3000, 3000, CvType.CV_8UC1);
		for (int x = 0; x < 3000; x++) {
			for (int y = 0; y < 3000; y++) {
				if (Math.sqrt(Math.pow(x - 1500, 2) + Math.pow(y - 1500, 2)) > 1500) {
					mat.put(x, y, setValue(255));
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
				mat.put(x, y, setValue(mRadarImg[i][j] >> 4));
			}
		}
		return mat;
	}

	private void saveSquareRadarImg(int[][] mRadarImg) {
		File fileOut = new File("data/squareImage/img" + String.format("%03d", mCount) + ".jpg");
		BufferedImage buffImage = new BufferedImage(3000, 1366, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				buffImage.setRGB(j, i, convertToRGBValue(mRadarImg[i][j]));
			}
		}
		try {
			boolean status = ImageIO.write(buffImage, "jpg", fileOut);
			System.out.println("Write file " + fileOut.getName() + " : " + status);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int convertToRGBValue(int value) {
		int RGBValue = value >> 4; // Chuyển 12 bit (4095) về 8 bit (255)
		// result = 255 - result; // Đảo ngược màu
		RGBValue = RGBValue << 16 | RGBValue << 8 | RGBValue; // Chuyển giá trị Gray Scale với kiểu RGB
		return RGBValue;
	}

	/*
	 * Functions get data from Frame
	 */
	private int[] getFrameValue(byte[] frame) {
		int[] frameInt = new int[3 + 510];
		frameInt[0] = getInt(frame[0]); // Frame ID
		frameInt[1] = getInt(frame[1]); // Pulse ID
		frameInt[2] = getIntOfLitterEndian(frame[2], frame[3]); // Pulse Code
		int index = 3;
		for (int i = 4; i < 1024; i = i + 2) {
			frameInt[index] = getIntOfLitterEndian(frame[i], frame[i + 1]);
			index++;
		}
		return frameInt;
	}

	private int getInt(byte b) {
		return b & 0xFF;
	}

	// 0x1A2B = 2B(LByte) -> 1A (UByte)
	private int getIntOfLitterEndian(byte L_Byte, byte U_Byte) {
		return (U_Byte & 0xFF) << 8 | (L_Byte & 0xFF);
	}

	private double[] setValue(int intValue) {
		double[] doubleValue = new double[3];
		doubleValue[0] = intValue;
		doubleValue[1] = intValue;
		doubleValue[2] = intValue;
		return doubleValue;

	}

}
