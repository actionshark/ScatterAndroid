package com.linkto.main.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.linkto.scatter.R;

public class DialogSimple extends Dialog {
	public interface OnClickListener {
		void onClick(int index);
	}

	private OnClickListener mOnClickListener;

	public DialogSimple(Context context) {
		super(context, R.style.simple_dialog);
		init();
	}

	private void init() {
		setContentView(R.layout.dialog_simple);

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
		lp.height = metrics.heightPixels * 4 / 10;
		window.setAttributes(lp);

		for (int i = 0; ; i++) {
			int id = getContext().getResources().getIdentifier("btn_" + i, "id",
					getContext().getPackageName());

			if (id == 0) {
				break;
			}

			int index = i;
			findViewById(id).setOnClickListener((view) -> {
				if (mOnClickListener != null) {
					mOnClickListener.onClick(index);
				}

				dismiss();
			});
		}

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	public void setContent(Object resId) {
		TextView tv = findViewById(R.id.tv_content);

		if (resId instanceof Integer) {
			tv.setText((int) resId);
		} else if (resId instanceof String) {
			tv.setText((String) resId);
		}
	}

	public void setButton(Object... texts) {
		for (int i = 0; i < texts.length; i++) {
			int id = getContext().getResources().getIdentifier("btn_" + i, "id",
					getContext().getPackageName());
			Button btn = findViewById(id);
			btn.setVisibility(View.VISIBLE);

			Object text = texts[i];

			if (text instanceof Integer) {
				btn.setText((int) text);
			} else if (text instanceof String) {
				btn.setText((String) text);
			}
		}

		for (int i = texts.length; ; i++) {
			int id = getContext().getResources().getIdentifier("btn_" + i, "id",
					getContext().getPackageName());

			if (id == 0) {
				break;
			}

			findViewById(id).setVisibility(View.GONE);
		}
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}
}
