import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
		}else{
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
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_BYTE_INDEXED);		
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

class PixelVector{
	int x, y;

	PixelVector(int x, int y){
		this.x = x;
		this.y = y;
	}
}

class CodeVector extends PixelVector{
	PixelVector vectorCluster;
	int numVectors;
	int difference;

	CodeVector(int x, int y){
		super(x,y);
		numVectors = 0;
		difference = 0;
		vectorCluster = new PixelVector(0, 0);
	}
}
public class ImageCompression {

	LinkedList<PixelVector> pixelVector = new LinkedList<PixelVector>();
	Map<Integer, CodeVector> codeVector = new HashMap<Integer, CodeVector>();

	public void createVectorSpace(byte[] bytes) {
		// TODO Auto-generated method stub
		int x, y;
		for(int i =0; i< bytes.length; i+=2){
			x = convertByteToInt(bytes[i]);
			y = convertByteToInt(bytes[i+1]);
			pixelVector.add(new PixelVector(x,y));
		}	
	}
	public boolean checkIfSquare(int n){
		boolean flag = false;
		double sqrt = Math.sqrt(n);
		int x = (int) sqrt;
		if(Math.pow(sqrt,2) == Math.pow(x,2))
		{
			flag = true;
		}
		return flag;
	}

	public int getNoOfBlocks(int n){
		int square = n/2;
		return (int) Math.sqrt(square);
	}

	public void InitializeCodeVectors(int n) {
		// TODO Auto-generated method stub
		boolean flag = checkIfSquare(n);
		int blockNoi, blockNoj ;
		if(flag == true){
			blockNoi = (int) (Math.sqrt(n));
			blockNoj = blockNoi;
		}else{
			blockNoi = getNoOfBlocks(n);
			blockNoj = n/blockNoi;
		}

		int blockSizei = 256/blockNoi;
		int blockSizej = 256/blockNoj;
		int x = 0, y =0;
		x = (x + blockSizei)/2-1; 
		y = (y + blockSizej)/2-1; 
		int index = 0;
		for(int i = 0; i<blockNoi;i++){
			for(int j=0;j<blockNoj;j++){
				CodeVector vector = new CodeVector(x, y);
				codeVector.put(index, vector);
				y = y + blockSizej;
				index++;
			}
			x = x + blockSizei;
			y = blockSizej/2 - 1;
		}	
		System.out.println(codeVector.size());
	}

	public int computeEuclideanDistance(PixelVector vector, CodeVector codeVector) {
		// TODO Auto-generated method stub
		int x = (int) Math.pow(Math.abs(vector.x - codeVector.x), 2);
		int y = (int) Math.pow(Math.abs(vector.y - codeVector.y), 2);
		int euclideanDist = (int) Math.sqrt(x + y);
		return euclideanDist;
	}

	public int checkConverge(int averageX, int averageY, CodeVector codeVector) {
		// TODO Auto-generated method stub
		int x = Math.abs(averageX - codeVector.x);
		int y =  Math.abs(averageY - codeVector.y);
		return x + y;
	}

	public int getAverage(CodeVector codeVector){
		return  Math.round(codeVector.vectorCluster.x/codeVector.numVectors);
	}

	public int updateCodeBook(CodeVector codeVector) {
		// TODO Auto-generated method stub
		boolean convergeCodeVector = false;
		int difference = 0;
		if(codeVector.numVectors != 0){
			int averageX = getAverage(codeVector);
			int averageY = getAverage(codeVector);
			difference = checkConverge(averageX, averageY, codeVector);

			if(codeVector.difference == difference){
				difference = 0;
			}else{
				codeVector.difference = difference;
			}

			codeVector.x = averageX;
			codeVector.y = averageY;
			convergeCodeVector = (difference == 0) ? true : false;
			clear(codeVector);
		}
		return difference;
	}

	public void clear(CodeVector codeVector) {
		// TODO Auto-generated method stub
		codeVector.numVectors = 0;
		codeVector.vectorCluster.x = 0;
		codeVector.vectorCluster.y = 0;
	}
	public boolean isCodeVectorConverge(boolean convergeCodeVector){
		return !convergeCodeVector;
	}

