package imcCore.Utils;

import org.junit.Assert;

import java.lang.reflect.Array;

public class GeneralTestUtils {
    public static void assertUnknownObj(Object retObj, Object methodPocketObject) {
        if (retObj == null) {
            Assert.assertNull(methodPocketObject);
        } else {
            if (retObj.getClass().isArray()) {
                if (retObj.getClass().getComponentType().isPrimitive()) {
                    int size = Array.getLength(retObj);
                    Assert.assertEquals(size, Array.getLength(methodPocketObject));
                    for (int i = 0; i < size; i++) {
                        Assert.assertEquals(Array.get(retObj, i), Array.get(methodPocketObject, i));
                    }
                } else {
                    Assert.assertArrayEquals((Object[]) retObj, (Object[]) methodPocketObject);
                }
            } else {
                Assert.assertEquals(retObj, methodPocketObject);
            }
        }
    }
}
