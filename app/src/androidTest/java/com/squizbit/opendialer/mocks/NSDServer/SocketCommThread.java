package com.squizbit.opendialer.mocks.NSDServer;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * A socket communication thread which processes Pairing requests from a client device. A call to
 * {@link #shutDownThread()} should be made by the owner when the thread is no longer needed.
 */
public class SocketCommThread extends Thread {

    public static final String REQUEST_CONNECT_CLIENT = "request-connect-client";
    public static final String SEND_LOGIN_TOKEN = "request-device-pair";
    private static final String TAG = "SocketCommThread";

    private ServerSocket mServerSocket;
    private Context mContext;

    private Boolean mDoDisplayError = false;
    private int mErrorStatus = 400;
    private String mWhereToError = REQUEST_CONNECT_CLIENT;


    public SocketCommThread(Context context, ServerSocket serverSocket) {
        mContext = context;
        mServerSocket = serverSocket;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Socket socket = null;

            try {
                socket = acceptSocket();

                if (socket == null) {
                    //Socket creating didn't work, jumping to the start of the loop
                    continue;
                }

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                JSONObject request = readRequest(dataInputStream);

                switch (request.getString("request")) {
                    case REQUEST_CONNECT_CLIENT:
                        //This is a quick paired response
                        if(mDoDisplayError && request.getString("request").equals(mWhereToError)){
                            writeStatus(dataOutputStream, mErrorStatus);
                        } else {
                            processDeviceSerialRequest(dataOutputStream);
                        }
                        break;
                    case SEND_LOGIN_TOKEN:
                        //This requires an server connection, so it needs a larger timeout
                        if(mDoDisplayError && request.getString("request").equals(mWhereToError)){
                            writeStatus(dataOutputStream, mErrorStatus);
                        } else {
                            writeStatus(dataOutputStream, 200);
                        }
                        break;
                    default:
                        writeStatus(dataOutputStream, 404);
                        break;
                }

            } catch (IOException | JSONException je) {
                //Nothing we can do here, just ignore the connection and hope they again
            }

            closeSocket(socket);

        }
        Log.d(TAG, "Ending comms thread");

    }

    public void setDoDisplayError(String whereToError, int errorStatus){
        mWhereToError = whereToError;
        mErrorStatus = errorStatus;
        mDoDisplayError = true;
    }

    public void setDisplaySuccess(){
        mDoDisplayError = false;
    }


    /**
     * Stops the SocketCommsThread
     */
    public void shutDownThread(){
        interrupt();
    }

    private Socket acceptSocket() {
        try {
            return mServerSocket.accept();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to accept incoming socket", ioe);
            return null;
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to close socket", ioe);
        }
    }


    private JSONObject readRequest(DataInputStream dataInputStream) {
        String rawData = null;

        try {
            rawData = dataInputStream.readUTF();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to read input from socket", ioe);
        }

        JSONObject jsonObject = new JSONObject();


        if (rawData != null) {
            try {
                jsonObject = new JSONObject(rawData);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to build JSONObject from input", je);
            }
        }

        if (jsonObject == null) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("request", "unknown");
            } catch (JSONException je) {
                Log.wtf(TAG, "Failed to build our own request object", je);
            }
        }

        return jsonObject;
    }

    private void processDeviceSerialRequest(DataOutputStream dataOutputStream) {
        String serialNumber = StroneNSDManager.getSerialNumber();
        try {
            JSONObject responseObject = new JSONObject();
            responseObject.put("serialnumber", serialNumber);
            responseObject.put("status", 200);
            writeJSONObject(dataOutputStream, responseObject);
        } catch (JSONException e) {
            writeStatus(dataOutputStream, 500);
        }
    }


    private void writeStatus(DataOutputStream dataOutputStream, int status) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", status);
            dataOutputStream.writeUTF(jsonObject.toString());
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Failed to write error", e);
        }
    }

    private void writeJSONObject(DataOutputStream dataOutputStream, JSONObject jsonObject){
        try {
            dataOutputStream.writeUTF(jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, "Failed to write error", e);
        }
    }



}
