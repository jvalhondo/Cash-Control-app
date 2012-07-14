package jvalhondo.android.app;

import android.app.Activity;
import android.os.Bundle;
import jvalhondo.android.app.R;

public class AboutActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        setTitle(R.string.about_title);
    }
}
