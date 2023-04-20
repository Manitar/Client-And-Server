package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LineMessageEncoderDecoder implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    //SEND to client
    @Override
    public byte[] encode(String message) {
        //here i want to add only 2 bytes of a random short encoded!
        //so we know that the given string is "XX|sssssss\0"
        String op1 = message.substring(0,message.indexOf('|'));
        short  op1S = Short.parseShort(op1);
        byte [] op1B = shortToBytes(op1S);
        message= message.substring(message.indexOf('|')+1);
        //so far i managed to get the values of the op encoded right
        //now lets add the second op!
        String op2 = message.substring(0,message.indexOf('|'));
        short  op2S = Short.parseShort(op2);
        if(op2S==10)
            op2S =14;
        byte [] op2B = shortToBytes(op2S);

        message= message.substring(message.indexOf('|')+1);

        byte [] mB = (message+ "\n").getBytes();
        byte [] ans = new byte[4+mB.length];
        ans[0] = op1B[0];
        ans[1] = op1B[1];
        ans[2] = op2B[0];
        ans[3] = op2B[1];
        for(int i=4; i<ans.length; i++)
            ans[i] = mB[i-4];
        return (ans); //uses utf8 by default
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    //READ from client
    @Override
    public String decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '\n') {
            return popString();
        }
        pushByte(nextByte);
        return null; //not a line yet
    }

    private void pushByte(byte nextByte) {
        if (len  >= bytes.length)
                bytes = Arrays.copyOf(bytes, (len * 2));

        bytes[len] = nextByte;
        len++;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        short oopp = bytesToShort();
        String message = "";
        if(oopp==0) {
            oopp = 10;
        }
        else if(oopp!=4 && oopp!=11)
            message = new String(bytes, 2, len-2, StandardCharsets.UTF_8);
        len = 0;
        bytes = new byte[1<<10];
        return String.valueOf(oopp) +"|"+ message;
    }

    public short bytesToShort() {
        short result = (short)((bytes[0] & 0xff) << 8);
        result += (short)(bytes[1] & 0xff);
        return result;

    }

}
