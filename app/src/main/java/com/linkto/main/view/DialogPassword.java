package com.linkto.main.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.linkto.scatter.R;

public class DialogPassword extends Dialog {
	public interface OnClickListener {
		void onClick(String password);
	}

	private OnClickListener mOnClickListener;

	private EditText mEtPassword;

	public DialogPassword(Context context) {
		super(context, R.style.simple_dialog);
		init();
	}

	private void init() {
		setContentView(R.layout.dialog_password);

		Window window = getWindow();
		if (window == null) {
			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		if (manager != null) {
			Display display = manager.getDefaultDisplay();
			display.getMetrics(metrics);
		}

		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = metrics.widthPixels * 8 / 10;
		lp.height = metrics.heightPixels * 5 / 10;
		window.setAttributes(lp);

		mEtPassword = findViewById(R.id.et_password);

		findViewById(R.id.btn_confirm).setOnClickListener((view) -> {
			if (mOnClickListener != null) {
				mOnClickListener.onClick(mEtPassword.getText().toString());
			}

			dismiss();
		});

		findViewById(R.id.btn_cancel).setOnClickListener((view) -> {
			if (mOnClickListener != null) {
				mOnClickListener.onClick(null);
			}

			dismiss();
		});

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}
}
