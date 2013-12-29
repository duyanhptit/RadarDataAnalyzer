package vn.ptit.addn.rda;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class ProcessingData {

	private String mPathData;
	private TreeMap<Integer, int[][]> mImg = new TreeMap<Integer, int[][]>();
	private int[][] mRadarImg = new int[1366][3000]; // 16383 / 12 = 1365
	private int[] mPulseData = new int[6 * 510]; // actual is 3000
	private int[] mPreFrame = new int[3 + 510];

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
					mRadarImg = new int[1500][3000];
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
			addToRadaImg(mPulseData, position);
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

	private void addToRadaImg(int[] pulseData, int position) {
		for (int i = 0; i < 3000; i++) {
			mRadarImg[position][i] = pulseData[i];
		}
		System.out.print("Vị trí mảng: " + position + " - ");
		if (675 < position && position < 690 && !mOverHalf) {
			mOverHalf = true;
		}
	}

	private void saveRadarImg(int[][] mRadarImg) {
		int[][] radaImg = convetGrayScale(mRadarImg);
		File fileOut = new File("data/rawImage/img" + String.format("%03d", mCount) + ".jpg");
		BufferedImage buffImage = new BufferedImage(3000, 1366, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				buffImage.setRGB(j, i, radaImg[i][j]);
			}
		}
		mCount++;
		try {
			// buffImage.setData(writerableRaster);
			boolean status = ImageIO.write(buffImage, "jpg", fileOut);
			System.out.println("Write file " + fileOut.getName() + " : " + status);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[][] convetGrayScale(int[][] mRadarImg) {
		int[][] result = new int[1366][3000];
		for (int i = 0; i < 1366; i++) {
			for (int j = 0; j < 3000; j++) {
				int value = mRadarImg[i][j] >> 4; // Chuyển 12 bit (4080) về 8 bit (256)
				// value = 255 - value; // Đảo ngược màu
				value = value << 16 | value << 8 | value; // Chuyển tương thích với màu RGB
				result[i][j] = value;
			}
		}
		return result;
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

}
