package jvalhondo.android.CashControl.app;

import jvalhondo.android.CashControl.app.R;
import android.app.Activity;
import android.os.Bundle;

public class HelpActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_view);
        setTitle(R.string.help_title);
    }
}
