package jvalhondo.android.app;

import android.app.Activity;
import android.os.Bundle;
import jvalhondo.android.app.R;

public class HelpActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_view);
        setTitle(R.string.help_title);
    }

}
