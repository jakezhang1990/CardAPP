package com.cardlan.carddemoapp;

import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.cardlan.carddemoapp.thread.LoopNonUIThread;
import com.cardlan.out.log.BaseLogContainer;
import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * SPI and hardware connection help class,
 * which integrates SPI communication,
 * serial keypad,Pasm card operation, etc.
 * Usage is to open SPI channel line,
 * call the corresponding command to connect and send and receive data.
 */
public class SpiHelper
        extends BaseLogContainer
{

    private byte[]             head            = {0x02};
    private byte           end             = 0x03;
    private byte[]         respond_cmd     = {head[0], 0x10, end, (byte) 0xFF};
    private byte[]         respond_data    = {head[0], 0x10, end, (byte) 0xFF};
    private byte[]         get_cmd         = {head[0], 0x20, end, (byte) 0xFF};
    private byte[]             get_data        = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private byte[]             lenArray        = new byte[4];
    private byte               cmd_open        = 0x01;
    private byte               cmd_close       = 0x02;
    private byte               cmd_write       = 0x03;
    private byte               cmd_read        = 0x04;
    private byte               cmd_colde_start = 0x05;
    private byte               cmd_hot_reset   = 0x06;
    private byte               cmd_data_length = 0x07;
    private byte               cmd_baudrate    = 0x08;
    private CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();
    //send cmd length
    private int                sendLength      = 4;
    int count = 2;
    private byte[] mReadLength;
    private byte[] mReadData;
    String scData;
    //Keypad displays an array of data;{0x1B,0x53,0x30}:The data first,The last 16 are default Spaces;
    byte[] Y485_line_1 = {0x1B,0x53,0x30,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20};
    private boolean mSpi_exit_Flag = true;
    private SPIDispathNonUIThread mSPIDispathNonUIThread;

    private String spiPath = "/dev/spidev2.0";
    private int    flags   = 0;

    private boolean mSpiOpenSuccess;


    public SpiHelper() {
//        mSPIDispathNonUIThread = new SPIDispathNonUIThread();
//        mSPIDispathNonUIThread.start();
//        Spi_rev_thread thread = new Spi_rev_thread();
//        mSpi_exit_Flag = false;
//        thread.start();

    }

    /**
     * Open spi.
     */
    public void openSPI() {
        if (!mSpiOpenSuccess) {
            Object obj = mCardLanDevCtrl.callSPIOpen(spiPath, flags);
            if (obj != null) {
                mSpiOpenSuccess = true;
            }
            mBaseLogClass.printLog(obj.toString());
        }
    }

    public void start() {
        mSPIDispathNonUIThread = new SPIDispathNonUIThread();
        mSPIDispathNonUIThread.start();
        Spi_rev_thread thread = new Spi_rev_thread();
        mSpi_exit_Flag = false;
        thread.start();
    }

    public void exitSPIThread() {
        mSpi_exit_Flag = true;
        if (mSPIDispathNonUIThread != null) {
            mSPIDispathNonUIThread.quit();
        }
        mSpiOpenSuccess = false;
    }
    /**
     * Open sc.
     *
     * @param scNumber the sc number
     */

    public void openSc(String scNumber) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("openSc --> scNumber : " + scNumber);
            return;
        }
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_open);
        cmdBytes = ByteUtil.addBytes(cmdBytes, lenArray);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("openSc", resultBytes);
    }

    /**
     * Cold start.
     *
     * @param scNumber the sc number
     */
    public void coldStart(String scNumber) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("coldStart --> scNumber : " + scNumber);
            return;
        }
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_colde_start);
        cmdBytes = ByteUtil.addBytes(cmdBytes, lenArray);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("coldStart", resultBytes);
    }

    /**
     * Close sc.
     *
     * @param scNumber the sc number
     */
    public void closeSc(String scNumber) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("closeSc --> scNumber : " + scNumber);
            return;
        }
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_close);
        cmdBytes = ByteUtil.addBytes(cmdBytes, lenArray);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("closeSc", resultBytes);
    }
    /**
     * Get can read data byte [ ].
     *
     * @param scNumber the sc number
     * @return the byte [ ]
     */
    public byte[] getCanReadData(String scNumber) {
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_data_length);
        cmdBytes = ByteUtil.addBytes(cmdBytes, lenArray);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        byte[] resultBytes = sendCmd(cmdBytes);

        return resultBytes;
    }

    /**
     * Read data length byte [ ].
     *
     * @param scNumber the sc number
     * @return the byte [ ]
     */
    public byte[] readDataLength(String scNumber) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("readDataLength --> scNumber : " + scNumber);
            return null;
        }
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_data_length);
        cmdBytes = ByteUtil.addBytes(cmdBytes, lenArray);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("readDataLength", resultBytes);
        return resultBytes;
    }

    /**
     * Set baudrate byte [ ].
     *
     * @param scNumber the sc number
     * @param baudrate the baudrate
     * @return the byte [ ]
     */
    public byte[] setBaudrate(String scNumber, String baudrate) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("setBaudrate --> scNumber : " + scNumber);
            //            return null;
            baudrate = "2580";
        }

        byte[] sendArrayLength = null;
        byte[] baudrateBytes = ByteUtil.hexStringToByteArray(baudrate);
        if (!ByteUtil.notNull(baudrate)) {
            mBaseLogClass.printLog("setBaudrate --> baudrate : " + baudrate);
            sendArrayLength = lenArray;
        } else {
            int sendDataLength = baudrateBytes.length;
            sendArrayLength = ByteUtil.reverseByteArray(ByteUtil.intToByteArray(sendDataLength));
        }

        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_baudrate);
        cmdBytes = ByteUtil.addBytes(cmdBytes, sendArrayLength);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        cmdBytes = ByteUtil.addBytes(cmdBytes, baudrateBytes);

        return  sendCmd(cmdBytes);
    }

    /**
     * Read sc data byte [ ].
     *
     * @param scNumber   the sc number
     * @param readLength the read length
     * @return the byte [ ]
     */
    public byte[] readScData(String scNumber, byte[] readLength) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("readScData --> scNumber : " + scNumber);
            return null;
        }

        byte[] sendArrayLength = null;

        if (!ByteUtil.notNull(readLength)) {
            mBaseLogClass.printLog("readScData --> readLength : " + readLength);
            sendArrayLength = lenArray;
        } else {
            int sendDataLength = readLength.length;
            sendArrayLength = ByteUtil.reverseByteArray(ByteUtil.intToByteArray(sendDataLength));
        }
        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_read);
        cmdBytes = ByteUtil.addBytes(cmdBytes, sendArrayLength);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        cmdBytes = ByteUtil.addBytes(cmdBytes, readLength);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("readScData", resultBytes);
        return resultBytes;
    }

    /**
     * Write sc data.
     *
     * @param scNumber the sc number
     * @param scData   the sc data
     */
    public void writeScData(String scNumber, String scData) {
        if (!ByteUtil.notNull(scNumber)) {
            mBaseLogClass.printLog("writeScData --> scNumber : " + scNumber);
            return;
        }

        byte[] sendArrayLength = null;

        if (!ByteUtil.notNull(scData)) {
            mBaseLogClass.printLog("writeScData --> scData : " + scData);
            sendArrayLength = lenArray;
        } else {
            //            byte[] scDataBytes = ByteUtil.hexStringToByteArray(scData);
            int sendDataLength = scData.length() / 2;
            sendArrayLength = ByteUtil.reverseByteArray(ByteUtil.intToByteArray(sendDataLength));
        }

        byte   scNumberByte = ByteUtil.hexStringToByte(scNumber);
        byte[] scDataBytes  = ByteUtil.hexStringToByteArray(scData);
        byte[] cmdBytes     = ByteUtil.addBytes(head, scNumberByte);
        cmdBytes = ByteUtil.addBytes(cmdBytes, cmd_write);
        cmdBytes = ByteUtil.addBytes(cmdBytes, sendArrayLength);
        cmdBytes = ByteUtil.addBytes(cmdBytes, end);
        cmdBytes = ByteUtil.addBytes(cmdBytes, scDataBytes);
        byte[] resultBytes = sendCmd(cmdBytes);
        mBaseLogClass.printLog("writeScData", resultBytes);
    }
    /**
     * Voice to speech at TTS;
     * @param scData Voice text;
     */
    public void setCmd_play(String scData) {
        if (!ByteUtil.notNull(scData)) {
            mBaseLogClass.printLog("writeScData --> scNumber : " + scData);
            return;
        }

        byte[] str_arg_len = {0x00, 0x00, 0x00, 0x00};
        byte[] scDataBytes = null;
        try {
            scDataBytes = scData.getBytes("GB2312");
            if (!ByteUtil.notNull(scDataBytes)) {
                mBaseLogClass.printLog("writeScData --> scData : " + scDataBytes);
            str_arg_len[0] = (byte) scDataBytes.length;
            } else {
                //            byte[] scDataBytes = ByteUtil.hexStringToByteArray(scData);

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        write((byte) 0x30, (byte) 0x03, str_arg_len, scDataBytes);

        mBaseLogClass.printLog("writeScData", scDataBytes);
    }


    public byte[] write(byte dev_number, byte cmd, byte[] ar_arg_len, byte[] arg_array) {
        if (ar_arg_len == null) {
            return null;
        }
        /*
        Check transfer parameter length
        * */
        int arg_len = ar_arg_len[0] + ar_arg_len[1] + ar_arg_len[2] + ar_arg_len[3];
        if (arg_array == null && arg_len == 0) {
            /*Spi communication without parameters*/

        } else if (arg_array != null && arg_array.length == arg_len) {
            /*Spi communication with parameters*/

        } else /* error */ {
            return null;
        }
        byte[] Send_Data_Bytes = new byte[0];
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, head);
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, dev_number);
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, cmd);
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, ar_arg_len);
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, end);
        Send_Data_Bytes = ByteUtil.addBytes(Send_Data_Bytes, arg_array);
        Log.d("send",ByteUtil.byteArrayToHexString(Send_Data_Bytes));
        return sendCmd(Send_Data_Bytes);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public byte[] write(String dev_number, String cmd, String str_arg_len, String str_arg) {
        if (ByteUtil.notNull(dev_number) == false || ByteUtil.notNull(cmd) == false || ByteUtil
                .notNull(str_arg_len) == false || str_arg_len.length() != 8) {
            return null;
        }
        byte b_dev_number = ByteUtil.hexStringToByte(dev_number);
        byte b_cmd = ByteUtil.hexStringToByte(cmd);
        byte[] ar_arg_len = ByteUtil.hexStringToByteArray(str_arg_len);
        byte[] ar_arg = null;
        if (str_arg != null || str_arg.isEmpty() != true) {
            ar_arg = ByteUtil.hexStringToByteArray(str_arg);
        }
        return write(b_dev_number, b_cmd, ar_arg_len, ar_arg);
    }

    public byte[] sendCmd(byte[] cmdBytes) {
        if (!ByteUtil.notNull(cmdBytes)) {
            return null;
        }

        int length = cmdBytes.length;
        int remain = length % sendLength;
        int fori = remain > 0 ? (length / sendLength) + 1 : length / sendLength;
        byte[] returnBytes = null;

        for (int i = 0; i < fori; i++) {
            byte[] sendBytes = new byte[sendLength];
            int start = i * sendLength;
            int cmdLength = sendLength;
            if (remain > 0) {
                if (i == fori - 1) {
                    cmdLength = length - i * sendLength;
                    System.arraycopy(cmdBytes, start, sendBytes, 0, cmdLength);
                } else {
                    sendBytes = ByteUtil.copyBytes(cmdBytes, start, cmdLength);
                }
            } else {
                sendBytes = ByteUtil.copyBytes(cmdBytes, start, cmdLength);
            }

            byte[] callBackBytes = mCardLanDevCtrl.callSPITransfer(sendBytes);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            returnBytes = ByteUtil.addBytes(returnBytes, callBackBytes);
        }
        return returnBytes;
    }

    /**
     * Thread for receiver ;
     */
    class Spi_rev_thread extends Thread {

        private int index = 0;

        @Override
        public void run() {
            super.run();
            while (!mSpi_exit_Flag) {
                openSPI();
                byte[] buff = null;
//                byte[] bytes = FileHelper.getFileBytes("/sys/class/gpio/gpio59/value", null);
                String gpio59_flag = readItems("/sys/class/gpio/gpio59/value");
//                String gpio59_flag = new String(bytes);
                if (gpio59_flag.compareTo("0") == 0) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    index++;
                    continue;
                }
                buff = SendResqustPacket();
                if (!ByteUtil.notNull(buff)) {
                    continue;
                }

                buff = getReadData(buff[0]);
                if (!ByteUtil.notNull(buff)) {
                    continue;
                }
                if (index <= 0) {
                    index++;
                    continue;
                }
                //dispatch spi buffer
                mSPIDispathNonUIThread.addMessage(buff, mSPIDispathNonUIThread);
            }
        }
    }
    public static String readItems(String logFile) {
        // Hard code adding some delay, to distinguish reading from memory and reading disk clearly
        //读取文件
        BufferedReader br = null;
        StringBuffer   sb = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
            sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }
    private SpiCallBackLinstener mSpiKeyCodeLinstener;

    private class SPIDispathNonUIThread extends LoopNonUIThread<byte[]> {

        //        private byte[]
//        private List<byte[]> mSpiBytes = new ArrayList<byte[]>();

        @Override
        public void doHandlerMsg(byte[] bytes) {
            //do samething in this thread
            if (!ByteUtil.notNull(bytes)) {
                printLog("receive message is nulll");
            }

//            printLog(ByteUtil.byteArrayToHexArray(bytes));

//            String s_msg = ByteUtil.byteArrayToHexString(bytes);

//            mSpiBytes.add(bytes);
            boolean hasNextData = true;
//            byte[] tempBytes = mSpiBytes.get(0);
//            mSpiBytes.remove(0);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes.length < 8 +i) {
                    continue;
                }
                if (bytes[i] != head[0] || bytes[i + 7] != end) {
                    continue;
                }
                int arg1_len = bytes[3] - bytes[3]%4 + (bytes[3]%4 != 0 ? 4 : 0 );
                int arg2_len = bytes[4] - bytes[4]%4 + (bytes[4]%4 != 0 ? 4 : 0 );
                int arg3_len = bytes[5] - bytes[5]%4 + (bytes[5]%4 != 0 ? 4 : 0 );
                int arg4_len = bytes[6] - bytes[6]%4 + (bytes[6]%4 != 0 ? 4 : 0 );
                byte[] respond_buff = ByteUtil.copyBytes(bytes, i, 8 + arg1_len +  arg2_len + arg3_len + arg4_len);
                if (ByteUtil.notNull(respond_buff) == false) {
                    continue;
                }
                //Log.d("spi receive3", ByteUtil.byteArrayToHexArray(respond_buff));

                {
                    byte code = respond_buff[0];
                    if (mSpiKeyCodeLinstener != null) {
                        mSpiKeyCodeLinstener.receiveSpiData(ByteUtil.byteToHexString(code),respond_buff);
                    }
                    //RxBus.get().send(ByteUtil.byteToInt(code), respond_buff);
                }

                {
                    byte code = respond_buff[1];
                    switch (code) {
                        case 0x50:
                        case 0x51:
                        case 0x52:
                        case 0x60:
                        case 0x61:
                        case 0x62: {
                            if (mSpiKeyCodeLinstener != null) {
                                mSpiKeyCodeLinstener.receivePasmData("pasm",respond_buff);
                            }
                            //RxBus.get().send(0x5060, respond_buff);
                        }
                        break;
                        case 0x20: //This is the little keyboard;
                            if (respond_buff.length < 8) {
                                return;
                            }
                            byte   keyByte = respond_buff[8];
                            String key     = "";
                            if (keyByte == 0x41) {
                                key = "F1";
                            } else if (keyByte == 0x42) {
                                key = "F2";
                            } else if (keyByte == 0x2E) {
                                key = ".";
                            } else if (keyByte == 0x08) {
                                key = "清除";
                            } else if (keyByte == 0x0D) {
                                key = "确定";
                            } else if (keyByte >= 0x30 && keyByte <= 0x39) {
                                byte start = 0x30;
                                key = String.valueOf(keyByte - start);
                            } else {
                                key = "unknow";
                            }

                            if (mSpiKeyCodeLinstener != null) {
                                mSpiKeyCodeLinstener.receiveKeyCode(key,keyByte);

                            }
                            break;
                        case 0x10:

                            break;
                        default: {
                            //RxBus.get().send(code, respond_buff);
                        }
                        break;
                    }
                }
            }
