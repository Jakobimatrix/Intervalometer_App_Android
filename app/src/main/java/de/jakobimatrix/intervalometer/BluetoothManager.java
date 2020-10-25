package de.jakobimatrix.intervalometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.Math.min;

public class BluetoothManager {

    private static BluetoothManager mInstance= null;

    protected BluetoothManager(){initializer("");}

    public static synchronized BluetoothManager getInstance() {
        if(null == mInstance){
            mInstance = new BluetoothManager();
        }
        return mInstance;
    }

    private void initializer(String device_name){
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(isBluetoothSupported()){
            getPairedDevices(true);
            try {
                connect(device_name);
            }catch (IOException e) {
                // device not avaiable,
            }
        }
    }

    public boolean isConnected() {
        return socket != null;
    }

    public boolean isBluetoothSupported(){
        return adapter != null;
    }

    public boolean isBluetoothEnabled(){
        return adapter.isEnabled();
    }

    /*!
     * \brief connect tries to connect to a device, which must be already paired.
     * \param device_name name of the device to open a connection to.
     * \return true if device was found. false if not. Throws exception if found but connection failed.
     */
    public boolean connect(String device_name) throws IOException {
        Log.i("BT::connect","start");
        for (BluetoothDevice device : current_paired_devices) {
            if(device_name.equals(device.getName())){
                connected_device = device;
                break;
            }
        }
        if(connected_device == null){
            return false;
        }

        ParcelUuid[] uuids = connected_device.getUuids();
        socket = connected_device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        // Creating new connections to remote Bluetooth devices should not be attempted while device discovery is in progress.
        adapter.cancelDiscovery();
        // This method will block until a connection is made or the connection fails.
        // If this method returns without an exception then this socket is now connected.
        socket.connect();
        output_stream = socket.getOutputStream();
        input_stream = socket.getInputStream();
        return true;
    }

    public void disconnect() throws IOException {
        if(isConnected()) {
            socket.close();
            output_stream = null;
            input_stream = null;
            socket = null;
            connected_device = null;
        }
    }

    public boolean send(byte[] buffer) throws IOException {
        if(output_stream != null) {
            output_stream.write(buffer, 0, buffer.length);
            return true;
        }
        return false;
    }

    public void refresh(){
        getPairedDevices(true);
    }

    public boolean read(int length_byte, StringBuilder message) throws IOException {
        if(input_stream == null){
            return false;
        }
        if(input_stream.available() <= 0){
            return false;
        }
        final int read_bytes = min(input_stream.available(), length_byte);

        byte[] buffer = new byte[read_bytes];
        final int index_start = 0;
        final int num_bytes_red = input_stream.read(buffer, index_start, read_bytes-index_start);

        message.append(Utility.bytes2string(buffer,read_bytes));
        return true;
    }

    /*!
     * \brief getPairedDeviceDames returns a list of all devices which are paired and ready for communication.
     * \param paired_devices An Vector to fill in the device names.
     */
    public void getPairedDeviceNames(ArrayList<String> paired_devices){
        for (BluetoothDevice device : current_paired_devices) {
            paired_devices.add(device.getName());
        }
    }

    /*!
     * \brief getPairedDevices scans for all paired devices and saves them into this->current_paired_devices.
     */
    private void getPairedDevices(boolean refresh){
        if(refresh) {
            current_paired_devices = adapter.getBondedDevices();
        }
    }
    private BluetoothSocket socket = null;
    private OutputStream output_stream = null;
    private InputStream input_stream = null;
    private BluetoothAdapter adapter = null;
    private BluetoothDevice connected_device = null;
    private Set<BluetoothDevice> current_paired_devices;
}
