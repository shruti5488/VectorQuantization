class CodeVector extends PixelVector{
	PixelVector vectorCluster;
	int numVectors;
	int difference;
	boolean flag;

	CodeVector(int x, int y){
		super(x,y);
		numVectors = 0;
		difference = 0;
		vectorCluster = new PixelVector(0, 0);
		flag = false;
	}


}