package pf.aismap;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;


public class SplashScreenActivity extends Activity {

    public static final int segundos = 2;
    public static final int milisegundos = segundos * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreenlyt);
        empezarcuenta();

    }

    public void empezarcuenta() {
        new CountDownTimer(milisegundos,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                Intent intent= new Intent(SplashScreenActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

}
