package nio.common;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;


public class NIOUtil {
    public static final int BUFFER_SIZE = 1024;

    public static ByteBuffer getByteBuffer(Object object) throws Exception {
        return ByteBuffer.wrap(getByteArray(object));
    }

    public static byte[] getByteArray(Object object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        if (!(object instanceof Serializable)) {
            throw new IOException("object not instanceof Serializable");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        try {
            objectOutput.writeObject(object);
            objectOutput.flush();

            return byteArrayOutputStream.toByteArray();
        } finally {
            objectOutput.close();
            byteArrayOutputStream.close();
        }
    }

    public static Object getObject(ByteBuffer byteBuffer) throws IOException, ClassNotFoundException {
        InputStream inputStream = new ByteArrayInputStream(byteBuffer.array());
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            try{
                Object object = objectInputStream.readObject();
                return object;
            }finally {
                objectInputStream.close();
            }

        }finally {
            inputStream.close();
        }
    }

}
