package com.yedc.openrosa.support;

import androidx.annotation.NonNull;

import com.yedc.openrosa.forms.OpenRosaXmlFetcher;
import com.yedc.openrosa.http.HttpCredentials;
import com.yedc.openrosa.http.HttpCredentialsInterface;

import java.net.URI;

public class StubWebCredentialsProvider implements OpenRosaXmlFetcher.WebCredentialsProvider {

    @Override
    public HttpCredentialsInterface getCredentials(@NonNull URI url) {
        return new HttpCredentials(null, null);
    }
}
