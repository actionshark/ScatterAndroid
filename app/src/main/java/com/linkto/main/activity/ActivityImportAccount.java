package com.linkto.main.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.linkto.main.core.AccountMgr;
import com.linkto.main.util.Broadcast;
import com.linkto.main.util.Util;
import com.linkto.scatter.R;

public class ActivityImportAccount extends ActivityBase implements Broadcast.Callback {
	private EditText mEtPrivateKey;

	private EditText mEtSetPassword;
	private EditText mEtRepeatPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_import_account);

		mEtPrivateKey = findViewById(R.id.et_private_key);

		mEtSetPassword = findViewById(R.id.et_set_password);
		mEtRepeatPassword = findViewById(R.id.et_repeat_password);

		findViewById(R.id.btn_confirm).setOnClickListener((view) -> {
			String password = mEtSetPassword.getText().toString();
			if (password.length() < Util.PASSWORD_LENGTH_MIN || password.length() > Util.PASSWORD_LENGTH_MAX) {
				Toast.makeText(ActivityImportAccount.this,
						getString(R.string.password_length_error,
								Util.PASSWORD_LENGTH_MIN, Util.PASSWORD_LENGTH_MAX),
						Toast.LENGTH_SHORT).show();
				return;
			}

			String passwordRepeat = mEtRepeatPassword.getText().toString();
			if (!password.equals(passwordRepeat)) {
				Toast.makeText(ActivityImportAccount.this,
						R.string.repeat_password_error,
						Toast.LENGTH_SHORT).show();
				return;
			}

			String privateKey = mEtPrivateKey.getText().toString();

			AccountMgr.importAccount(privateKey, password);

			Toast.makeText(ActivityImportAccount.this,
					R.string.wait_a_moment,
					Toast.LENGTH_SHORT).show();
		});

		Broadcast.addListener(Broadcast.NAME_ACCOUNT_IMPORTED, this, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Broadcast.removeListener(null, this);
	}

	@Override
	public void onBroadcast(String name, Object data) {
		if (Broadcast.NAME_ACCOUNT_IMPORTED.equals(name)) {
			finish();
		}
	}
}
