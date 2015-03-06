package fr.bytel.bluetoothsample.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class aims at realizing all kind of conversions involving Hexa/int/bytes
 *
 * @author Bertrand Martel Bouygues Telecom
 */
public class ConvertFunctions {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Build a byte array to string with a prefix message
     *
     * @param message
     *            message to print before byte array
     * @param array
     *            byte array to print
     * @param separator
     *            separator between byte values
     * @return string message
     */
    public static String byteArrayToStringMessage(String message, byte[] array,
                                                  char separator) {
        String log = "";
        if (!message.equals(""))
            log = message + " : ";

        for (int count = 0; count < array.length; count++) {
            if (count == 0) {
                log += (ConvertFunctions.convertFromIntToHexa(array[count])
                        + " " + separator + " ");
            } else if (count != array.length - 1) {
                log += (ConvertFunctions.convertFromIntToHexa(array[count])
                        + " " + separator + " ");
            } else {
                log += (ConvertFunctions.convertFromIntToHexa(array[count]));
            }
        }
        return log;
    }
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
    /**
     * convert byte array to hex dump
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];

        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);

    }

    /**
     * <b>Determine if an array is not empty (not full of 0s)</b>
     *
     * @param array
     *            byte array to test
     * @return true if full of 0s and false if not
     */
    public static boolean isEmptyArray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0)
                return false;
        }
        return true;
    }

    /**
     *
     * Convert integer to byte array of size 2 if length <65535
     *
     * @param i
     *            integer to convert
     * @return
     */
    public static byte[] convertIntToByte2Array(int i) {
        byte[] result = new byte[2];
        result[0] = (byte) (i >> 8);
        result[1] = (byte) (i >> 0);
        return result;
    }

    /**
     * Function used to convert byte array to hexa string
     *
     * @param table
     *            byte array
     * @return hexa string
     */
    public static String convertByteArrayToString(byte[] table) {
        String text = "";
        for (int count = 0; count < table.length; count++) {
            text = text + ConvertFunctions.convertFromIntToHexa(table[count]);
        }
        return text;
    }

    /**
     * Convert a byte array to format in String comma separated
     *
     * @param data
     *            byte array to format
     * @return string with byte value converted to integer and separated by
     *         comma
     */
    public static String convertByteArrayToStringCommaSeparated(byte[] data) {
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            ret += (data[i] & 0xFF) + ",";
        }
        return ret.substring(0, ret.length() - 1);
    }

    /**
     *
     * Convert integer to byte array of size 2 if length <65535
     *
     * @param i
     *            integer to convert
     * @return
     */
    public static byte[] convertIntToByte3Array(int i) {
        byte[] result = new byte[3];
        result[0] = (byte) (i >> 16);
        result[1] = (byte) (i >> 8);
        result[2] = (byte) (i >> 0);
        return result;
    }

    /**
     * Compare two byte array value and return true if array value are identical
     *
     * @param array1
     *            first array to compare
     * @param array2
     *            second array to compare
     * @return true if array1 and array2 are identical and false if not
     */
    public static boolean compareTwoByteArray(byte[] array1, byte[] array2) {
        if (array1 != null && array2 != null) {
            if (array1.length == array2.length) {
                for (int i = 0; i < array1.length; i++) {
                    if (array1[i] != array2[i]) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static byte[] convertIntToByteArray(int arg) {
        BigInteger bigInt = BigInteger.valueOf(arg);
        return bigInt.toByteArray();
    }

    /**
     *
     * Convert integer to byte array of size 4
     *
     * @param i
     *            integer to convert
     * @return
     */
    public static byte[] convertIntToByte4Array(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i >> 0);
        return result;
    }

    /**
     * Convert from int data into String hexadecimal (ex 255 => "0xFF")
     *
     * @param data
     *            data to convert into hexa
     * @return
     *
     *         data converted into hexa
     */
    public static String convertFromIntToHexa(byte data) {
        int dataTmp = data & 0xFF;
		/* Put character in uppercase */
        String value = Integer.toHexString(dataTmp).toUpperCase();
		/* Add 0 if length equal to 1 */
        if (value.length() == 1) {
            value = "0" + value;
        }
        return value;
    }

    /**
     *
     * Convert a byte array to integer
     *
     * @param array
     *            byte array
     * @return integer value
     */
    public static int convertByteArrayToInt(byte[] array) {
        int ret = 0;
        for (int i = 0; i < array.length; i++) {
            ret += (array[i] & 0xFF) << ((array.length - 1 - i) * 8);
        }
        return ret;
    }

    /**
     *
     * Convert String with integer comma separated to array
     *
     * @param arg
     *            arg
     * @return byte array
     */
    public static byte[] convertStringToByteArray(String arg) {

        String temp = "";
        List<byte[]> byteList = new ArrayList<byte[]>();

        for (int i = 0; i < arg.length(); i++) {

            if (arg.charAt(i) != ',') {
                temp += arg.charAt(i);
            } else {
                byteList.add(new byte[] { (byte) Integer.parseInt(temp) });
                temp = "";
            }
        }
        byteList.add(new byte[] { (byte) Integer.parseInt(temp) });

        byte[] array = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            array[i] = byteList.get(i)[0];
        }
        return array;
    }

    /**
     * Convert a list of string values in a single string with values
     * "separatored" separated
     *
     * @param listOfString
     *            a list containing string
     * @return a single string with all values "separatored" separated
     */
    public static String convertStringListCommaSeparated(
            List<String> listOfString, String separator) {
        String temp = "";
        if (listOfString.size() > 0) {
            for (int i = 0; i < listOfString.size(); i++) {
                temp += listOfString.get(i) + separator;
            }
            return temp.substring(0, temp.length() - separator.length());
        } else {
            return temp;
        }
    }
}
