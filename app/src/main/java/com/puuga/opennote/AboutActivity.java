package com.puuga.opennote;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    TextView tvVersion;
    TextView tvPerson1;
    TextView tvPerson2;
    TextView tvPerson3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initInstances();
    }

    private void initInstances() {
        tvVersion = (TextView) findViewById(R.id.tv_version);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(getString(R.string.version, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException ignored) {

        }

        tvPerson1 = (TextView) findViewById(R.id.tv_person_1);
        tvPerson2 = (TextView) findViewById(R.id.tv_person_2);
        tvPerson3 = (TextView) findViewById(R.id.tv_person_3);
    }

    public void emailTo(View v) {
        String email = null;
        switch (v.getId()) {
            case R.id.tv_person_1:
                email = "siwaoh@gmail.com";
                break;
            case R.id.tv_person_2:
                email = "nopphawit.p@gmail.com";
                break;
            case R.id.tv_person_3:
                email = "tanapong.tor@gmail.com";
                break;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        startActivity(Intent.createChooser(emailIntent, "Send email to " + email));
    }
}
