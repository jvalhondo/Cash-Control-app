package jvalhondo.android.app;

import swcm.android.R;
import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        setTitle(R.string.about_title);
    }
}
