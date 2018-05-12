import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataStructure {
    static final BigInteger TWO = new BigInteger("2");
    private static SecureRandom secureRandom = new SecureRandom();

    private static byte[] getTemplate() {
        byte[] message = new byte[256];
        //signature
        message[0] = 0x41;
        message[1] = 0x53;
        message[2] = 0x43;
        message[3] = 0x50;
        message[4] = 0x00;
        message[5] = 0x01;

        return message;
    }

    public static byte[] getPlainMessage(String text) {
        byte[] message = getTemplate();
        byte[] plainText;
        try {
            if (text.length() > 216) {
                String sub = text.substring(0, 216);
                plainText = sub.getBytes("UTF-8");
            } else {
                plainText = text.getBytes("UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return message;
        }

        for (int i = 0; i < plainText.length; i++) {
            message[i + 20] = plainText[i];
        }

        //Write the length of the message
        message[9] = new Integer(plainText.length).byteValue();
        for (int i = 0; i < plainText.length; i++) {
            message[i + 20] = plainText[i];
        }

        byte[] completeMessage = new byte[236];
        for (int i = 0; i < 236; i++) {
            completeMessage[i] = message[i];
        }
        byte[] mac = hashSHA1(completeMessage);
        System.out.println(byteArray2Hex(mac));

        for (int i = 0; i < 20; i++) {
            message[i + 236] = mac[i];
        }

        return message;
    }

    public static String getMessage(byte[] data) {
        boolean validSignature = (data[0] == 0x41)
                        && (data[1] == 0x53)
                        && (data[2] == 0x43)
                        && (data[3] == 0x50);

        String message = null;
        if (validSignature) {
            message = "";
            int messageLength = unsignedToBytes(data[9]);
            for (int i = 0; i < messageLength; ++i) {
                message = message + Character.toString((char) data[20 + i]);
            }

            byte[] completeMessage = new byte[236];
            for (int i = 0; i < 236; i++) {
                completeMessage[i] = data[i];
            }
            byte[] verifedMac = hashSHA1(completeMessage);
            byte[] mac = new byte[20];
            for (int i = 0; i < 20; i++) {
                mac[i] = data[236 + i];
            }

            if (!Arrays.equals(mac, verifedMac)) {
                message = "TAMPERED MESSAGE:\n" + message;
            }

        } else {
            System.out.println("Invalid signature.");
            message = "";
        }

        return message;
    }

    private static String byteArray2Hex(final byte[] hash) {
        java.util.Formatter formatter = new java.util.Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static byte[] hashSHA1(byte[] hashThis) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(hashThis);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptData(byte[] data, char[] key) throws Exception {
        System.out.println(key);
        byte[] keyBytes = toBytes(key);

        SecretKeySpec secret = new SecretKeySpec(keyBytes, "DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);

        return cipher.doFinal(data);
    }

    public static byte[] decryptData(byte[] data, char[] key) throws Exception {
        byte[] keyBytes = toBytes(key);
        SecretKeySpec secret = new SecretKeySpec(keyBytes, "DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secret);

        return cipher.doFinal(data);
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public static Variables getVariables(String message) throws Exception {
        String[] variables = message.split(",");
        if (variables.length != 3) {
            throw new Exception("Invalid initialization message: number of variables.");
        }

        BigInteger q = null, a = null, y = null;

        for (String variable : variables) {
            String[] pair = variable.split("=");
            if (pair.length != 2) {
                throw new Exception("Invalid initialization message: not a pair.");
            }

            if (pair[0].equals("q")) {
                q = new BigInteger(pair[1]);
            } else if (pair[0].equals("a")) {
                a = new BigInteger(pair[1]);
            } else if (pair[0].equals("y")) {
                y = new BigInteger(pair[1]);
            }
        }
        return new Variables(q, a, y);
    }

    public static char[] validateKey(BigInteger privateKey) {
        String key = privateKey.toString();
        if (key.length() > 8) {
            key = key.substring(0, 8);
        } else if (key.length() < 8) {
            for (int i = 0; i < (8 - key.length()); i++) {
                key += "0";
            }
        }
        return key.toCharArray();
    }

    public static BigInteger randomBigInteger(BigInteger max) {
        BigInteger result;
        do {
            result = new BigInteger(max.bitLength(), secureRandom);
        } while (result.compareTo(max) >= 0);
        return result;
    }

    public static String createInitialization(BigInteger q, BigInteger a, BigInteger y) {
        return "q=" + q.toString() + ",a=" + a.toString() + ",y=" + y.toString();
    }

    public static BigInteger fastExp(BigInteger C, BigInteger d, BigInteger n)
    {
        if (d.doubleValue() == 0.0D) {
            return new BigInteger("1");
        } else {
            return d.mod(new BigInteger("2")).doubleValue() == 0.0D ? fastExp(C.multiply(C).mod(n), d.divide(new BigInteger("2")), n) : C.multiply(fastExp(C, d.add(new BigInteger("-1")), n)).mod(n);
        }
    }

}


