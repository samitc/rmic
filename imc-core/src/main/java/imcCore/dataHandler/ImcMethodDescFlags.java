package imcCore.dataHandler;

public class ImcMethodDescFlags {
    private final static int IS_SEND_RET_OBJ = 0;
    private final static int IS_SEND_PARAMS = 1;

    private static byte getIntWithFlag(int flag) {
        return (byte) (1 << flag);
    }

    private static boolean checkFlagWithInt(byte flag, int intVal) {
        return (flag & getIntWithFlag(intVal)) != 0;
    }

    public static byte readFlags(MethodPocket methodPocket) {
        byte flags = 0;
        if (methodPocket.hasRetObj()) {
            flags |= getIntWithFlag(IS_SEND_RET_OBJ);
        }
        if (methodPocket.hasParams()) {
            flags |= getIntWithFlag(IS_SEND_PARAMS);
        }
        return flags;
    }

    public static boolean hasSendRetObj(byte flag) {
        return checkFlagWithInt(flag, IS_SEND_RET_OBJ);
    }

    public static boolean hasSendParams(byte flag) {
        return checkFlagWithInt(flag, IS_SEND_PARAMS);
    }
}
