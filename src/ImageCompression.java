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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

	public int getAverage(int vectorTotal, int numVectors){
		return  Math.round(vectorTotal/numVectors);
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
			convergeCodeVector = updateCodeBook(codeVector);
			count ++;
		}
//		System.out.println("Total count: " + count);
	}

	public boolean updateCodeBook(Map<Integer, CodeVector> codeVector) {
		boolean flag = true;
		for(Entry<Integer, CodeVector> codeVectorValue :  codeVector.entrySet()){
			if(codeVectorValue.getValue().numVectors != 0){
				int averageX = getAverage(codeVectorValue.getValue().vectorCluster.x, codeVectorValue.getValue().numVectors);
				int averageY = getAverage(codeVectorValue.getValue().vectorCluster.y, codeVectorValue.getValue().numVectors);
				int difference = checkConverge(averageX, averageY, codeVectorValue.getValue());

				if(difference!=codeVectorValue.getValue().difference){
					codeVectorValue.getValue().x = averageX;
					codeVectorValue.getValue().y = averageY;
					codeVectorValue.getValue().difference = difference;
					flag = false;
				}
				clear(codeVectorValue.getValue());
			}
		}
		return flag;
	}

	public void quantizeInputVector(byte[] bytes) {
		// TODO Auto-generated method stub
		int i =0;
		double errX = 0, errY = 0;
		for(i=0; i<bytes.length; i+=2){
			int x = convertByteToInt(bytes[i]);
			int y = convertByteToInt(bytes[i+1]);
			PixelVector pixelVector = new PixelVector(x, y);
			int key = findNearestCodeWord(pixelVector);
			bytes[i] = (byte) codeVector.get(key).x;
			bytes[i+1] = (byte) codeVector.get(key).y;
			
			errX += Math.pow(codeVector.get(key).x - pixelVector.x, 2);
			errY += Math.pow(codeVector.get(key).y - pixelVector.y, 2);
		}
		
		double meanX = errX/pixelVector.size();
		double meanY = errY/pixelVector.size();
		double meanErr =  (meanX + meanY)/2.0;
		
		System.out.println("Computed Mean Square Error(MSE) is " + meanErr);
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
//		long time = System.currentTimeMillis();
//		String string = "/Users/shruti5488/Documents/JAVA/JavaPractice/String1/ImageCompression/image1.rgb 256";
//		args = string.split(" ");
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

		String fileType = args[0].split("\\.")[1];
		
		ImageByte originalImage =new ImageByte();
		originalImage.getBytes(args[0]);
		ImageByte compressImage = new ImageByte();
		compressImage.getBytes(args[0]);
		System.out.println("Please wait while the compressed image is loading..");
		ImageCompression imageCompress = new ImageCompression();
		System.out.println("Creating vector space..");
		imageCompress.createVectorSpace(compressImage.bytes);
		System.out.println("Initializing codebook..");
		imageCompress.InitializeCodeVectors(Integer.parseInt(args[1]));
		System.out.println("Clustering vectors around each code word..");
		imageCompress.clusterizeVectors();
		System.out.println("Quantizing input vectors..");
		imageCompress.quantizeInputVector(compressImage.bytes);

		BufferedImage[] image = originalImage.getBufferedImage(fileType,originalImage.bytes,compressImage.bytes);
		System.out.println("The image is compressed successfully !");
		originalImage.displayImage(image);
//		long time2 = System.currentTimeMillis();
//		NumberFormat formatter = new DecimalFormat("#0.00000");
//		System.out.print("Execution time is " + formatter.format((time2 - time) / 1000d) + " seconds");
	}
}