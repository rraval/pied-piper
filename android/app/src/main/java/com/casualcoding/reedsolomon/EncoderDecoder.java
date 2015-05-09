
package com.casualcoding.reedsolomon;

import java.io.ByteArrayOutputStream;

import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

/**
 * 
 * Purpose: to provide a simple class for encoding and decoding/repairing 
 * data using Reed-Solomon forward error correction codes.
 * Data can only be encoded/decoded in 256 byte chunks, which is a limitation
 * of the Reed-Solomon classes that this class wraps
 * 
 * @author Blake Hamilton <blake.a.hamilton@gmail.com>
 * 		   http://www.casual-coding.blogspot.com/
 * @version 1.0	 
 *
 */

public class EncoderDecoder {

	private ReedSolomonDecoder decoder;
	private ReedSolomonEncoder encoder;
	
	public EncoderDecoder(){
		decoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);
		encoder = new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256);
	}
	
	
	/**
	 * Encodes the supplied byte array, generating and appending the supplied number of error correction bytes to be used.
	 * 
	 * 
	 * @param data the bytes to be encoded
	 * @param numErrorCorrectionBytes The number of error correction bytes to be generated from the user supplied data
	 * @return The encoded bytes, where the size of the encoded data is the original size + the number of bytes used for error correction
	 * @throws DataTooLargeException if total size of data supplied by user to be encoded, plus the number of error
	 * correction bytes is greater than 256 bytes
	 */
	public byte[] encodeData(byte[] data, int numErrorCorrectionBytes) throws DataTooLargeException{
				
		if(data == null || data.length == 0){
			return null;
		}
		if( (data.length + numErrorCorrectionBytes) > 256){
			throw new DataTooLargeException("Data Length + Number or error correction bytes cannot exceed 256 bytes");
		}
				
		int totalBytes = numErrorCorrectionBytes + data.length;		
		int[] dataInts = new int[totalBytes];
		
		for(int i = 0; i < data.length; i++){
		  dataInts[i] = data[i] & 0xFF; 
		}
		
		encoder.encode(dataInts, numErrorCorrectionBytes);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		for(int i: dataInts){
			bos.write(i);
		}
		
		return bos.toByteArray();
	}
	
	
	/**
	 * 
	 * Repairs and decodes the supplied byte array, removing the error correction codes and returning the original data
	 * 
	 * @param data The bytes to be repaired/decoded
	 * @param numErrorCorrectionBytes The number of error correction bytes present in the encoded data.
	 * If this field is incorrect the encoded data may not be able to be repaired/encoded
	 * @return The decoded/repaired data. The returned byte array will be N bytes shorter than the supplied 
	 * encoded data, where N equals the number of error correction bytes within the encoded byte array
	 * @throws ReedSolomonException if the data is not able to be repaired/decoded
	 * @throws DataTooLargeException if the supplied byte array is greater than 256 bytes
	 */
	public byte[] decodeData(byte[] data, int numErrorCorrectionBytes) throws ReedSolomonException, DataTooLargeException{
			
		if(data == null || data.length == 0){
			return null;
		}
		if(data.length > 256){
			throw new DataTooLargeException("Data exceeds 256 bytes! Too large");
		}
		
		int[] dataInts = new int[data.length];
		
		for(int i = 0; i < data.length; i++){
		  dataInts[i] = data[i] & 0xFF;
		}
				
		int totalBytes = data.length - numErrorCorrectionBytes; 
		
		decoder.decode(dataInts, numErrorCorrectionBytes);		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		for(int i = 0; i < totalBytes && i < dataInts.length; i++){
			bos.write(dataInts[i]); // read in all the data sans error correction codes
		}
		
		return bos.toByteArray();
	}

	
	public class DataTooLargeException extends Exception{

		private static final long serialVersionUID = 8706995404080410370L;

		public DataTooLargeException(String message){
			super(message);
		}		
	}
	
}
