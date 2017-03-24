import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

class ImageByte {
	BufferedImage img;
	byte[] bytes ;
	int width = 352 ;
	int height = 288;

	public void getBytes(String args){
		img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		File file = new File(args);
		InputStream is;
		try {
			is = new FileInputStream(file);
			long fileSize = file.length();
			bytes = new byte[(int)fileSize];

			int offset = 0;
			int numRead = 0;

			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayImage(BufferedImage[] image) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		frame.setTitle("Image Compression using Vector Quantization");
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		ImageIcon originalIcon = new ImageIcon(image[0]);
		ImageIcon compressIcon = new ImageIcon(image[1]);
		JLabel originalLabel = new JLabel(originalIcon); 
		JLabel compressLabel = new JLabel(compressIcon); 

		JPanel originalPanel = new JPanel();
		TitledBorder originalBorder = getPanelBorder("Original Image");
		originalPanel.setBorder(originalBorder);
		originalPanel.add(originalLabel);

		JPanel compressedPanel = new JPanel();
		TitledBorder compressedBorder = getPanelBorder("Compressed Image");
		compressedPanel.setBorder(compressedBorder);
		compressedPanel.add(compressLabel);

		frame.getContentPane().add(originalPanel);
		frame.getContentPane().add(compressedPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private static TitledBorder getPanelBorder(String caption) {
		// TODO Auto-generated method stub
		TitledBorder border;
		border  = new TitledBorder(caption);
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitlePosition(TitledBorder.TOP);
		return border;
	}

	public BufferedImage[] getBufferedImage(String fileType, byte[] originalImage, byte[] compressImage) {
		// TODO Auto-generated method stub
		BufferedImage[] imageArray = new BufferedImage[2] ;
		if(fileType.equals("raw")){
			imageArray[0] = displayGray(originalImage);
			imageArray[1] = displayGray(compressImage);	
		}else if(fileType.equals("rgb")){
			imageArray[0] = displayRGB(originalImage);
			imageArray[1] = displayRGB(compressImage);
		}
		return imageArray;
	}

	public static BufferedImage displayRGB(byte[] bytesRGB) {
		// TODO Auto-generated method stub
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);		
		int ind = 0;
		for(int y = 0; y < 288; y++){
			for(int x = 0; x < 352; x++){
				byte r = bytesRGB[ind];
				byte g = bytesRGB[ind+352*288];
				byte b = bytesRGB[ind+352*288*2];
				int pixx = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				ind++;
				img.setRGB(x,y,pixx);
			}
		}
		return img;
	}
	public static BufferedImage displayGray(byte[] bytesGray) {
		// TODO Auto-generated method stub
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);		
		int ind = 0;
		for(int y = 0; y < 288; y++){
			for(int x = 0; x < 352; x++){
				int r = bytesGray[ind];
				int pixx = 0xff000000 | ((r & 0xff) << 16) | ((r & 0xff) << 8) | (r & 0xff);
				ind++;
				img.setRGB(x,y,pixx);
			}
		}
		return img;
	}
}

