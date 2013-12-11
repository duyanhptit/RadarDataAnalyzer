package vn.ptit.addn.rda;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class ProcessingData {

	private String mPathData;
	private TreeMap<Integer, int[][]> mImg = new TreeMap<Integer, int[][]>();
	private int[][] mRadarImg = new int[20000][3000];
	private int[] mTempPulseData = new int[6 * 510]; // actual is 3000
	private int[] mTempPreFrame = new int[3 + 510];

	private int mIndexRadarImg = 0;

	public ProcessingData(String pathData) {
		mPathData = pathData;
	}

	public void readData() {
		try {
			InputStream in = new FileInputStream(mPathData);
			byte[] frame = new byte[1024];
			int firstPulseCode = -1;
			while (in.read(frame) > 1) {
				int[] frameInt = processingFrame(frame);
				// Check condition create new radar image
				if (firstPulseCode == -1) {
					firstPulseCode = frameInt[2];
				} else if (firstPulseCode - 12 <= frameInt[2] //
						&& frameInt[2] <= firstPulseCode //
						&& mIndexRadarImg > 3000) { // condition create new radar image
					System.out.println(mIndexRadarImg);
					saveRadarImg(mRadarImg);
					mRadarImg = new int[20000][3000];
					mIndexRadarImg = 0;
					// in ảnh và reset mRadarImg
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] processingFrame(byte[] frame) {
		int[] frameInt = getFrameValue(frame);
		if (frameInt[1] != mTempPreFrame[1]) { // compare PID
			addToRadaImg(mTempPulseData);
			mTempPulseData = new int[6 * 510];
			System.out.println(frameInt[2]);
		}
		mTempPreFrame = frameInt;
		// push frame to pulse data
		int FID = frameInt[0];
		for (int i = 3; i < frameInt.length; i++) {
			mTempPulseData[FID * 510 + (i - 3)] = frameInt[i];
		}
		return frameInt;
	}

	private void addToRadaImg(int[] pulseData) {
		for (int i = 0; i < 3000; i++) {
			mRadarImg[mIndexRadarImg][i] = pulseData[i];
		}
		System.out.print(mIndexRadarImg + " : ");
		mIndexRadarImg++;
	}

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

	private void saveRadarImg(int[][] mRadarImg) {
		// TODO Auto-generated method stub

	}

	private int getInt(byte b) {
		return b & 0xFF;
	}

	// 0x1A2B = 2B(LByte) -> 1A (UByte)
	private int getIntOfLitterEndian(byte L_Byte, byte U_Byte) {
		return (U_Byte & 0xFF) << 8 | (L_Byte & 0xFF);
	}

}
