package imcCore.dataHandler;

import imcCore.dataHandler.classHandlers.ITypeContract;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class FieldHandlerTest {

    private static void addHandlers(List<ITypeContract<?>> allHandlers, Class<?>... classes) {
        for (Class<?> classed : classes) {
            allHandlers.add(FieldHandler.getTypeContract(classed));
        }
    }

    @Test
    public void uniqueTypeIdTest() {
        Set<Integer> allId = new HashSet<>();
        List<ITypeContract<?>> allHandlers = new ArrayList<>();
        addHandlers(allHandlers,
                int.class,
                boolean.class,
                byte.class,
                char.class,
                short.class,
                long.class,
                float.class,
                double.class);
        allHandlers.forEach(iTypeContract -> Assert.assertTrue(allId.add(iTypeContract.getType())));
        Assert.assertEquals(allHandlers.size(), allId.size());
    }
}
