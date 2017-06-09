package rudiment.alaramapp.util;

import android.content.Context;

public class Extension {
	ValidationTemplate vali;

	public Extension() {
		vali = new Implementation();
	}

	public boolean executeStrategy(Context context, String text_if_needed, String check_tag) {

		return vali.template(context, text_if_needed, check_tag);

	}

}