	public void clusterizeVectors() {
		// TODO Auto-generated method stub
		boolean convergeCodeVector = false;
		int count = 0;
		while(isCodeVectorConverge(convergeCodeVector)){
			convergeCodeVector = true;
			for(PixelVector vector : pixelVector){
				int indexcodeVector = findNearestCodeWord(vector);
				CodeVector clustorcodeVector = codeVector.get(indexcodeVector);
				clustorcodeVector.vectorCluster.x = clustorcodeVector.vectorCluster.x + vector.x;
				clustorcodeVector.vectorCluster.y = clustorcodeVector.vectorCluster.y + vector.y;
				clustorcodeVector.numVectors++;
			}

			for(Entry<Integer, CodeVector> codeVector : codeVector.entrySet()){
				int difference = updateCodeBook(codeVector.getValue());
				if(difference!=0){
					convergeCodeVector = false;
				}
			}	
			count ++;
		}

		System.out.println("Total count: " + count);
	}
	public void quantizeInputVector(byte[] bytes) {
		// TODO Auto-generated method stub
		int i =0;
		for(i=0; i<bytes.length; i+=2){
			int x = convertByteToInt(bytes[i]);
			int y = convertByteToInt(bytes[i+1]);
			PixelVector vector = new PixelVector(x, y);
			int key = findNearestCodeWord(vector);
			bytes[i] = (byte) codeVector.get(key).x;
			bytes[i+1] = (byte) codeVector.get(key).y;
		}
	}

	public int convertByteToInt(byte inputByte){
		return (int)inputByte & 0xff ;
	}

	public int findNearestCodeWord(PixelVector vector) {
		// TODO Auto-generated method stub
		int euclideanDist;
		int minDistance = Integer.MAX_VALUE;
		int indexCodeVector = 0 ;

		for(Entry<Integer, CodeVector> codeVector : codeVector.entrySet()){
			CodeVector cv = codeVector.getValue();
			euclideanDist = computeEuclideanDistance(vector, cv );
			if(euclideanDist<minDistance){
				minDistance = euclideanDist;
				indexCodeVector = codeVector.getKey();
			}
		}
		return indexCodeVector;
	}

	public static boolean checkPowerOfTwo(int n) {
		// TODO Auto-generated method stub
		return (n > 0) && ((n & (n - 1)) == 0);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String string = "/Users/shruti5488/Documents/JAVA/JavaPractice/String1/ImageCompression/image2.raw 256";
		args = string.split(" ");
		if(args.length!=2){
			System.out.println("Error: Unkown parameters entered. Please enter in the below format:");
			System.out.println("ImageCompression myImage.raw N");
			System.out.println("Program terminated..");
			System.exit(0);
		}

		if(!checkPowerOfTwo(Integer.parseInt(args[1]))){
			System.out.println("Error: The entered number of vectors for the codebook is not a power of 2!");
			System.out.println("Program terminated..");
			System.exit(0);
		}

		String fileType = string.split(" ")[0].split("\\.")[1];

		ImageByte originalImage =new ImageByte();
		originalImage.getBytes(args[0]);
		ImageByte compressImage = new ImageByte();
		compressImage.getBytes(args[0]);
		System.out.println("Please wait while the compressed image is loading..");
		ImageCompression imageCompress = new ImageCompression();
		imageCompress.createVectorSpace(compressImage.bytes);
		imageCompress.InitializeCodeVectors(Integer.parseInt(args[1]));
		imageCompress.clusterizeVectors();
		imageCompress.quantizeInputVector(compressImage.bytes);
		System.out.println(imageCompress.codeVector.size());
		//		for(Entry<Integer, CodeVector> codeVector :imageCompress.codeVector.entrySet()){
		//			System.out.println(codeVector.getValue().x + "," + codeVector.getValue().y );
		//		}
		BufferedImage[] image = originalImage.getBufferedImage(fileType,originalImage.bytes,compressImage.bytes);
		System.out.println("The image is compressed successfully !");
		originalImage.displayImage(image);
	}
}
