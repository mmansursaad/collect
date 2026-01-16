package com.yedc.android.support.pages;

public class ServerAuthDialog extends Page<ServerAuthDialog> {

    @Override
    public ServerAuthDialog assertOnPage() {
        assertText(com.yedc.strings.R.string.server_requires_auth);
        return this;
    }

    public ServerAuthDialog fillUsername(String username) {
        inputText(com.yedc.strings.R.string.username, username);
        return this;
    }

    public ServerAuthDialog fillPassword(String password) {
        inputText(com.yedc.strings.R.string.password, password);
        return this;
    }

    public <D extends Page<D>> D clickOK(D destination) {
        clickOnString(com.yedc.strings.R.string.ok);
        return destination.assertOnPage();
    }
}
