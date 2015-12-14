package com.squizbit.opendialer.mocks.NSDServer;

import android.content.Context;

import java.util.Random;

public class StroneNSDManager {
    private static final String TAG = "Hiten Server";
    private static String sSerialNumber = null; // Currently hard - coded. In the later stages it will be fetched using a JNI wrapper.


    private static String generateRandomSerial(){
        Random random = new Random(System.currentTimeMillis());
        String randomSerial = "";
        for(int i = 0; i < 16; i++ ){
            int value = random.nextInt(9);
            randomSerial += Integer.toString(value);
        }

        //Current logic requires it to be even
        randomSerial += "2";
        return randomSerial;
    }

    public StroneNSDManager(Context context) {
    }

    public static String getSerialNumber(){
        if(sSerialNumber == null){
            sSerialNumber = forceRegenSerialNumber();
        }

        return sSerialNumber;
    }

    public static String forceRegenSerialNumber(){
        sSerialNumber =  generateRandomSerial();
        return sSerialNumber;
    }


    public void setSerialNumber(String serialNumber) {
       sSerialNumber = serialNumber;
    }

    public static String getServiceNameFromSerialNumber(String deviceType) {
        String serialNumber = getSerialNumber();
        return deviceType + "-" + serialNumber.substring(serialNumber.length() - 4);
    }
}
