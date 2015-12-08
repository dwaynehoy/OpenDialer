package com.squizbit.opendialer.mocks.NSDServer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;


public class NSDPairingServer {

    private final String mServiceName;
    private final Context mContext;
    private ServerSocket mServerSocket;

    private String SERVICE_TYPE = "_roam._tcp.";
    private SocketCommThread mSocketCommsThread;


    public NSDPairingServer(Context context){
        StroneNSDManager.forceRegenSerialNumber();
        mServiceName = StroneNSDManager.getServiceNameFromSerialNumber("Roam");
        mContext = context;
    }

    public String start(){
        return startNSDService();
    }

    private NsdManager.RegistrationListener mNsdRegistrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
        }
    };

    private ServerSocket buildServerSocket() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
        }

        return serverSocket;
    }

    private String startNSDService() {

        mServerSocket = buildServerSocket();
        if (mServerSocket == null) {
            return null;
        }

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(mServerSocket.getLocalPort());

        mSocketCommsThread = new SocketCommThread(mContext, mServerSocket);
        mSocketCommsThread.start();

        NsdManager manager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        manager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mNsdRegistrationListener);

        return mServiceName;
    }

    public void doSendError(String whereToError, int errorStatus){
        mSocketCommsThread.setDoDisplayError(whereToError, errorStatus);
    }

    public void doSendSuccess(){
        mSocketCommsThread.setDisplaySuccess();
    }

    public void stop(){
        stopNSDService();
    }

    private void stopNSDService() {
        NsdManager manager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        manager.unregisterService(mNsdRegistrationListener);

        if (mSocketCommsThread != null && !mSocketCommsThread.isInterrupted()) {
            mSocketCommsThread.interrupt();
        }

        if (mServerSocket != null && !mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
            } catch (IOException io) {
                //Absolutely nothing we can do here, not even usefully to log it.
            }
        }
    }
}
