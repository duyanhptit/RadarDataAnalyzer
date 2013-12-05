package vn.ptit.addn.rda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ReadData {

	private static final String PATH = "E:/Rada data/data";
	private List<String> mPathFiles;
	private int count = 0;

	public ReadData() {
		mPathFiles = getAllFileName();
	}

	// get all file into E:\Rada data\data and build list file path
	private List<String> getAllFileName() {
		List<String> pathFiles = new ArrayList<String>();
		File folder = new File(PATH);
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				pathFiles.add(PATH + "/" + fileEntry.getName());
			}
		}
		return pathFiles;
	}

	public void readAllRawData() {
		for (String pathFile : mPathFiles) {
			System.out.println("readding file " + pathFile);
			try {
				InputStream in = new FileInputStream(pathFile);
				byte[] buffer = new byte[1024];
				while (in.read(buffer) > 1) {
					int b = buffer[0] & 0xFF; // Note: b & 0xFF to Integer
					if (b != 0xFF) {
						processFrame(buffer);
						// String s1 = String.format("%8s",
						// Integer.toBinaryString(b)).replace(" ", "0");
						// System.out.println(s1);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processFrame(byte[] buffer) {
		// Xử lý frame có dữ liệu
	}

	public void showPathFiles() {
		for (String pathFile : mPathFiles) {
			System.out.println(pathFile);
		}
	}

	// byte b1 = buffer[0];
	// String s1 = String.format("%8s", Integer.toBinaryString(b1 &
	// 0xFF)).replace(" ", "0");
	// System.out.println(s1);
}
