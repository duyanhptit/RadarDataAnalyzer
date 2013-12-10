package vn.ptit.addn.rda;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessingData {

	private String mPathData;
	private int[][] mRadarImg = new int[0x3FFF + 1][3000];
	private int mIndexRadarImg = 0;
	private int[] mTempPulseData = new int[3000];
	private int[] mTempPreFrame = new int[3 + 510];

	public ProcessingData(String pathData) {
		mPathData = pathData;
	}

	public void readData() {
		try {
			InputStream in = new FileInputStream(mPathData);
			byte[] frame = new byte[1024];
			while (in.read(frame) > 1) {
				processingFrame(frame);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processingFrame(byte[] frame) {
		int[] frameInt = getFrameValue(frame);
		if (frameInt[1] != mTempPreFrame[1]) { // compare PID
			addToRadaImg(mTempPulseData);
			mTempPulseData = new int[3000];
		}
		// push frame to pulse data
		int FID = frameInt[0];
		int PulseCode = frameInt[2];
		for (int i = 3; i < frameInt.length; i++) {
			mTempPulseData[FID * 510 + (i - 3)] = frameInt[i];
		}
	}

	private void addToRadaImg(int[] pulseData) {

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

	private int getInt(byte b) {
		return b & 0xFF;
	}

	// 0x1A2B = 2B(LByte) -> 1A (UByte)
	private int getIntOfLitterEndian(byte L_Byte, byte U_Byte) {
		return (U_Byte & 0xFF) << 8 | (L_Byte & 0xFF);
	}

}
