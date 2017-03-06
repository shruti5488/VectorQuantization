import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class ImageByte {
	BufferedImage img;
	byte[] bytes ;
	int width = 352 ;
	int height = 288;

	ImageByte(String args){
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

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

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	CodeVector(int x, int y){
		super(x,y);
		numVectors = 0;
		vectorCluster = new PixelVector(0, 0);
//		vectorCluster.x = 0;
//		vectorCluster.y = 0;
	}
}
public class ImageCompression {

	ArrayList<PixelVector> pixelVector = new ArrayList<PixelVector>();
	Map<Integer, CodeVector> codeVector = new HashMap<Integer, CodeVector>();

	public void createVectorSpace(byte[] bytes) {
		// TODO Auto-generated method stub
		int x, y;
		Byte b;
		for(int i =0; i< bytes.length; i+=2){
			
			x = (int)bytes[i] & 0x000000FF;
			b = bytes[i+1];
			y = (int)bytes[i+1] & 0x000000FF;
			pixelVector.add(new PixelVector(x,y));
		}	
	}

	public void InitializeCodeVectors(int n) {
		// TODO Auto-generated method stub

		int blockNo = (int) (Math.log(n)/Math.log(2));
		System.out.println(blockNo);
		int blockSize = 256/blockNo;
		int x = 0, y =0;
		x = (x + blockSize)/2-1; //31
		y = (y + blockSize)/2-1; //31
		int index = 0;
		for(int i = 0; i<blockNo;i++){
			for(int j=0;j<blockNo;j++){
				CodeVector vector = new CodeVector(x, y);
				codeVector.put(index, vector);
				System.out.println(x + " " + y + " " + j);
				y = y + blockSize;
				index++;
			}
			x = x + blockSize;
			y = blockSize/2 - 1;
		}	
	}

	public int computeEuclideanDistance(PixelVector vector, CodeVector codeVector) {
		// TODO Auto-generated method stub
		int x = (int) Math.pow(Math.abs(vector.x - codeVector.x), 2);
		int y = (int) Math.pow(Math.abs(vector.y - codeVector.y), 2);
		int euclideanDist = Math.abs(x + y);
		return euclideanDist;
	}
	
	public int checkConverge(int averageX, int averageY, CodeVector codeVector) {
		// TODO Auto-generated method stub
		int x = Math.abs(averageX - codeVector.x);
		int y =  Math.abs(codeVector.y - codeVector.y);
		return x + y;
	}
	
	public int updateCodeBook(CodeVector codeVector) {
		// TODO Auto-generated method stub
		boolean convergeCodeVector = false;
		int averageX = Math.round(codeVector.vectorCluster.x/codeVector.numVectors);
		int averageY = Math.round(codeVector.vectorCluster.y/codeVector.numVectors);
		
		int difference = checkConverge(averageX, averageY, codeVector);
		codeVector.x = averageX;
		codeVector.y = averageY;
		
		convergeCodeVector = (difference == 0) ? true : false;
		clear(codeVector);
		return difference;
	}

	public void clear(CodeVector codeVector) {
		// TODO Auto-generated method stub
		codeVector.numVectors = 0;
		codeVector.vectorCluster.x = 0;
		codeVector.vectorCluster.y = 0;
		
	}

	public void clusterizeVectors() {
		// TODO Auto-generated method stub
		
		
		boolean convergeCodeVector = false;
		
		while(!convergeCodeVector){
			for(PixelVector vector : pixelVector){
				int indexcodeVector = findNearestCodeWord(vector);
				
				CodeVector clustorcodeVector = codeVector.get(indexcodeVector);
				clustorcodeVector.vectorCluster.x = clustorcodeVector.vectorCluster.x + vector.x;
				clustorcodeVector.vectorCluster.y = clustorcodeVector.vectorCluster.y + vector.y;
				clustorcodeVector.numVectors++;
				
			}
			
			convergeCodeVector = true;
			for(Entry<Integer, CodeVector> codeVector : codeVector.entrySet()){
				int difference = updateCodeBook(codeVector.getValue());
//				System.out.print(codeVector.getValue().x + " " +codeVector.getValue().y + ", ");
				if(difference!=0){
					convergeCodeVector = false;
				}
			}	
			System.out.println();
		}
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String string = "/Users/shruti5488/Documents/JAVA/JavaPractice/String1/ImageCompression/image1.rgb 4";
		String[] stringArg = string.split(" ");
		ImageByte originalImage = new ImageByte(stringArg[0]);
		ImageByte compressImage = new ImageByte(stringArg[0]);

		ImageCompression imageCompress = new ImageCompression();
		imageCompress.createVectorSpace(compressImage.bytes);
		imageCompress.InitializeCodeVectors(Integer.parseInt(stringArg[1]));
		imageCompress.clusterizeVectors();
		
		for(Entry<Integer, CodeVector> codeVector : imageCompress.codeVector.entrySet()){
			System.out.println(codeVector.getValue().x + " " + codeVector.getValue().y );
		}
		

	}

}
