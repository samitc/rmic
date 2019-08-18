package imcCore.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StreamUtil {
    public static int bytesToInt(byte[] data, int startIndex) {
        return ((data[startIndex] & 0xFF) << 24) | ((data[1 + startIndex] & 0xFF) << 16)
                | ((data[2 + startIndex] & 0xFF) << 8) | (data[3 + startIndex] & 0xFF);
    }

    public static byte[] intToBytes(int data) {
        byte[] buf = new byte[4];
        addIntToByte(buf, data, 0);
        return buf;
    }

    public static void addIntToByte(byte[] data, int num, int startIndex) {
        data[startIndex] = (byte) (num >>> 24);
        data[1 + startIndex] = (byte) (num >>> 16);
        data[2 + startIndex] = (byte) (num >>> 8);
        data[3 + startIndex] = (byte) (num);
    }
    public static void writeString(String s, DataOutput output) throws IOException {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        output.writeInt(data.length);
        output.write(data);
    }
    public static String readString(DataInput input) throws IOException {
        int dataL = input.readInt();
        byte[] data = new byte[dataL];
        input.readFully(data);
        return new String(data, StandardCharsets.UTF_8);
    }
}
