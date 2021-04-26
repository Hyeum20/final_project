package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpActivity extends AppCompatActivity {
    public TextView Toptext;
    public Button StartButton;
    public Button ConnButton;
    public Button moveDetPage;

    public static Socket socket = null;
    public static BufferedReader networkReader = null;
    public static BufferedWriter networkWriter = null;

    String TAG = TcpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);

        ConnButton = findViewById(R.id.button1);
        StartButton = findViewById(R.id.button2);
        moveDetPage = findViewById(R.id.moveDetPage);

        final EditText ipNumber = findViewById(R.id.ipText);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        ConnButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Connect 시도", Toast.LENGTH_SHORT).show();
                String addr = ipNumber.getText().toString().trim();
                ConnectThread thread = new ConnectThread(addr);

                //키보드 자동 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ipNumber.getWindowToken(), 0);

                thread.start();

            }
        });


        // mesage Test
        StartButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartThread sthread = new StartThread();

                sthread.start();

            }
        });

        // 페이지 이동
        moveDetPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                startActivity(intent);
            }
        });



    }

    // 데이터 송신
    class StartThread extends Thread{

        public StartThread(){
        }


        public void run(){
            // 데이터 송신
            try {
                PrintWriter out = new PrintWriter(networkWriter, true);
                out.println("메시지 전송 Test!!!!!!!!!!!");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"데이터 송신 오류");
            }
        }

    }

    // 연결 스레드
    class ConnectThread extends Thread {
        String hostname;

        public ConnectThread(String addr) {
            hostname = addr;
        }

        public void run() {
            try { //클라이언트 소켓 생성

                int port = 0000;
                socket = new Socket(hostname, port);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream() , "euc-kr"));
                networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream() , "euc-kr"));

                Log.d(TAG, "Socket 생성, 연결.");

                Toptext = findViewById(R.id.text1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InetAddress addr = socket.getInetAddress();

                        String tmp = addr.getHostAddress();

                        Toptext.setText(tmp + " 연결 완료");
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

                        ConnButton.setEnabled(false);
                        StartButton.setEnabled(true);


//                        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
//                        int width = dm.widthPixels;
//                        int height = dm.heightPixels;

                        // 화면 크기
                        Display display = getWindowManager().getDefaultDisplay();
                        DisplayMetrics outMetrics = new DisplayMetrics ();
                        display.getMetrics(outMetrics);

                        float density = getResources().getDisplayMetrics().density;
                        float height = outMetrics.heightPixels / density;
                        float width = outMetrics.widthPixels / density;
                        //dp별 layout 별도 적용


                        // 화면 크기 넘겨주기
                        PrintWriter out = new PrintWriter(networkWriter, true);
                        out.println(width+","+height);
                        
                    }
                });

            } catch (UnknownHostException uhe) { // 소켓 생성 시 전달되는 호스트(www.unknown-host.com)의 IP를 식별할 수 없음.

                Log.e(TAG, " 생성 Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                    }
                });

            } catch (IOException ioe) { // 소켓 생성 과정에서 I/O 에러 발생.

                Log.e(TAG, " 생성 Error : 네트워크 응답 없음");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음", Toast.LENGTH_SHORT).show();
                        Toptext.setText("네트워크 연결 오류");
                    }
                });


            } catch (SecurityException se) { // security manager에서 허용되지 않은 기능 수행.

                Log.e(TAG, " 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                    }
                });


            } catch (IllegalArgumentException le) { // 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

                Log.e(TAG, " 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                    }
                });


            }
        }
    }

//    @Override
//    protected void onStop() {  //앱 종료시
//        super.onStop();
//        try {
//            socket.close(); //소켓을 닫는다.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}