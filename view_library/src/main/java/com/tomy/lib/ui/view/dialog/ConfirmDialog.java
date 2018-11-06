package com.tomy.lib.ui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tomy.lib.ui.R;

/**@author Tomy
 * Created by Tomy on 2015-03-11.
 */
public class ConfirmDialog extends Dialog {
    public ConfirmDialog(Context context, int theme) {
        super(context, theme);
        setContentView(R.layout.confirm_dialog);
        setCanceledOnTouchOutside(true);
    }

    public ConfirmDialog(Context context) {
        this(context, R.style.dialog);
    }

    public void setPositiveListener(View.OnClickListener listener) {
        findViewById(R.id.btn_ok).setOnClickListener(listener);
    }

    public void setNegativeListener(View.OnClickListener listener) {
        findViewById(R.id.btn_cancel).setOnClickListener(listener);
    }

    public void setMessage(int msgId) {
        ((TextView) findViewById(R.id.message)).setText(msgId);
    }
}
