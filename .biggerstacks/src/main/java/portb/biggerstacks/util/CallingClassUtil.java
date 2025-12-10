/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

public class CallingClassUtil
{
    public static String getCallerClassName()
    {
        for (StackTraceElement element : Thread.currentThread().getStackTrace())
        {
            String className = element.getClassName();
            if (!className.startsWith("net.minecraft") && !className.startsWith("java.lang.Thread") &&
                        !className.startsWith("portb"))
                return className + " [" + element.getFileName() + ":" + element.getLineNumber() + "]";
        }
        
        return null;
    }
}
