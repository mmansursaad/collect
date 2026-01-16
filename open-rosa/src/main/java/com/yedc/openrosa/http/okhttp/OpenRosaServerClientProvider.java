package com.yedc.openrosa.http.okhttp;

import androidx.annotation.NonNull;

import com.yedc.openrosa.http.HttpCredentialsInterface;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @NonNull HttpCredentialsInterface credentialsInterface);
}
