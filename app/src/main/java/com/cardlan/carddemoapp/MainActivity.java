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
import android.widget.RadioGroup;
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
public class MainActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    CardReadWriteUtil mReadWriteUtil = new CardReadWriteUtil();
    private EditText lbl1_index_Editv;
    private Button lbl1_btn;
    private TextView lbl1_read_result_tv, lbl1_key_tv;

    private EditText lbl2_sector_Editv, lbl2_index_Editv, lbl2_key_Editv;
    private RadioGroup lbl2_keyTypeRG;
    private Button lbl2_btn;
    private TextView lbl2_read_result_tv;
    private String read_type="0a";
    private String write_type="0a";

    private EditText lbl3_index_Editv, lbl3_write_data_Editv;
    private Button lbl3_btn;
    private TextView lbl3_write_result_tv, lbl3_key_tv;
    private String TAG=MainActivity.class.getSimpleName();

    private EditText lbl4_sector_Editv, lbl4_index_Editv, lbl4_key_Editv, lbl4_write_data_Editv;
    private RadioGroup lbl4_keyTypeRG;
    private Button lbl4_btn;
    private TextView lbl4_write_result_tv;

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

        //lbl2
        lbl2_sector_Editv=findViewById(R.id.lbl2_sector_Editv);
        lbl2_index_Editv=findViewById(R.id.lbl2_index_Editv);
        lbl2_key_Editv=findViewById(R.id.lbl2_key_Editv);
        lbl2_keyTypeRG=findViewById(R.id.lbl2_keyTypeRG);
        lbl2_keyTypeRG.setOnCheckedChangeListener(this);
        lbl2_btn=findViewById(R.id.lbl2_btn);
        lbl2_btn.setOnClickListener(this);
        lbl2_read_result_tv=findViewById(R.id.lbl2_read_result_tv);

        //lbl3
        lbl3_index_Editv=findViewById(R.id.lbl3_index_Editv);
        lbl3_write_data_Editv=findViewById(R.id.lbl3_write_data_Editv);
        lbl3_btn=findViewById(R.id.lbl3_btn);
        lbl3_write_result_tv=findViewById(R.id.lbl3_write_result_tv);
        lbl3_key_tv=findViewById(R.id.lbl3_key_tv);
        lbl3_btn.setOnClickListener(this);

        //lbl4

        lbl4_sector_Editv=findViewById(R.id.lbl4_sector_Editv);
        lbl4_index_Editv=findViewById(R.id.lbl4_index_Editv);
        lbl4_key_Editv=findViewById(R.id.lbl4_key_Editv);
        lbl4_write_data_Editv=findViewById(R.id.lbl4_write_data_Editv);
        lbl4_keyTypeRG=findViewById(R.id.lbl4_keyTypeRG);
        lbl4_keyTypeRG.setOnCheckedChangeListener(this);
        lbl4_btn=findViewById(R.id.lbl4_btn);
        lbl4_btn.setOnClickListener(this);
        lbl4_write_result_tv=findViewById(R.id.lbl4_write_result_tv);
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


                //按位取反并且转为16进制字符串
                String cardSnHexStringNot = CardReadWriteUtil2.byteNotHexStr(cardSnByteArray);

                String byte4Sn = cardSnHexStringNot.substring(0, 8);//截取前8位

                String keyA = "0A" + byte4Sn + "81";   //用户卡keyA生成规则
                Log.i(TAG, "sector = " + sector + " index= " + index+" keyA="+keyA);

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
            case R.id.lbl2_btn:
                //read
                byte[] cardSnByteArray2 = mReadWriteUtil.getCardResetBytes();
                Log.i(TAG, "read cardSn2 = " + ByteUtil.byteArrayToHexString(cardSnByteArray2));
                if (!ByteUtil.notNull(cardSnByteArray2)) {
                    Toast.makeText(this, "read not find the card !", Toast.LENGTH_SHORT).show();
                    return;
                }
