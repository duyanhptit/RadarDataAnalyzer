package vn.ptit.addn.rda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FilterData {

	private List<String> mPathFiles;
	private OutputStream mOutput = null;

	public FilterData(String pathInput) {
		mPathFiles = getAllFileName(pathInput);
		createOutput();
	}

	// get all file into E:\Radar data\data and build list file input path
	private List<String> getAllFileName(String pathInput) {
		List<String> pathFiles = new ArrayList<String>();
		File folder = new File(pathInput);
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				pathFiles.add(pathInput + "/" + fileEntry.getName());
			}
		}
		return pathFiles;
	}

	private void createOutput() {
		try {
			File file = new File(main.PATH_DATA);
			file.getParentFile().mkdirs();
			mOutput = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void filterRawData() {
		try {
			for (String pathFile : mPathFiles) {
				System.out.println("readding file " + pathFile);
				InputStream in = new FileInputStream(pathFile);
				byte[] buffer = new byte[1024];
				while (in.read(buffer) > 1) {
					int b = buffer[0] & 0xFF; // Note: b & 0xFF to Integer
					if (b != 0xFF) {
						processFrame(buffer);
					}
				}
			}
			mOutput.flush();
			mOutput.close();
			System.out.println("Writed data to " + main.PATH_DATA);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processFrame(byte[] buffer) {
		try {
			mOutput.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
