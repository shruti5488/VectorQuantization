
Image compression using Vector Quantization for grayscale and colored images

Here are the steps that you need to implement to compress an image.
1.	Understanding your two pixel vector space to see what vectors your image contains
2.	Initialization of codewords - select N initial codewords
3.	Clustering vectors around each code word 
4.	Refine and Update your code words depending on outcome of 3.
Repeat steps 3 and 4 until code words don’t change or the change is very minimal.
5.	Quantize input vectors to produce output image

