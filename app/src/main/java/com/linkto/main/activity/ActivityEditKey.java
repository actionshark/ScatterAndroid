package com.linkto.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.linkto.main.util.Util;
import com.linkto.main.util.Encryption;
import com.linkto.main.core.Eos;
import com.linkto.main.util.Storage;
import com.linkto.scatter.R;

public class ActivityEditKey extends ActivityBase {
	private EditText mEtPrivateKey;

	private EditText mEtSetPassword;
	private EditText mEtRepeatPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit_key);

		mEtPrivateKey = findViewById(R.id.et_private_key);

		mEtSetPassword = findViewById(R.id.et_set_password);
		mEtRepeatPassword = findViewById(R.id.et_repeat_password);

		findViewById(R.id.btn_confirm).setOnClickListener((view) -> {
			String password = mEtSetPassword.getText().toString();
			if (password.length() < Util.PASSWORD_LENGTH_MIN || password.length() > Util.PASSWORD_LENGTH_MAX) {
				Toast.makeText(ActivityEditKey.this,
						getString(R.string.password_length_error,
								Util.PASSWORD_LENGTH_MIN, Util.PASSWORD_LENGTH_MAX),
						Toast.LENGTH_SHORT).show();
				return;
			}

			String passwordRepeat = mEtRepeatPassword.getText().toString();
			if (!password.equals(passwordRepeat)) {
				Toast.makeText(ActivityEditKey.this,
						R.string.repeat_password_error,
						Toast.LENGTH_SHORT).show();
				return;
			}

			String privateKey = mEtPrivateKey.getText().toString();
			String publicKey = Eos.privateToPublic(privateKey);

			if (publicKey == null) {
				Toast.makeText(ActivityEditKey.this,
						R.string.not_valid_private,
						Toast.LENGTH_SHORT).show();
				return;
			}

			new Thread(() -> {
				String account = Eos.getKeyAccounts(publicKey);
				runOnUiThread(() -> {
					if (account == null) {
						Toast.makeText(ActivityEditKey.this,
								R.string.account_not_exist,
								Toast.LENGTH_SHORT).show();
						return;
					}

					String cipher = Encryption.encode(password, privateKey);
					Storage.set(Util.PRIVATE_KEY_CIPHER, cipher);

					Toast.makeText(ActivityEditKey.this,
							R.string.import_account_success,
							Toast.LENGTH_SHORT).show();

					Intent intent = new Intent();
					intent.setClass(ActivityEditKey.this, ActivityAccount.class);
					startActivity(intent);

					finish();
				});
			}
			).start();

			Toast.makeText(ActivityEditKey.this,
					R.string.wait_a_moment,
					Toast.LENGTH_SHORT).show();
		});
	}
}