//                char readSector2 = CardReadWriteUtil2.stringToChar();
                byte readSector2=0;
                if (!TextUtils.isEmpty(lbl2_sector_Editv.getText().toString())){
                    readSector2=Byte.valueOf(lbl2_sector_Editv.getText().toString());
                }
                char readindex2 = CardReadWriteUtil2.stringToChar(lbl2_index_Editv.getText().toString());
//                byte sector2 = ByteUtil.intToByteTwo(readSector2);
                byte sector2 = readSector2;
                byte index2 = ByteUtil.intToByteTwo(readindex2);
                String key2=TextUtils.isEmpty(lbl2_key_Editv.getText().toString())?"0ACFD6AE4A81":lbl2_key_Editv.getText().toString();

                Log.i(TAG, "readSector2="+readSector2+"readindex2="+readindex2+"sector2 = " + sector2 + " index2= " + index2+" key2= "+key2+" read_type="+read_type);
                byte[] readTemp2 = null;
                // the subsequent reads and writes need to be written using the computed read key
                if (read_type.equals("0a")){
                    readTemp2 = mReadWriteUtil.callReadJNI(ByteUtil.byteToHex(sector2),
                            ByteUtil.byteToHex(index2), key2, null);
                }else {
                    readTemp2 = mReadWriteUtil.callReadJNI(ByteUtil.byteToHex(sector2),
                            ByteUtil.byteToHex(index2), key2, "0b");
                }

                if (ByteUtil.notNull(readTemp2)) {
                    lbl2_read_result_tv.setText(ByteUtil.byteArrayToHexString(readTemp2));

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
                }else {
                    Toast.makeText(this, "writing failed, writeResult="+writeResult, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.lbl4_btn:
                //write
                byte[] cardSnByteArray4 = mReadWriteUtil.getCardResetBytes();
                Log.i(TAG, "read cardSn4 = " + ByteUtil.byteArrayToHexString(cardSnByteArray4));
                if (!ByteUtil.notNull(cardSnByteArray4)) {
                    Toast.makeText(this, "read not find the card !", Toast.LENGTH_SHORT).show();
                    return;
                }
//                char readSector2 = CardReadWriteUtil2.stringToChar();
                String readSector4="0";
                if (!TextUtils.isEmpty(lbl4_sector_Editv.getText().toString())){
                    readSector4=lbl4_sector_Editv.getText().toString();
                }

                String readindex4 = "0";
                if (!TextUtils.isEmpty(lbl4_index_Editv.getText().toString())){
                    readindex4=lbl4_index_Editv.getText().toString();
                }

                String key4=TextUtils.isEmpty(lbl4_key_Editv.getText().toString())?"0ACFD6AE4A81":lbl4_key_Editv.getText().toString();

                int writeResult4 =0;
                if (write_type.equals("0a")){
                    writeResult4=mReadWriteUtil.callWriteJNI(readSector4,
                            readindex4,
                            ByteUtil.byteArrayToHexString(new byte[4]),
                            key4, null);
                }else {
                    writeResult4=mReadWriteUtil.callWriteJNI(readSector4,
                            readindex4,
                            ByteUtil.byteArrayToHexString(new byte[4]),
                            key4, "0b");
                }

                if (writeResult4 == 5) {
                    lbl4_write_result_tv.setText(getResources().getString(R.string.m1_write_value) + writeResult4);
                    Toast.makeText(this, "Writeing successfully !", Toast.LENGTH_SHORT).show();
                    //showToast(getResources().getString(R.string.writing_successfully));
                }else {
                    Toast.makeText(this, "writing failed, writeResult="+writeResult4, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup.equals(lbl2_keyTypeRG)){
            //read choice key type
            if (radioGroup.getCheckedRadioButtonId()==R.id.radio_akey){
                read_type="0a";
            }else{
                read_type="0b";
            }
        }else if (radioGroup.equals(lbl4_keyTypeRG)){
            if (radioGroup.getCheckedRadioButtonId()==R.id.radio4_akey){
                write_type="0a";
            }else {
                write_type="0b";
            }
        }
    }
}
