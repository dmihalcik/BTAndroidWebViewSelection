package net.londatiga.android;

import org.json.JSONObject;

public interface QuickPopupListener {

	boolean onPrepareMenu(QuickAction mContextMenu, JSONObject etc);

}
