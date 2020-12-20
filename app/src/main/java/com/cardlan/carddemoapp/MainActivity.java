package com.cardlan.carddemoapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cardlan.carddemoapp.util.CardReadWriteUtil;
import com.cardlan.carddemoapp.util.CardReadWriteUtil2;
import com.cardlan.twoshowinonescreen.DeviceCardConfig;
import com.cardlan.utils.ByteUtil;

/**
 * @Description: java类作用描述
 * @Author: jakezhang
 * @CreateDate: 2020/12/20 21:46
 */
public class MainActivity extends Activity implements View.OnClickListener {

    CardReadWriteUtil mReadWriteUtil = new CardReadWriteUtil();
    private EditText lbl1_index_Editv;
    private Button lbl1_btn;
    private TextView lbl1_read_result_tv, lbl1_key_tv;

    private EditText lbl3_index_Editv, lbl3_write_data_Editv;
    private Button lbl3_btn;
    private TextView lbl3_write_result_tv, lbl3_key_tv;
    private String TAG=MainActivity.class.getSimpleName();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //lbl1
        lbl1_index_Editv=findViewById(R.id.lbl1_index_Editv);
        lbl1_btn=findViewById(R.id.lbl1_btn);
        lbl1_key_tv=findViewById(R.id.lbl1_key_tv);
        lbl1_read_result_tv=findViewById(R.id.lbl1_read_result_tv);
        lbl1_btn.setOnClickListener(this);

        //lbl3
        lbl3_index_Editv=findViewById(R.id.lbl3_index_Editv);
        lbl3_write_data_Editv=findViewById(R.id.lbl3_write_data_Editv);
        lbl3_btn=findViewById(R.id.lbl3_btn);
        lbl3_write_result_tv=findViewById(R.id.lbl3_write_result_tv);
        lbl3_key_tv=findViewById(R.id.lbl3_key_tv);
        lbl3_btn.setOnClickListener(this);


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lbl1_btn:
                //read m1 card 10扇区

                byte[] cardSnByteArray = mReadWriteUtil.getCardResetBytes();
                Log.i(TAG, "read cardSn = " + ByteUtil.byteArrayToHexString(cardSnByteArray));
                if (!ByteUtil.notNull(cardSnByteArray)) {
                    Toast.makeText(this, "read not find the card !", Toast.LENGTH_SHORT).show();
                    return;
                }

                //char readSector = stringToChar("10");//10扇区号
                char readindex = stringToChar(lbl1_index_Editv.getText().toString());
//                char readSector = CardReadWriteUtil2.stringToChar(mEditxt_sector_read.getText().toString());
//                char readindex = CardReadWriteUtil2.stringToChar(mEditxt_read_index.getText().toString());
                byte sector = ByteUtil.intToByteTwo(10);
                byte index = ByteUtil.intToByteTwo(readindex);
                Log.i(TAG, "sector = " + sector + " index= " + index);

                //按位取反并且转为16进制字符串
                String cardSnHexStringNot = CardReadWriteUtil2.byteNotHexStr(cardSnByteArray);

                String byte4Sn = cardSnHexStringNot.substring(0, 8);//截取前8位

                String keyA = "0A" + byte4Sn + "81";   //用户卡keyA生成规则
                Log.i(TAG, "read cardSnHexStringNot = " + cardSnHexStringNot+"read substring 4byte hex = " + byte4Sn+"read keyA = " + keyA);

                lbl1_key_tv.setText("keyA: "+keyA);

                byte[] readTemp = null;
                // the subsequent reads and writes need to be written using the computed read key
                readTemp = mReadWriteUtil.callReadJNI(ByteUtil.byteToHex(sector),
                        ByteUtil.byteToHex(index), keyA, null);
                if (ByteUtil.notNull(readTemp)) {
                    lbl1_read_result_tv.setText(ByteUtil.byteArrayToHexString(readTemp));

                    Toast.makeText(this, "read card scuess", Toast.LENGTH_SHORT).show();
                } else {
                    lbl1_read_result_tv.setText("");
                    Toast.makeText(this, "read card failed", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.lbl3_btn:
                //write 10扇区
                byte[] wcardSnByteArray = mReadWriteUtil.getCardResetBytes();
                Log.i(TAG, "cardSn = " + ByteUtil.byteArrayToHexString(wcardSnByteArray));

                if (!ByteUtil.notNull(wcardSnByteArray)) {
                    Toast.makeText(this, "write not find the card !", Toast.LENGTH_SHORT).show();
                    return;
                }

                String wcardSnHexStringNot = CardReadWriteUtil2.byteNotHexStr(wcardSnByteArray);

                String wbyte4Sn = wcardSnHexStringNot.substring(0, 8);//截取前8位

                String write_keyA = "0A" + wbyte4Sn + "81";
                Log.i(TAG, "write cardSnHexStringNot = " + wcardSnHexStringNot+"write substring 4byte hex = " + wbyte4Sn+"write_keyA = " + write_keyA);
                lbl3_key_tv.setText("keyA: "+write_keyA);

               /* String writeSector = "10";
//                String writeSector=mEditxt_sector_write.getText().toString().trim();
                String writeIndex = TextUtils.isEmpty(lbl3_index_Editv.getText().toString().trim()) ? "0" : lbl3_index_Editv.getText().toString().trim();

                String write_data_Hex=ByteUtil.byteArrayToHexString(new byte[4]);
                Log.i(TAG, "用户卡write StatusOfWrite keyA写入参数 = " + "扇区号："+writeSector+"块号："+writeIndex+
                        "写入的16进制数据："+write_data_Hex+"写操作传入的key:"+write_keyA);
                //用户卡写入
                int writeResult = mReadWriteUtil.callWriteJNI(writeSector, writeIndex, write_data_Hex, write_keyA, "0a");
                Log.i(TAG, "write StatusOfWrite keyA写入结果返回码 = " + writeResult);
                if (writeResult == DeviceCardConfig.MONE_CARD_WRITE_SUCCESS_STATUS) {//5
                    // 用户卡A秘钥写入的结果
                    lbl3_write_result_tv.setText("StatusOfWrite is: " + writeResult);
                    Toast.makeText(this, "Writeing successfully !", Toast.LENGTH_SHORT).show();

                }else {
                    lbl3_write_result_tv.setText("");
                    Toast.makeText(this, "writing failed+状态码为： "+writeResult, Toast.LENGTH_SHORT).show();
                }*/

                int writeResult = mReadWriteUtil.callWriteJNI("10",
                        "1",
                        ByteUtil.byteArrayToHexString(new byte[4]),
                        write_keyA, null);
                if (writeResult == 5) {
                    lbl3_write_result_tv.setText(getResources().getString(R.string.m1_write_value) + writeResult);
                    Toast.makeText(this, "Writeing successfully !", Toast.LENGTH_SHORT).show();
                    //showToast(getResources().getString(R.string.writing_successfully));
                }

                break;
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
