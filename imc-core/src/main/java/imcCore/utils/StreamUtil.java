package imcCore.utils;

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
}
