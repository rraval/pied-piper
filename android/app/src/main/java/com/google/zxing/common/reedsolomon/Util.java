package com.google.zxing.common.reedsolomon;

import java.util.ArrayList;


public class Util {

	
	public static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
                result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
}	
	
	
	public static String toHex(byte[] buf) {
        if (buf == null)
                return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
                appendHex(result, buf[i]);
//              if(i > 0 && i%16 == 0){
//            	  result.append("\n");
//              }
              if(i > 0 && i%4 == 0){
            	  result.append(" ");
              }
        }
        return result.toString();
}
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
}

	
}
