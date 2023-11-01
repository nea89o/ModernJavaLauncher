package moe.nea.modernjava.launch.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TextIoUtils {
    /**
     * Flush out the standard IO, without closing it. Works even after {@link System#setOut(PrintStream)} or similar has been called
     */
    public static void flushStdIO() {
        try {
            // These streams should not be closed. We just want to flush them out
            //noinspection resource
            new FileOutputStream(FileDescriptor.out).flush();
            //noinspection resource
            new FileOutputStream(FileDescriptor.err).flush();
        } catch (IOException ignored) {
        }
    }
}
