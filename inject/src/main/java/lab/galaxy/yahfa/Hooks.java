package lab.galaxy.yahfa;

import com.xiyuan.inject.Hook_Activity_onCreate;
import com.xiyuan.inject.Hook_Intent_constructor;
import com.xiyuan.inject.Hook_SecretKeySpec_Constructor;
import com.xiyuan.inject.Hook_Tfcc_b;

/**
 * Created by liuruikai756 on 31/03/2017.
 */

public class Hooks {

    public static Class[] hooks = {
            Hook_Activity_onCreate.class,
            Hook_Intent_constructor.class,
            Hook_Tfcc_b.class,
            Hook_SecretKeySpec_Constructor.class
    };

}
