package uk.co.wideopentech.edges;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FaceDetector {

    private static native void SetCascadeDataNative(final String path);

    public static void init(InputStream str) {

        try {
            File cascadeDir = MainActivity.getContext().getDir("", Context.MODE_PRIVATE);
            File file = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = str.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            str.close();
            os.close();

            SetCascadeDataNative(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
