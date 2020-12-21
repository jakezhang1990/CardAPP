package com.cardlan.carddemoapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.cardlan.carddemoapp.util.CardReadWriteUtil;
import com.cardlan.carddemoapp.util.CardReadWriteUtil2;
import com.cardlan.carddemoapp.util.TimeThread;
import com.cardlan.utils.ByteUtil;

/**
 * 主页面
 */
public class MainActivity2 extends Activity {

    private static final String TAG = MainActivity2.class.getSimpleName();
    private TextView dateTV;
    TimeThread timeThread;

    CardReadWriteUtil mCardReadWriteUtil;

    private TextView tv_title,tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("余额查询");
        tv_content=findViewById(R.id.tv_content);
        //tv_content.setText("");

        dateTV=findViewById(R.id.dateTV);
        timeThread=new TimeThread(dateTV);
        timeThread.start();

        //read M1 card;
        mCardReadWriteUtil = new CardReadWriteUtil();
        byte[] cardSnByteArray = mCardReadWriteUtil.getCardResetBytes();
        Log.i(TAG, "read cardSn = " + ByteUtil.byteArrayToHexString(cardSnByteArray));

        if (!ByteUtil.notNull(cardSnByteArray)) {
            Toast.makeText(this, "read not find the card !", Toast.LENGTH_SHORT).show();
            return;

        }

        char readSector = stringToChar("10");//10扇区号
        char readindex = stringToChar("2");//2块
        byte sector = ByteUtil.intToByteTwo(readSector);
        byte index = ByteUtil.intToByteTwo(readindex);
        Log.i(TAG, "sector = " + sector + " index= " + index);

        //按位取反并且转为16进制字符串
        String cardSnHexStringNot = CardReadWriteUtil2.byteNotHexStr(cardSnByteArray);
        Log.i(TAG, "read cardSnHexStringNot = " + cardSnHexStringNot);
        String byte4Sn = cardSnHexStringNot.substring(0, 8);//截取前8位
        Log.i(TAG, "read substring 4byte hex = " + byte4Sn);
        String keyA = "0A" + byte4Sn + "81";   //用户卡keyA生成规则
        Log.i(TAG, "read keyA = " + keyA);

        byte[] readTemp = null;
        // the subsequent reads and writes need to be written using the computed read key
        readTemp = mCardReadWriteUtil.callReadJNI(ByteUtil.byteToHex(sector),
                ByteUtil.byteToHex(index), keyA, null);
        if (ByteUtil.notNull(readTemp)) {
            //老卡读卡
            tv_content.setText("当前余额： "+ ByteUtil.byteArrayToHexString(readTemp));
            Toast.makeText(this, "读卡成功", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "卡片不合法，读卡失败", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeThread!=null){
            timeThread.interrupt();
        }
    }

    private char stringToChar(String string) {
        if (!ByteUtil.notNull(string)) {
            string = "0";
        }
        int ivalue = Integer.parseInt(string);
        return (char) ivalue;
    }

}