//            int dataLength = 0;
//            byte[] usbHeadBytes = {0x02, 0x20, 0x02};
//            int copyStart = 0;
//            while (hasNextData) {
//
//                if (bytes.length < 8) {
//                    //数据错误了
//                    printLog("SPI数据错误了");
//                    return;
//                }
//
//                for (int i = 0; i < 4; i++) {
//                    dataLength += ByteUtil.byteToInt(bytes[i + usbHeadBytes.length]);
//                }
//                if (dataLength <= 0) {
//                    //没有数据
//                    printLog("SPI没有数据");
//                    return;
//                }
//                if (bytes.length < 8 + dataLength) {
//                    printLog("SPI数据不全");
//                    return;
//                }
//
//                byte[] copyHeadBytes = ByteUtil.copyBytes(bytes, copyStart, usbHeadBytes.length);
//
//                boolean isUsbCmd = Arrays.equals(usbHeadBytes, copyHeadBytes);
//                if (isUsbCmd) {
//                    //usb　的数据
//                    byte   keyByte = bytes[8];
//                    String key     = "";
//                    if (keyByte == 0x41) {
//                        key = "F1";
//                    } else if (keyByte == 0x42) {
//                        key = "F2";
//                    } else if (keyByte == 0x2E) {
//                        key = ".";
//                    } else if (keyByte == 0x08) {
//                        key = "清除";
//                    } else if (keyByte == 0x0D) {
//                        key = "确定";
//                    } else if (keyByte >= 0x30 && keyByte <= 0x39) {
//                        byte start = 0x30;
//                        key = String.valueOf(keyByte - start);
//                    } else {
//                        key = "unknow";
//                    }
//
//                    if (mSpiKeyCodeLinstener != null) {
//                        //
//                        mSpiKeyCodeLinstener.receiveKeyCode(key,keyByte);
//
//                    }
//                }
//                int fillCount = dataLength % 4;
//                if (fillCount > 0) {
//                    //说明需要填充字节
//                    copyStart = (8 + dataLength + (4 - fillCount));
//                } else {
//                    copyStart = (8 + dataLength);
//                }
//
//                if (bytes.length < copyStart + 8) {
//                    //说明没有数据了
//                    hasNextData = false;
//                    continue;
//                }
//                bytes = ByteUtil.copyBytes(bytes, copyStart, bytes.length - copyStart);
//            }

        }

        @Override
        public void doHandlerMessage(Message handlerMessage) {

        }
    }

    /**
     * Echo keyboard values.
     * @param keyByte
     */
    public void sendKeyboard(byte keyByte) {
        //;
        byte[] uHeadBytes = {0x02, 0x20, 0x01,0x13,0x00,0x00,0x00,0x03};

        byte[] Y485_line_2 = {0x1B,0x53,0x31,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20};

        if (keyByte >= 0x30 && keyByte <= 0x39) {
            if (count > 18) { //
                return ;
            } else {
                count++;
                Y485_line_1[count] = keyByte;
            }
        } else if (keyByte == 0x08) { //;
            if (count<3) { //
                return ;
            } else {
                Y485_line_1[count]= 0x20;
                count--;
            }
        } else if (keyByte == 0x2E) {
            //.
            if (contains(keyByte)) {
                return;
            } else {
                if (count > 18 || count < 3) { //
                    return ;
                } else {
                    count++;
                    Y485_line_1[count] = keyByte;
                }
            }

        }
        sendCmd(ByteUtil.addBytes(uHeadBytes, Y485_line_1));

    }

    private boolean contains(byte keyByte) {
        for (byte b: Y485_line_1) {
            if (b == keyByte) {
                return true;
            }
        }
        return false;
    }

    public void setmSpiCallBackLinstener(SpiCallBackLinstener mSpiKeyCodeLinstener) {
        this.mSpiKeyCodeLinstener = mSpiKeyCodeLinstener;
    }

    public interface SpiCallBackLinstener {
        /**
         * SPi keypad callback,
         * @param keyCode  valuve of the Keyboard.
         * @param keyByte  byte of Keyboard;
         */
        void receiveKeyCode(String keyCode, byte keyByte);

        /**
         * Pasm CallBack
         * @param code  command of the Pasm.
         * @param respondBuff  respond of CallBack
         */
        void receivePasmData(String code, byte[] respondBuff);

        /**
         * SPi CallBack
         * @param code  command of the SPi.
         * @param respondBuff  respond of CallBack.
         */
        void receiveSpiData(String code, byte[] respondBuff);
    }


    public byte[] SendResqustPacket() {
        byte[] buff = null;
        byte[] returnbuff = null;
    /*==========================新版的写法==================================*/
        mCardLanDevCtrl.callSPITransfer(respond_cmd);
        buff = mCardLanDevCtrl.callSPITransfer(get_data);
        if (buff == null || buff.length != 4) {
            return null;
        }
        returnbuff = ByteUtil.addBytes(returnbuff, buff);
        Log.d(" Spi Respond Data:", ByteUtil.byteArrayToHexString(returnbuff));
        return returnbuff;
    /*==================================旧版的写法===================================*/
//        for (int i = 0; i < 3; i++) {
//            buff = mCardLanDevCtrl.callSPITransfer(respond_data);
//            if (buff == null || buff.length != 4) {
//                continue;
//            }
//            if (buff[0] == head) {
//                break;
//            }
//        }
//        if (buff == null || buff.length != 4 || buff[0] != head) {
//            return null;
//        }
//        returnbuff = ByteUtil.addBytes(returnbuff, buff);
//        buff = mCardLanDevCtrl.callSPITransfer(get_data);
//        if (buff == null || buff.length != 4 || buff[3] != end) {
//            return null;
//        }
//        returnbuff = ByteUtil.addBytes(returnbuff, buff);
//        Log.d(" Spi Respond Data:", ByteUtil.byteArrayToHexString(returnbuff));
//
//        return returnbuff;
    }

    public byte[] getReadData(int len) {
        byte[] returnbuff = null;
    /*================================新版的写法================================*/
        mCardLanDevCtrl.callSPITransfer(get_cmd);
        for (int i = 0; i < len; i = i + 4) {
            byte[] callBackBytes = mCardLanDevCtrl.callSPITransfer(get_data);
            if (callBackBytes == null || callBackBytes.length != 4) {
                return null;
            }
            returnbuff = ByteUtil.addBytes(returnbuff, callBackBytes);
        }
        if (ByteUtil.notNull(returnbuff) == false) {
            return null;
        }
        Log.d(" Spi Read Data", ByteUtil.byteArrayToHexString(returnbuff));
        return returnbuff;

/*=================================旧版的写法===============================*/
//        for (int i = 0; i < len; i = i + 4) {
//            byte[] callBackBytes = mCardLanDevCtrl.callSPITransfer(get_data);
//            if (callBackBytes == null || callBackBytes.length != 4) {
//                return null;
//            }
//            returnbuff = ByteUtil.addBytes(returnbuff, callBackBytes);
//        }
//
//        if (ByteUtil.notNull(returnbuff) == false) {
//            return null;
//        }
//        Log.d(" Spi Read Data", ByteUtil.byteArrayToHexString(returnbuff));
//        return returnbuff;
    }


    private void printLog(String message) {
        mBaseLogClass.printLog(message);
    }
}
