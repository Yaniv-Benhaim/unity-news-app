package com.unitynews.contract;

import com.unitynews.contract.BackendStatusDto;

interface IBackendStatusCallback {
    void onSuccess(in BackendStatusDto status);
    void onError(String code, String message);
}
